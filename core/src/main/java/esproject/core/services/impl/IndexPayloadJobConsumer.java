package esproject.core.services.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.consumer.JobConsumer;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.propertytypes.ServiceDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.AssetManager;
import com.day.cq.replication.ReplicationActionType;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import esproject.core.beans.IndexBean;
import esproject.core.beans.SearchBean;
import esproject.core.configurations.impl.ElasticSearchConfigurationImpl;
import esproject.core.constants.EsSearchConstants;
import esproject.core.constants.IndexingConstants;
import esproject.core.utils.EsSearchUtils;
import esproject.core.services.ResourceResolverService;
import esproject.core.utils.AEMUtils;

/**
 * The Class IndexPayloadJobConsumer.
 */
@Component(service = JobConsumer.class, immediate = true, property = {
		JobConsumer.PROPERTY_TOPICS + "=" + "search/createpayload/job" })
@ServiceDescription("Queue and build jobs while replication")
public class IndexPayloadJobConsumer implements JobConsumer {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(IndexPayloadJobConsumer.class);

	/** The resource resolver service. */
	@Reference
	private ResourceResolverService resourceResolverService;

	/** The resource resolver factory. */
	@Reference
	private ResourceResolverFactory resourceResolverFactory;

	/** The elastic search configuration impl. */
	@Reference
	ElasticSearchConfigurationImpl elasticSearchConfigurationImpl;

	/**
	 * Process.
	 *
	 * @param job the job
	 * @return the job result
	 */
	@Override
	public JobResult process(Job job) {
		try {
			LOGGER.info("Processing the JOB *******");
			String url = (String) job.getProperty(EsSearchConstants.PATH);
			ReplicationActionType actionType = (ReplicationActionType) job.getProperty(EsSearchConstants.ACTION_TYPE);
			LOGGER.info("The path in which the replication is triggered and passed to the Job is " + "{}", url);

			final ResourceResolver resolver = resourceResolverService.getSystemResourceResolver();

			if (Objects.nonNull(resolver) && actionType == ReplicationActionType.ACTIVATE) {
				LOGGER.info("Activate request for " + "{}", url);
				createIndex(resolver, url, actionType);

			} else if (Objects.nonNull(resolver) && actionType == ReplicationActionType.DEACTIVATE) {
				LOGGER.info("Deactivate request for " + "{}", url);
				deleteIndex(resolver, url, actionType);
			}

			return JobResult.OK;
		} catch (JsonProcessingException e) {
			LOGGER.error("Exception  ", e);
			return JobResult.FAILED;
		}
	}

	/**
	 * Creates the index.
	 *
	 * @param resolver   the resolver
	 * @param url        the url
	 * @param actionType the action type
	 * @throws JsonProcessingException
	 */
	private void createIndex(ResourceResolver resolver, String url, ReplicationActionType actionType)
			throws JsonProcessingException {
		String payload = StringUtils.EMPTY;
		String id;

		if (url.startsWith(EsSearchConstants.ROOT_SITE)) {
			payload = createRequestPayload(url, resolver);
			id = url.substring(AEMUtils.getRootPathCountry(url).length() + 1, url.length()).replace("/", "-");
			LOGGER.info("Index Id created for indexing :" + "{}", url);
			payload = createIndexPayload(id, actionType) + System.lineSeparator() + payload;
		} else if (url.startsWith(EsSearchConstants.DAM_ROOT)) {
			payload = createAssetRequestPayload(url, resolver);
			id = url.replace(EsSearchConstants.DAM_ROOT + EsSearchConstants.FORWARDSLASH, "").replace("/", "-");
			LOGGER.info("Index Id created for indexing :" + "{}", url);
			payload = createIndexPayload(id, actionType) + System.lineSeparator() + payload;
		}

		String jcrPayload = EsSearchUtils.getJcrProperty(resolver,
				EsSearchConstants.PAYLOAD + AEMUtils.getLanguageFromPath(url),
				elasticSearchConfigurationImpl.getEsConfigPagePath());

		if (StringUtils.isNotBlank(jcrPayload)) {
			payload = jcrPayload + System.lineSeparator() + payload;
			EsSearchUtils.setJcrProperty(resolver, payload,
					EsSearchConstants.PAYLOAD + AEMUtils.getLanguageFromPath(url),
					elasticSearchConfigurationImpl.getEsConfigPagePath());
		} else {
			EsSearchUtils.setJcrProperty(resolver, payload,
					EsSearchConstants.PAYLOAD + AEMUtils.getLanguageFromPath(url),
					elasticSearchConfigurationImpl.getEsConfigPagePath());
		}
	}

	/**
	 * Creates the index payload.
	 *
	 * @param id         the id
	 * @param actionType the action type
	 * @return the string
	 * @throws JsonProcessingException
	 */
	private String createIndexPayload(String id, ReplicationActionType actionType) throws JsonProcessingException {
		Map<String, Object> indexMap = new HashMap<>();
		ObjectMapper objectMapper = new ObjectMapper();
		IndexBean bean = new IndexBean();
		bean.setid(id);
		if (actionType == ReplicationActionType.ACTIVATE) {
			indexMap.put(EsSearchConstants.INDEX, bean);
		} else if (actionType == ReplicationActionType.DEACTIVATE) {
			indexMap.put(EsSearchConstants.DELETE, bean);
		}
		return objectMapper.writeValueAsString(indexMap);

	}

	/**
	 * Delete index.
	 *
	 * @param resolver   the resolver
	 * @param url        the url
	 * @param actionType the action type
	 * @throws JsonProcessingException
	 */
	private void deleteIndex(ResourceResolver resolver, String url, ReplicationActionType actionType)
			throws JsonProcessingException {
		String id = null;
		if (url.startsWith(EsSearchConstants.ROOT_SITE)) {
			id = url.substring(AEMUtils.getRootPathCountry(url).length() + 1, url.length()).replace("/", "-");
			LOGGER.info("Index Id created for deleting :" + "{}", url);
		} else if (url.startsWith(EsSearchConstants.DAM_ROOT)) {
			id = url.replace(EsSearchConstants.DAM_ROOT + EsSearchConstants.FORWARDSLASH, "").replace("/", "-");
			LOGGER.info("Index Id created for deleting :" + "{}", url);
		}
		String payload = createIndexPayload(id, actionType);

		String jcrPayload = EsSearchUtils.getJcrProperty(resolver,
				EsSearchConstants.PAYLOAD + AEMUtils.getLanguageFromPath(url),
				elasticSearchConfigurationImpl.getEsConfigPagePath());

		if (StringUtils.isNotBlank(jcrPayload)) {
			payload = jcrPayload + System.lineSeparator() + payload;
			EsSearchUtils.setJcrProperty(resolver, payload,
					EsSearchConstants.PAYLOAD + AEMUtils.getLanguageFromPath(url),
					elasticSearchConfigurationImpl.getEsConfigPagePath());
		} else {
			EsSearchUtils.setJcrProperty(resolver, payload,
					EsSearchConstants.PAYLOAD + AEMUtils.getLanguageFromPath(url),
					elasticSearchConfigurationImpl.getEsConfigPagePath());
		}

	}

	/**
	 * Creates the request payload.
	 *
	 * @param path     the path
	 * @param resolver the resolver
	 * @return the string
	 */
	public String createRequestPayload(String path, ResourceResolver resolver) {
		final Resource resource = resolver.getResource(path);
		String json = null;
		Page page = null;
		if (Objects.nonNull(resource)) {
			final PageManager pageMgr = resolver.adaptTo(PageManager.class);
			page = Objects.nonNull(pageMgr) ? pageMgr.getContainingPage(resource) : page;

			if (Objects.nonNull(page)) {
				List<String> facetsList = new ArrayList<>();
				ObjectMapper objectMapper = new ObjectMapper();

				Map<String, Object> req = new HashMap<>();

				List<String> compMapping = getComponentMappingList(resolver, EsSearchConstants.COMPONENT_MAPPING_LIST);
				List<Resource> resourceList = EsSearchUtils.getAllIndexedResources(resolver,
						resource.getChild(JcrConstants.JCR_CONTENT), compMapping);

				for (Resource res : resourceList) {
					final ValueMap vMap = res.getValueMap();
					iterateValueMap(req, res, vMap, compMapping);
					getMultifieldProps(res, req, compMapping);
				}

				setESFieldsWthProps(resolver, page, facetsList, req);

				try {
					json = objectMapper.writeValueAsString(req);
				} catch (JsonProcessingException e) {
					LOGGER.error("JsonProcessingException  in createRequestPayload()", e);
				}
			}
		}
		return json;
	}

	/**
	 * Sets the payload fields.
	 *
	 * @param resolver   the resolver
	 * @param page       the page
	 * @param facetsList the facets list
	 * @param req        the req
	 * @param resource
	 */
	private void setESFieldsWthProps(ResourceResolver resolver, Page page, List<String> facetsList,
			Map<String, Object> req) {
		ValueMap pageProperties = page.getProperties();
		Set<String> facets = EsSearchUtils.getFacets(resolver, elasticSearchConfigurationImpl.getEsConfigPagePath());
		List<String> pagePropMapping = getComponentMappingList(resolver, EsSearchConstants.PAGE_MAPPING_LIST);
		List<SearchBean> searchBeans = EsSearchUtils.getSearchBeanList(pagePropMapping);
		for (SearchBean searchBean : searchBeans) {

			req.put(searchBean.getEsKey(), pageProperties.get(searchBean.getPropName(), StringUtils.EMPTY));
		}
		req.put(IndexingConstants.TYPE, EsSearchConstants.PAGE);
		req.put(IndexingConstants.SITE, EsSearchConstants.SITE);
		req.put(IndexingConstants.SOURCE, EsSearchConstants.SOURCE);
		req.put(IndexingConstants.PAGE_NAME, page.getPath().substring(page.getPath().lastIndexOf('/') + 1));
		req.put(IndexingConstants.PATH, page.getPath().replace(EsSearchConstants.ROOT_SITE, StringUtils.EMPTY));
		req.put(IndexingConstants.FACETS_TAGS, facets);
		LOGGER.info("payload created in setESFieldsWthProps");
	}

	/**
	 * Iterate value map.
	 *
	 * @param req         the req
	 * @param res         the res
	 * @param vMap        the v map
	 * @param compMapping the comp mapping
	 */
	private void iterateValueMap(Map<String, Object> req, Resource res, final ValueMap vMap, List<String> compMapping) {
		for (Entry<String, Object> valueMapEntry : vMap.entrySet()) {
			if (valueMapEntry.getKey().startsWith(EsSearchConstants.TRANSLATE_PROPERTY_PREFIX)) {
				setRequestMap(valueMapEntry, vMap, res, req, compMapping);
			}

		}
	}

	/**
	 * Sets the request map.
	 *
	 * @param valueMapEntry the e
	 * @param vMap          the v map
	 * @param res           the res
	 * @param req           the req
	 * @param compMapping   the comp mapping
	 */
	private void setRequestMap(Entry<String, Object> valueMapEntry, ValueMap vMap, Resource res,
			Map<String, Object> req, List<String> compMapping) {
		int count = 0;
		List<SearchBean> beanList = EsSearchUtils.getAemPropNEsProp(compMapping).get(res.getResourceType());
		if (Objects.nonNull(beanList)) {
			for (SearchBean val : beanList) {
				if (valueMapEntry.getKey().equalsIgnoreCase(val.getPropName())) {
					count++;
					String prop = vMap.get(val.getPropName(), StringUtils.EMPTY);
					setComponentProp(req, val.getEsKey(), prop);
				}
			}
			if (count < 1) {
				String prop = valueMapEntry.getValue().toString();
				setComponentProp(req, IndexingConstants.ALL_TEXT, prop);
			}
		} else {
			String prop = valueMapEntry.getValue().toString();
			setComponentProp(req, IndexingConstants.ALL_TEXT, prop);
		}
	}

	/**
	 * Sets the prop.
	 *
	 * @param req  the req
	 * @param key  the key
	 * @param prop the prop
	 */
	private void setComponentProp(Map<String, Object> req, String key, String prop) {
		if (StringUtils.isNotBlank(prop)) {
			if (req.get(key) == null) {
				req.put(key, prop);
			} else {
				prop = req.get(key) + " " + prop;
				req.replace(key, prop);
			}
		}
	}

	/**
	 * Gets the component mapping list.
	 *
	 * @param resolver       the resolver
	 * @param mulitfieldName the mulitfield name
	 * @return the component mapping list
	 */
	private List<String> getComponentMappingList(ResourceResolver resolver, String mulitfieldName) {
		final Resource searchResource = resolver.getResource(elasticSearchConfigurationImpl.getEsConfigPagePath());
		final PageManager pageMgr = resolver.adaptTo(PageManager.class);
		Page searchPage = Objects.nonNull(pageMgr) ? pageMgr.getContainingPage(searchResource) : null;
		final List<String> mappingList = new ArrayList<>();
		if (Objects.nonNull(searchPage)) {
			Resource esSearchResource = AEMUtils.getResourceOfType(searchPage.getContentResource(),
					EsSearchConstants.ES_RESOURCE_TYPE);
			if (Objects.nonNull(esSearchResource)) {
				final Iterator<Resource> itr = esSearchResource.listChildren();
				while (itr.hasNext()) {
					final Resource res = itr.next();
					addMappings(mulitfieldName, mappingList, res);
				}

			}
		}

		return mappingList;

	}

	/**
	 * Adds the mappings.
	 *
	 * @param mulitfieldName the mulitfield name
	 * @param mappingList    the mapping list
	 * @param res            the res
	 */
	private void addMappings(String mulitfieldName, final List<String> mappingList, final Resource res) {
		if (Objects.nonNull(res) && res.getName().equalsIgnoreCase(mulitfieldName)) {
			final Iterator<Resource> itr = res.listChildren();
			while (itr.hasNext()) {
				final Resource itemRes = itr.next();
				if (Objects.nonNull(itemRes)) {
					final ValueMap vMap = itemRes.getValueMap();

					mappingList.add(vMap.get(EsSearchConstants.MAPPING_PROP, StringUtils.EMPTY));
				}
			}
		}
	}

	/**
	 * Gets the multifield props.
	 *
	 * @param resource    the resource
	 * @param req         the req
	 * @param compMapping the comp mapping
	 * @return the multifield props
	 */
	private void getMultifieldProps(Resource resource, Map<String, Object> req, List<String> compMapping) {
		if (Objects.nonNull(resource)) {
			final Iterator<Resource> itr = resource.listChildren();
			while (itr.hasNext()) {
				final Resource res = itr.next();
				setMultifieldProps(req, compMapping, res);
			}
		}
	}

	/**
	 * Sets the multifield props.
	 *
	 * @param req         the req
	 * @param compMapping the comp mapping
	 * @param res         the res
	 */
	private void setMultifieldProps(Map<String, Object> req, List<String> compMapping, final Resource res) {
		if (Objects.nonNull(res) && res.getName().startsWith(EsSearchConstants.TRANSLATE_MULTIFIELD_PREFIX)) {
			final Iterator<Resource> itr1 = res.listChildren();
			while (itr1.hasNext()) {
				final Resource itemRes = itr1.next();
				if (Objects.nonNull(itemRes)) {
					final ValueMap vMap = itemRes.getValueMap();

					iterateValueMap(req, res, vMap, compMapping);
				}
			}
		}
	}

	/**
	 * Creates the asset request payload.
	 *
	 * @param url      the url
	 * @param resolver the resolver
	 * @return the string
	 */
	private String createAssetRequestPayload(String url, ResourceResolver resolver) {
		Map<String, Object> req = new HashMap<>();
		String json = null;
		ObjectMapper objectMapper = new ObjectMapper();
		AssetManager assetManager = resolver.adaptTo(AssetManager.class);
		Resource resource = resolver.getResource(url);
		if (StringUtils.isNotBlank(url) && Objects.nonNull(assetManager)) {
			Asset asset = resource.adaptTo(Asset.class);
			if (Objects.nonNull(asset)) {
				setESFieldsForAsset(url, resolver, req, asset);
			}

		}
		try {

			json = objectMapper.writeValueAsString(req);
		} catch (JsonProcessingException e) {
			LOGGER.error("JsonProcessingException in createAssetRequestPayload() ", e);
		}
		return json;
	}

	/**
	 * Sets the ES fields for asset.
	 *
	 * @param url      the url
	 * @param resolver the resolver
	 * @param req      the req
	 * @param asset    the asset
	 */
	private void setESFieldsForAsset(String url, ResourceResolver resolver, Map<String, Object> req, Asset asset) {
		List<String> assetPropMapping = getComponentMappingList(resolver, EsSearchConstants.ASSET_MAPPING_LIST);
		List<SearchBean> searchBeans = EsSearchUtils.getSearchBeanList(assetPropMapping);
		for (SearchBean searchBean : searchBeans) {
			if (searchBean.getEsKey().contains(IndexingConstants.TAGS)) {

				req.put(searchBean.getEsKey(), asset.getMetadata(searchBean.getPropName()));
			} else {
				req.put(searchBean.getEsKey(),
						Objects.nonNull(asset.getMetadataValue(searchBean.getPropName()))
								? asset.getMetadataValue(searchBean.getPropName())
								: asset.getName());
			}
		}
		req.put(IndexingConstants.TYPE, EsSearchConstants.ASSET);
		req.put(IndexingConstants.PATH, url);
		req.put(IndexingConstants.SITE, EsSearchConstants.SITE);
		req.put(IndexingConstants.SOURCE, EsSearchConstants.SOURCE);
		LOGGER.info("payload created in setESFieldsForAsset");
	}

}

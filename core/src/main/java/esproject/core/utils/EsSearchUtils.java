package esproject.core.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.wcm.api.Page;

import esproject.core.beans.SearchBean;
import esproject.core.constants.EsSearchConstants;

/**
 * The Class EsSearchUtils.
 */
public class EsSearchUtils {

    /** Logger Instantiation. */
    private static final Logger LOGGER = LoggerFactory.getLogger(EsSearchUtils.class);

    /**
     * Instantiates a new utils.
     */
    private EsSearchUtils() {
    }

    /**
     * Gets the facets.
     *
     * @param resolver
     *            the resolver
     * @param esConfigPagePath
     * @return the facets
     */
    public static Set<String> getFacets(ResourceResolver resolver, String esConfigPagePath) {
        LOGGER.info("Inside get facets");
        Set<String> facets = new HashSet<>();
        final Resource resource = resolver.getResource(esConfigPagePath);
        Page page = AEMUtils.getPageFromResource(resource);
        Resource esSearchResource = AEMUtils.getResourceOfType(page.getContentResource(),
                EsSearchConstants.ES_RESOURCE_TYPE);
        String facetString = esSearchResource.getValueMap().get(EsSearchConstants.FACETS, StringUtils.EMPTY);
        for (String item : facetString.split(",")) {
            facets.add(item);
        }
        LOGGER.debug("facets {}", facets);
        return facets;
    }

    /**
     * Gets the all indexed resources.
     *
     * @param resolver
     *            the resolver
     * @param rootResource
     *            the root resource
     * @param esConfig
     *            the es config
     * @return the all indexed resources
     */
    public static List<Resource> getAllIndexedResources(ResourceResolver resolver, Resource rootResource,
            List<String> esConfig) {
        List<Resource> list = new ArrayList<>();
        if (null == rootResource || ResourceUtil.isNonExistingResource(rootResource)) {
            return list;
        }

        if (!JcrConstants.JCR_CONTENT.equals(rootResource.getName())) {
            return list;
        }
        AEMUtils.getAllPageComponents(resolver, rootResource, list);
        List<Resource> esResourceList = new ArrayList<>();
        for (Resource res : list) {
            esResourceList.add(res);

        }

        return esResourceList;
    }

    /**
     * Gets the aem prop N es prop.
     *
     * @param esConfig
     *            the es config
     * @return the aem prop N es prop
     */
    public static Map<String, List<SearchBean>> getAemPropNEsProp(List<String> esConfig) {
        Map<String, List<SearchBean>> compPropNEsProp = new HashMap<>();
        for (String config : esConfig) {
            String[] resConfig = config.split(":");

            SearchBean searchBean = new SearchBean(resConfig[1], resConfig[2]);

            if (compPropNEsProp.get(resConfig[0]) == null) {
                List<SearchBean> esBeanList = new ArrayList<>();
                esBeanList.add(searchBean);
                compPropNEsProp.put(resConfig[0], esBeanList);
            } else {
                List<SearchBean> esBeanList = compPropNEsProp.get(resConfig[0]);
                esBeanList.add(searchBean);
                compPropNEsProp.put(resConfig[0], esBeanList);
            }
        }
        return compPropNEsProp;
    }

    /**
     * Gets the search bean list.
     *
     * @param pgaePropMapping
     *            the pgae prop mapping
     * @return the search bean list
     */
    public static List<SearchBean> getSearchBeanList(List<String> pgaePropMapping) {
        List<SearchBean> searchBeans = new ArrayList<>();
        for (String mapping : pgaePropMapping) {
            String[] mappingArray = mapping.split(",");
            SearchBean searchBean = new SearchBean(mappingArray[0], mappingArray[1]);
            searchBeans.add(searchBean);
        }
        return searchBeans;
    }

    /**
     * Gets the tag title.
     *
     * @param tags
     *            the tags
     * @return the tag title
     */
    public static String[] getTagTitle(String[] tags) {
        if (Objects.nonNull(tags) && ArrayUtils.isNotEmpty(tags)) {
            final String[] tagArray = new String[tags.length];
            for (int i = 0; i < tags.length; i++) {
                tagArray[i] = tags[i].substring(tags[i].lastIndexOf('/') + 1);
            }
            return tagArray;
        }
        return new String[0];
    }

    /**
     * Checks if is page no index.
     *
     * @param resolver
     *            the resolver
     * @param path
     *            the path
     * @return true, if is page no index
     */
    public static boolean isPageNoIndex(ResourceResolver resolver, String path) {
        final Resource resource = resolver.getResource(path);
        Page page = AEMUtils.getPageFromResource(resource);
        if (Objects.nonNull(page)) {
            ValueMap pageProperties = page.getProperties();
            return pageProperties.get(EsSearchConstants.NO_INDEX_PROP, StringUtils.EMPTY)
                    .equalsIgnoreCase(EsSearchConstants.NO_INDEX);
        }
        return false;
    }

    /**
     * Sets the jcr property.
     *
     * @param resolver
     *            the resolver
     * @param jcrPayload
     *            the jcr payload
     * @param property
     *            the property
     * @param pagePath
     */
    public static void setJcrProperty(ResourceResolver resolver, String jcrPayload, String property, String pagePath) {
        Resource pageJcrResource = resolver
                .getResource(pagePath + EsSearchConstants.FORWARDSLASH + JcrConstants.JCR_CONTENT);
        if (Objects.nonNull(pageJcrResource)) {
            Node node = pageJcrResource.adaptTo(Node.class);
            try {
                node.setProperty(property, String.valueOf(jcrPayload));
                node.getSession().save();
            } catch (RepositoryException e) {
                LOGGER.error("RepositoryException while saving property in node ", e);
            }
        }
    }

    /**
     * Gets the jcr property.
     *
     * @param resolver
     *            the resolver
     * @param property
     *            the property
     * @param pagePath
     * @return the jcr property
     */
    public static String getJcrProperty(ResourceResolver resolver, String property, String pagePath) {
        Resource pageJcrResource = resolver
                .getResource(pagePath + EsSearchConstants.FORWARDSLASH + JcrConstants.JCR_CONTENT);
        return Objects.nonNull(pageJcrResource) ? pageJcrResource.getValueMap().get(property, StringUtils.EMPTY)
                : StringUtils.EMPTY;
    }
    
   
}

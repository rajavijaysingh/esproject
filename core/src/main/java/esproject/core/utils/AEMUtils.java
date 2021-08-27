package esproject.core.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.vault.util.Text;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;

import esproject.core.constants.EsSearchConstants;

/**
 * This is a generic utility class required for AEM implementation..
 * 
 *
 */
public final class AEMUtils {

	/** Logger Instantiation. */
	private static final Logger LOGGER = LoggerFactory.getLogger(AEMUtils.class);

	/**
	 * Instantiates a new AEM utils.
	 */
	private AEMUtils() {
	}

	/**
	 * Gets the language from path.
	 *
	 * @param pagePath the page path
	 * @return the language from path
	 */
	public static String getLanguageFromPath(String pagePath) {
		LOGGER.debug("AEMUtils getLanguageFromPath() pagePath:- {}", pagePath);
		final String regionalPagePath = getRootPathLanguage(pagePath);
		final String language = StringUtils.substringAfterLast(regionalPagePath, EsSearchConstants.FORWARDSLASH);
		LOGGER.debug("AEMUtils getLanguageFromPath() language:- {}", language);
		return language;
	}

	/**
	 * Gets the root path language.
	 *
	 * @param resStr the res str
	 * @return the root path language
	 */
	public static String getRootPathLanguage(String resStr) {
		if (resStr.startsWith(EsSearchConstants.DAM_ROOT_DOCUMENTS)) {
			return Text.getAbsoluteParent(resStr, 4);
		} else if (resStr.startsWith(EsSearchConstants.DAM_ROOT)) {
			return EsSearchConstants.FORWARDSLASH + EsSearchConstants.DEFAULT_LANGUAGE;
		} else if (resStr.startsWith(EsSearchConstants.EXP_FRAGMENT_ROOT)) {
			return Text.getAbsoluteParent(resStr, EsSearchConstants.XF_LANGUAGE_LEVEL);
		} else {
			return Text.getAbsoluteParent(resStr, 3);
		}
	}

	/**
	 * Gets the page from resource.
	 *
	 * @param pageResource the page resource
	 * @return the page from resource
	 */
	public static Page getPageFromResource(Resource pageResource) {
		LOGGER.debug("Inside getPageFromResource() method");
		Page page = null;

		if (Objects.nonNull(pageResource)) {
			final ResourceResolver resourceResolver = pageResource.getResourceResolver();
			final PageManager pageMgr = resourceResolver.adaptTo(PageManager.class);

			if (Objects.nonNull(pageMgr)) {
				page = pageMgr.getContainingPage(pageResource);
			}
		}
		LOGGER.debug("Exit getPageFromResource() method :: {}", page);
		return page;
	}

	/**
	 * Gets the all page components.
	 *
	 * @param resolver     the resolver
	 * @param rootResource the root resource
	 * @param resourceList the resource list
	 * @return the all page components
	 */
	public static List<Resource> getAllPageComponents(ResourceResolver resolver, Resource rootResource,
			List<Resource> resourceList) {
		LOGGER.debug("Inside  getAllResourcesUnderPage() :: rootResource:{}", rootResource);
		if (Objects.nonNull(rootResource)
				&& rootResource.getResourceType().contains(EsSearchConstants.COMPONENT_PATH)) {
			resourceList.add(rootResource);
		} else if (Objects.nonNull(rootResource)
				&& rootResource.getResourceType().contains("cq/experience-fragments")) {
			getAllXFComponents(resolver,
					rootResource.getValueMap().get("fragmentPath", StringUtils.EMPTY) + "/jcr:content", resourceList);
		}
		if (Objects.nonNull(rootResource) && rootResource.hasChildren()) {
			final Iterable<Resource> resourceIterable = rootResource.getChildren();
			for (final Resource res : resourceIterable) {
				getAllPageComponents(resolver, res, resourceList);
			}
		}

		LOGGER.debug("Exit  getAllPageComponents()");
		return resourceList;
	}

	/**
	 * Gets the all XF components.
	 *
	 * @param resolver     the resolver
	 * @param path         the path
	 * @param resourceList the resource list
	 * @return the all XF components
	 */
	public static void getAllXFComponents(ResourceResolver resolver, String path, List<Resource> resourceList) {
		LOGGER.debug("Inside  getAllXFComponents() :: rootResource:{}", path);
		final Resource rootResource = resolver.getResource(path);
		if (null == rootResource || ResourceUtil.isNonExistingResource(rootResource)) {
			LOGGER.debug("getAllResourcesUnderPage() Resource does not exist :: {}", rootResource);
			return;
		}

		if (!JcrConstants.JCR_CONTENT.equals(rootResource.getName())) {
			LOGGER.debug("getAllResourcesUnderPage() Resource exists :: {}", rootResource);
			return;
		}
		getAllPageComponents(resolver, rootResource, resourceList);
		LOGGER.debug("Exit  getAllXFComponents()");
	}

	/**
	 * Gets the resource of type.
	 *
	 * @param rootResource the root resource
	 * @param resourceType the resource type
	 * @return the resource of type
	 */
	public static Resource getResourceOfType(Resource rootResource, String resourceType) {
		LOGGER.debug("Inside AEMUtils getResourceOfType() rootResource :{}, resourceType :{}", rootResource,
				resourceType);
		if (Objects.nonNull(rootResource) && !JcrConstants.JCR_CONTENT.equals(rootResource.getName())) {
			return null;
		}
		final List<Resource> list = new ArrayList<>();
		getAllResourcesUnderPage(rootResource, resourceType, list, true);
		LOGGER.debug("Exit AEMUtils getResourceOfType()");
		return list.isEmpty() ? null : list.get(0);
	}

	/**
	 * Gets the all resources under page.
	 *
	 * @param rootResource the root resource
	 * @param resourceType the resource type
	 * @param list         the list
	 * @param breakAtFirst the break at first
	 * @return the all resources under page
	 */
	private static void getAllResourcesUnderPage(Resource rootResource, String resourceType, List<Resource> list,
			boolean breakAtFirst) {
		LOGGER.debug("Inside  getAllResourcesUnderPage() :: rootResource:{} resourceType:{}", rootResource,
				resourceType);
		if (Objects.nonNull(rootResource) && rootResource.isResourceType(resourceType)) {
			list.add(rootResource);
		}

		if (breakAtFirst && !list.isEmpty()) {
			return;
		}

		if (Objects.nonNull(rootResource) && rootResource.hasChildren()) {
			final Iterable<Resource> resourceIterable = rootResource.getChildren();
			for (final Resource res : resourceIterable) {
				getAllResourcesUnderPage(res, resourceType, list, breakAtFirst);
			}
		}
		LOGGER.debug("Exit  getAllResourcesUnderPage() method");
	}

	/**
	 * Gets the root path country.
	 *
	 * @param resStr the res str
	 * @return the root path country
	 */
	public static String getRootPathCountry(String resStr) {
		if (resStr.startsWith(EsSearchConstants.DAM_ROOT)) {
			return resStr;
		}
		return Text.getAbsoluteParent(resStr, EsSearchConstants.COUNTRY_LEVEL);
	}
}

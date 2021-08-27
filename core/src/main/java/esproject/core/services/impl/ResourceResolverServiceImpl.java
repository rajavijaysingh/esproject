package esproject.core.services.impl;

import java.util.HashMap;

import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import esproject.core.constants.EsSearchConstants;
import esproject.core.services.ResourceResolverService;

/**
 * The Class ResourceResolverServiceImpl.
 */
@Component(immediate = true, service = ResourceResolverService.class)
public class ResourceResolverServiceImpl implements ResourceResolverService {

    /** The resource resolver factory. */
    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    /** The logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceResolverServiceImpl.class);

    /**
     * Gets the system resource resolver.
     *
     * @return the system resource resolver
     */
    @Override
    public ResourceResolver getSystemResourceResolver() {
        final HashMap<String, Object> param = new HashMap<>();
        param.put(ResourceResolverFactory.SUBSERVICE, EsSearchConstants.SERVICE_USER);

        if (resourceResolverFactory != null) {
            try {
                return resourceResolverFactory.getServiceResourceResolver(param);
            } catch (LoginException loginException) {
                LOGGER.error("Not Authorized :", loginException);
            }
        }
        return null;
    }

}


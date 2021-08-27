package esproject.core.services;

import org.apache.sling.api.resource.ResourceResolver;

/**
 * The Interface ResourceResolverService.
 */
public interface ResourceResolverService {

    /**
     * Gets the system resource resolver.
     *
     * @return the system resource resolver
     */
    ResourceResolver getSystemResourceResolver();
    
}

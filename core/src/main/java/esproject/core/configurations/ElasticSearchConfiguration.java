package esproject.core.configurations;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/**
 * Elastic Search OSGI configuration file for project. ElasticSearchConfiguration class only for OSGI config values.
 *
 * @author yograna
 *
 */
@ObjectClassDefinition(name = "Elastic Search Configuration", description = "Elastic Search OSGI Configuration")
public @interface ElasticSearchConfiguration {

    
    @AttributeDefinition(
            name = "Scheduling Time",
            description = "Scheduling Time for Job"
                    + "For example: 2")
    int getSchedulingTime() default 2;
    
    
    @AttributeDefinition(
            name = "Elastic Search Url",
            description = "Elastic Search Url"
                    + "For example: https://search.es.url.com/")
    String getESUrl() default "https://search.es.url.com/";
    
    @AttributeDefinition(
            name = "Elastic Search Config Page Path",
            description = "Elastic Search Config Page Path"
                    + "For example: content/esproject/us/en/elastic-search-configuration")
    String getESConfigPagePath() default "content/esproject/us/en/elastic-search-configuration";
    
    @AttributeDefinition(
            name = "Bulk Index API",
            description = "Bulk Index API"
                    + "For example: /_bulk")
    String getBulkIndexAPI() default "/_bulk";
    
}


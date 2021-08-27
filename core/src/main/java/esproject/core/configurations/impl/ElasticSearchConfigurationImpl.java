package esproject.core.configurations.impl;

import java.io.Serializable;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.metatype.annotations.Designate;

import esproject.core.configurations.ElasticSearchConfiguration;

/**
 * Content OSGI configuration file for project. ElasticSearchConfigurationImpl class only for OSGI config values.
 *
 * @author yograna
 *
 */
@Component(
        service = ElasticSearchConfigurationImpl.class,
        immediate = true,
        configurationPolicy = ConfigurationPolicy.REQUIRE)
@Designate(ocd = ElasticSearchConfiguration.class)
public class ElasticSearchConfigurationImpl implements Serializable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1905122041950251207L;

    /** The scheduling time. */
    private int schedulingTime;

    /** The e S url. */
    private String esUrl;

    /** The es config page path. */
    private String esConfigPagePath;

    /** The bulk index API. */
    private String bulkIndexAPI;

    /**
     * Activate.
     *
     * @param configuration
     *            the configuration
     */
    @Activate
    @Modified
    public void activate(final ElasticSearchConfiguration configuration) {

        schedulingTime = configuration.getSchedulingTime();
        esUrl = configuration.getESUrl();
        esConfigPagePath = configuration.getESConfigPagePath();
        bulkIndexAPI = configuration.getBulkIndexAPI();

    }

    /**
     * Gets the scheduling time.
     *
     * @return the scheduling time
     */
    public int getSchedulingTime() {
        return schedulingTime;
    }

    /**
     * Gets the e S url.
     *
     * @return the e S url
     */
    public String getESUrl() {
        return esUrl;
    }

    /**
     * Gets the es config page path.
     *
     * @return the es config page path
     */
    public String getEsConfigPagePath() {
        return esConfigPagePath;
    }

    /**
     * Gets the bulk index API.
     *
     * @return the bulk index API
     */
    public String getBulkIndexAPI() {
        return bulkIndexAPI;
    }

}

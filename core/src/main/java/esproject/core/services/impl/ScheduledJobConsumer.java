package esproject.core.services.impl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Iterator;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.JobManager;
import org.apache.sling.event.jobs.ScheduledJobInfo;
import org.apache.sling.event.jobs.consumer.JobConsumer;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.propertytypes.ServiceDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.net.HttpHeaders;

import esproject.core.configurations.impl.ElasticSearchConfigurationImpl;
import esproject.core.configurations.impl.SecretConfigurationImpl;
import esproject.core.constants.EsSearchConstants;
import esproject.core.utils.EsSearchUtils;
import esproject.core.services.ResourceResolverService;

/**
 * The Class ScheduledJobConsumer.
 */
@Component(
        service = JobConsumer.class,
        immediate = true,
        property = { JobConsumer.PROPERTY_TOPICS + "=" + "search/scheduled/job" })
@ServiceDescription("Queue and Schedule build jobs while replication")
public class ScheduledJobConsumer implements JobConsumer {

    /** The Constant LOGGER. */
    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledJobConsumer.class);

    /** The resource resolver service. */
    @Reference
    private ResourceResolverService resourceResolverService;

    /** The job manager. */
    @Reference
    private JobManager jobManager;

    /** The resource resolver factory. */
    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    /** The elastic search configuration impl. */
    @Reference
    ElasticSearchConfigurationImpl elasticSearchConfigurationImpl;

    /** The secret configuration impl. */
    @Reference
    SecretConfigurationImpl secretConfigurationImpl;

    /**
     * Process.
     *
     * @param job
     *            the job
     * @return the job result
     */
    @Override
    public JobResult process(Job job) {
        LOGGER.info("Processing the JOB *******");
        final ResourceResolver resolver = resourceResolverService.getSystemResourceResolver();

        String payload = EsSearchUtils.getJcrProperty(resolver, EsSearchConstants.PAYLOAD_EN,
                elasticSearchConfigurationImpl.getEsConfigPagePath()) + System.lineSeparator();
        String urlString = EsSearchUtils.getJcrProperty(resolver, EsSearchConstants.ES_URL_EN,
                elasticSearchConfigurationImpl.getEsConfigPagePath());
        if (StringUtils.isNotBlank(urlString) && StringUtils.isNotBlank(payload)) {
            LOGGER.info("Indexing documents at {}", urlString);
            pushDocumentsToES(payload, urlString, resolver);
        }
        unScheduleJob();
        return JobResult.OK;
    }

    /**
     * Push documents to ES.
     *
     * @param payload
     *            the payload
     * @param urlString
     *            the url string
     * @param resolver
     */
    private void pushDocumentsToES(String payload, String urlString, ResourceResolver resolver) {
        final HttpPost req = new HttpPost(urlString);

        String authHeader = "Basic " + createAuthentication();
        StringEntity entity = new StringEntity(payload, StandardCharsets.UTF_8);
        req.setEntity(entity);
        req.setHeader("Content-Type", "application/json");
        req.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
        final HttpResponse res = sendRequest(req);
        LOGGER.info("ES Indexing Response is {}", res);
        if (res != null) {
            final int statusCode = res.getStatusLine().getStatusCode();
            LOGGER.info("Create Index Response Status {}", statusCode);
            if (res.getStatusLine().getStatusCode() == 200 || res.getStatusLine().getStatusCode() == 201) {
                EsSearchUtils.setJcrProperty(resolver, "", EsSearchConstants.PAYLOAD_EN,
                        elasticSearchConfigurationImpl.getEsConfigPagePath());
            }
        }
    }

    /**
     * Creates the authentication.
     *
     * @return the string
     */
    private String createAuthentication() {
        String auth = secretConfigurationImpl.getEsUser() + ":" + secretConfigurationImpl.getEsPassword();
        LOGGER.info("ES user is  {}", secretConfigurationImpl.getEsUser());
        String encodedAuth = null;
        encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        return encodedAuth;
    }

    /**
     * Send request.
     *
     * @param <T>
     *            the generic type
     * @param request
     *            the request
     * @return the http response
     */
    public static <T extends HttpRequestBase> HttpResponse sendRequest(final T request) {

        RequestConfig.Builder requestBuilder = RequestConfig.custom();
        requestBuilder.setConnectTimeout(5000);
        requestBuilder.setConnectionRequestTimeout(5000);
        requestBuilder.setSocketTimeout(5000);

        HttpClientBuilder builder = HttpClientBuilder.create();
        builder.setDefaultRequestConfig(requestBuilder.build());

        final HttpClient client = builder.build();
        HttpResponse response = null;

        try {
            response = client.execute(request);
        } catch (IOException e) {
            LOGGER.error("Exception in sendRequest() is ", e);
        }

        return response;
    }

    /**
     * Un schedule job.
     */
    private void unScheduleJob() {
        if (Objects.nonNull(jobManager.getScheduledJobs()) && !jobManager.getScheduledJobs().isEmpty()) {
            Iterator<ScheduledJobInfo> iter = jobManager.getScheduledJobs().iterator();
            while (iter.hasNext()) {
                ScheduledJobInfo scheduledJobInfo = iter.next();
                if (scheduledJobInfo.getJobTopic().equalsIgnoreCase(EsSearchConstants.SCHEDULED_JOB_TOPIC)) {
                    scheduledJobInfo.unschedule();
                }
            }
        }
    }
}

package esproject.core.listeners;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import org.apache.sling.event.jobs.JobBuilder.ScheduleBuilder;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.event.jobs.JobManager;
import org.apache.sling.event.jobs.ScheduledJobInfo;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.propertytypes.ServiceDescription;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;

import org.osgi.service.event.EventHandler;

import org.slf4j.Logger;

import org.slf4j.LoggerFactory;

import com.day.cq.replication.ReplicationAction;
import com.day.cq.replication.ReplicationActionType;

import esproject.core.configurations.impl.ElasticSearchConfigurationImpl;
import esproject.core.constants.EsSearchConstants;
import esproject.core.utils.EsSearchUtils;
import esproject.core.services.ResourceResolverService;
import esproject.core.utils.AEMUtils;

/**
 * The listener interface for receiving replicationEvent events. The class that is interested in processing a
 * replicationEvent event implements this interface, and the object created with that class is registered with a
 * component using the component's <code>addReplicationEventListener<code> method. When the replicationEvent event
 * occurs, that object's appropriate method is invoked.
 *
 * @see ReplicationEventEvent
 */
@Component(
        service = EventHandler.class,
        immediate = true,
        property = { EventConstants.EVENT_TOPIC + "=" + ReplicationAction.EVENT_TOPIC })
@ServiceDescription("Listen on changes while replication")
public class CustomReplicationListener implements EventHandler {

    /** The Constant LOGGER. */
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomReplicationListener.class);

    /** The job manager. */
    @Reference
    private JobManager jobManager;

    /** The resource resolver service. */
    @Reference
    private ResourceResolverService resourceResolverService;

    /** The resource resolver factory. */
    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    @Reference
    ElasticSearchConfigurationImpl elasticSearchConfigurationImpl;

    /**
     * Handle event.
     *
     * @param event
     *            the event
     */
    public void handleEvent(Event event) {

        LOGGER.info("Custom ReplicationEventListener");
        ReplicationAction action = ReplicationAction.fromEvent(event);
        final ResourceResolver resolver = resourceResolverService.getSystemResourceResolver();

        if (Objects.nonNull(action) && Objects.nonNull(resolver)
                && (action.getType() == ReplicationActionType.ACTIVATE
                        || action.getType() == ReplicationActionType.DEACTIVATE)
                && (action.getPath().startsWith(EsSearchConstants.ROOT_SITE)
                        || action.getPath().startsWith(EsSearchConstants.DAM_ROOT))
                && !EsSearchUtils.isPageNoIndex(resolver, action.getPath())) {

            setEsUrl(action.getPath(), resolver);
            Map<String, Object> jobProperties = new HashMap<>();
            jobProperties.put(EsSearchConstants.PATH, action.getPath());
            jobProperties.put(EsSearchConstants.ACTION_TYPE, action.getType());
            jobManager.addJob(EsSearchConstants.CREATE_PAYLOAD_JOB_TOPIC, jobProperties);

            if (jobManager.getScheduledJobs().isEmpty()) {

                scheduleJob();
            } else if (!jobManager.getScheduledJobs().isEmpty()) {

                rescheduleJob();
            }

            LOGGER.info("Replication action {} occured on {} ", action.getType().getName(), action.getPath());

        }

        LOGGER.info("ReplicationEventListener Ended");

    }

    /**
     * Schedule job.
     */
    private void scheduleJob() {
        LOGGER.info("Scheduler Job scheduled after 2 mins");
        ScheduleBuilder scheduleBuilder = jobManager.createJob(EsSearchConstants.SCHEDULED_JOB_TOPIC).schedule();
        addSchedulingTime(scheduleBuilder);
        if (scheduleBuilder.add() == null) {
            LOGGER.info("Scheduler Job for replication events failed");
        }

    }

    /**
     * Reschedule job.
     */
    private void rescheduleJob() {
        Iterator<ScheduledJobInfo> iter = jobManager.getScheduledJobs().iterator();
        while (iter.hasNext()) {
            ScheduledJobInfo scheduledJobInfo = iter.next();
            if (scheduledJobInfo.getJobTopic().equalsIgnoreCase(EsSearchConstants.SCHEDULED_JOB_TOPIC)) {
                ScheduleBuilder scheduleBuilder = scheduledJobInfo.reschedule();
                LOGGER.info("Scheduler Job Rescheduled after 2 mins");
                addSchedulingTime(scheduleBuilder);
                if (scheduleBuilder.add() == null) {
                    LOGGER.info("Scheduler Job for replication events failed");
                }
            }

        }
    }

    /**
     * Adds the scheduling time.
     *
     * @param scheduleBuilder
     *            the schedule builder
     */
    private void addSchedulingTime(ScheduleBuilder scheduleBuilder) {
        Calendar rightNow = Calendar.getInstance();
        int min = rightNow.get(Calendar.MINUTE);
        scheduleBuilder.hourly(min + elasticSearchConfigurationImpl.getSchedulingTime());
    }

    /**
     * Sets the es url.
     *
     * @param url
     *            the url
     * @param resolver
     *            the resolver
     */
    private void setEsUrl(String url, ResourceResolver resolver) {
        String language = AEMUtils.getLanguageFromPath(url);
        String urlString = elasticSearchConfigurationImpl.getESUrl() + EsSearchConstants.PROJECT_US + language
                + elasticSearchConfigurationImpl.getBulkIndexAPI();
        EsSearchUtils.setJcrProperty(resolver, urlString, EsSearchConstants.ES_URL + language,
                elasticSearchConfigurationImpl.getEsConfigPagePath());
        LOGGER.debug("ES URL added {}", urlString);

    }

}

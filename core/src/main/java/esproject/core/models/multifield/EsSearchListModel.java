package esproject.core.models.multifield;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import com.adobe.cq.export.json.ExporterConstants;

/**
 * The Class EsSearchListModel.
 */
@Model(
        adaptables = { SlingHttpServletRequest.class, Resource.class },
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
@Exporter(name = ExporterConstants.SLING_MODEL_EXPORTER_NAME, extensions = ExporterConstants.SLING_MODEL_EXTENSION)
public class EsSearchListModel {

    /** The resource. */
    @Self
    private Resource resource;

    /** The  mapping. */
    @ValueMapValue
    private String esMapping;

    /**
     * Gets the  mapping.
     *
     * @return the  mapping
     */
    public String getEsMapping() {
        return resource.getValueMap().get("esMapping", StringUtils.EMPTY);
    }

}

package esproject.core.models.impl;

import javax.inject.Inject;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Via;

import com.adobe.cq.export.json.ComponentExporter;
import com.adobe.cq.export.json.ExporterConstants;

import esproject.core.models.SampleComponentModel;

/**
 * The Class SampleComponentModelImpl.
 */
@Model(adaptables = { SlingHttpServletRequest.class,
		Resource.class }, resourceType = SampleComponentModelImpl.RESOURCE_TYPE, adapters = {
				SampleComponentModel.class,
				ComponentExporter.class }, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
@Exporter(name = ExporterConstants.SLING_MODEL_EXPORTER_NAME, extensions = ExporterConstants.SLING_MODEL_EXTENSION)
public class SampleComponentModelImpl implements SampleComponentModel {

	/** The Constant RESOURCE_TYPE. */
	static final String RESOURCE_TYPE = "esproject/components/sampleComponent";

	/** The esx RT headline. */
	@Inject
	@Via("resource")
	private String esxRTHeadline;

	/** The esx T body copy. */
	@Inject
	@Via("resource")
	private String esxTBodyCopy;

	/**
	 * Gets the esx RT headline.
	 *
	 * @return the esx RT headline
	 */
	public String getEsxRTHeadline() {
		return esxRTHeadline;
	}

	/**
	 * Gets the esx T body copy.
	 *
	 * @return the esx T body copy
	 */
	public String getEsxTBodyCopy() {
		return esxTBodyCopy;
	}

	/**
	 * Gets the exported type.
	 *
	 * @return the exported type
	 */
	@Override
	public String getExportedType() {
		return SampleComponentModelImpl.RESOURCE_TYPE;
	}

}

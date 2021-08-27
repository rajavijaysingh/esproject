package esproject.core.models.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Via;
import org.apache.sling.models.annotations.injectorspecific.Self;

import com.adobe.cq.export.json.ComponentExporter;
import com.adobe.cq.export.json.ExporterConstants;

import esproject.core.models.EsSearchModel;
import esproject.core.models.multifield.EsSearchListModel;

/**
 * The Class EsSearchModelImpl.
 */
@Model(adaptables = { SlingHttpServletRequest.class,
		Resource.class }, resourceType = EsSearchModelImpl.RESOURCE_TYPE, adapters = { EsSearchModel.class,
				ComponentExporter.class }, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
@Exporter(name = ExporterConstants.SLING_MODEL_EXPORTER_NAME, extensions = ExporterConstants.SLING_MODEL_EXTENSION)
public class EsSearchModelImpl implements EsSearchModel {

	/** The Constant RESOURCE_TYPE. */
	static final String RESOURCE_TYPE = "esproject/components/esSearch";

	/** The resource. */
	@Self
	private Resource resource;

	/** The es facets. */
	@Inject
	@Via("resource")
	private String esFacets;

	/** The esx J component mapping list. */
	@Inject
	@Via("resource")
	private List<EsSearchListModel> esxJComponentMappingList;

	/** The config type list. */
	@Inject
	@Via("resource")
	private List<EsSearchListModel> esxJPageMappingList;

	/** The esx J asset mapping list. */
	@Inject
	@Via("resource")
	private List<EsSearchListModel> esxJAssetMappingList;

	/**
	 * Gets the esx J component mapping list.
	 *
	 * @return the esx J component mapping list
	 */
	@Override
	public List<EsSearchListModel> getEsxJComponentMappingList() {
		return Objects.nonNull(esxJComponentMappingList) ? esxJComponentMappingList : new ArrayList<>();
	}

	/**
	 * Gets the es facets.
	 *
	 * @return the es facets
	 */
	@Override
	public String getEsFacets() {
		return esFacets;
	}

	/**
	 * Gets the esx J page mapping list.
	 *
	 * @return the esx J page mapping list
	 */
	@Override
	public List<EsSearchListModel> getEsxJPageMappingList() {
		return Objects.nonNull(esxJPageMappingList) ? esxJPageMappingList : new ArrayList<>();
	}

	/**
	 * Gets the esx J asset mapping list.
	 *
	 * @return the esx J asset mapping list
	 */
	@Override
	public List<EsSearchListModel> getEsxJAssetMappingList() {
		return Objects.nonNull(esxJAssetMappingList) ? esxJAssetMappingList : new ArrayList<>();
	}

	/**
	 * Gets the exported type.
	 *
	 * @return the exported type
	 */
	@Override
	public String getExportedType() {
		return EsSearchModelImpl.RESOURCE_TYPE;
	}

}

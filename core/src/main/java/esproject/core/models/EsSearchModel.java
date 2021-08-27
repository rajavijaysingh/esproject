package esproject.core.models;

import java.util.List;

import com.adobe.cq.export.json.ComponentExporter;

import esproject.core.models.multifield.EsSearchListModel;

/**
 * The Interface EsSearchModel.
 */
public interface EsSearchModel extends ComponentExporter {

	/**
	 * Gets the es facets.
	 *
	 * @return the es facets
	 */
	String getEsFacets();

	/**
	 * Gets the esx J component mapping list.
	 *
	 * @return the esx J component mapping list
	 */
	public List<EsSearchListModel> getEsxJComponentMappingList();

	/**
	 * Gets the esx J page mapping list.
	 *
	 * @return the esx J page mapping list
	 */
	public List<EsSearchListModel> getEsxJPageMappingList();

	/**
	 * Gets the esx J asset mapping list.
	 *
	 * @return the esx J asset mapping list
	 */
	public List<EsSearchListModel> getEsxJAssetMappingList();

}

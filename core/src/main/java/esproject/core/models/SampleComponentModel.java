package esproject.core.models;

import com.adobe.cq.export.json.ComponentExporter;

/**
 * The Interface SampleComponentModel.
 */
public interface SampleComponentModel extends ComponentExporter {

	/**
	 * Gets the esx RT headline.
	 *
	 * @return the esx RT headline
	 */
	String getEsxRTHeadline();

	/**
	 * Gets the esx T body copy.
	 *
	 * @return the esx T body copy
	 */
	String getEsxTBodyCopy();

}

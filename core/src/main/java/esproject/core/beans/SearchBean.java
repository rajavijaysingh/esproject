package esproject.core.beans;

/**
 * The Class SearchBean.
 */
public class SearchBean {

	/** The prop name. */
	private String propName;

	/** The es key. */
	private String esKey;

	/**
	 * Instantiates a new search bean.
	 *
	 * @param propName the prop name
	 * @param esKey    the es key
	 */
	public SearchBean(String propName, String esKey) {
		this.propName = propName;
		this.esKey = esKey;
	}

	/**
	 * Gets the prop name.
	 *
	 * @return the prop name
	 */
	public String getPropName() {
		return propName;
	}

	/**
	 * Gets the es key.
	 *
	 * @return the es key
	 */
	public String getEsKey() {
		return esKey;
	}

}

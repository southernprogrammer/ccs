package edu.columbia.bonaha;

/**
 *
 * @author sumans
 *
 */
public interface BListener {

	/**
	 *
	 * @param n
	 */
	void serviceUpdated(BNode n);

	/**
	 *
	 * @param n
	 */
	void serviceExited(BNode n);
}

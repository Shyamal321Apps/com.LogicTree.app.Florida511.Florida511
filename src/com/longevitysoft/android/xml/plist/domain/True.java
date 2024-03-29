package com.longevitysoft.android.xml.plist.domain;

/**
 * Represents a simple plist true element.
 */
public class True extends PListObject implements IPListSimpleObject<Boolean> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3560354198720649001L;

	public True() {
		setType(PListObjectType.TRUE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.longevitysoft.android.xml.plist.domain.IPListSimpleObject#getValue()
	 */

	public Boolean getValue() {
		return new Boolean(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.longevitysoft.android.xml.plist.domain.IPListSimpleObject#setValue
	 * (java.lang.Object)
	 */

	public void setValue(Boolean val) {
		// noop
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.longevitysoft.android.xml.plist.domain.IPListSimpleObject#setValue
	 * (java.lang.String)
	 */

	public void setValue(java.lang.String val) {
		// noop
	}

}
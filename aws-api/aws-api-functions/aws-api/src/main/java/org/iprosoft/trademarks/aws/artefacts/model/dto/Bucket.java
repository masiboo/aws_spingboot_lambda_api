package org.iprosoft.trademarks.aws.artefacts.model.dto;

import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode
public class Bucket implements Serializable {

	private final static long serialVersionUID = 434674237350152356L;

	private String name;

	/**
	 * No args constructor for use in serialization
	 */
	public Bucket() {
	}

	/**
	 * @param name
	 */
	public Bucket(String name) {
		super();
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(Bucket.class.getName())
			.append('@')
			.append(Integer.toHexString(System.identityHashCode(this)))
			.append('[');
		sb.append("name");
		sb.append('=');
		sb.append(((this.name == null) ? "<null>" : this.name));
		sb.append(',');
		if (sb.charAt((sb.length() - 1)) == ',') {
			sb.setCharAt((sb.length() - 1), ']');
		}
		else {
			sb.append(']');
		}
		return sb.toString();
	}

}

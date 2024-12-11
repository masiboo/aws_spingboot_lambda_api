package org.iprosoft.trademarks.aws.artefacts.model.entity;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PagedArtefactJobs {

	private Set<ArtefactJob> items;

	private String nextToken;

	private boolean hasMorePages;

	public PagedArtefactJobs() {
		items = new HashSet<>();
	}

	// Constructor
	public PagedArtefactJobs(Set<ArtefactJob> items, String nextToken, boolean hasMorePages) {
		this.items = items;
		this.nextToken = nextToken;
		this.hasMorePages = hasMorePages;
	}

	// Getters and setters
	public Set<ArtefactJob> getItems() {
		return items;
	}

	public void setItems(Set<ArtefactJob> items) {
		this.items = items;
	}

	public void appendItems(Set<ArtefactJob> items) {
		this.items.addAll(items);
	}

	public boolean isHasMorePages() {
		return hasMorePages;
	}

	public void setHasMorePages(boolean hasMorePages) {
		this.hasMorePages = hasMorePages;
	}

	public String getNextToken() {
		return nextToken;
	}

	public void setNextToken(String nextToken) {
		this.nextToken = nextToken;
	}

}

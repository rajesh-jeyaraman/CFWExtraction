package com.p3.archon.jsonparser.bean;

import java.util.ArrayList;
import java.util.List;

public class Result {
	Long id;
	String name;
	List<Children> children = new ArrayList<Children>();
	String hasChildren;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Children> getChildren() {
		return children;
	}

	public void setChildren(List<Children> children) {
		this.children = children;
	}
	
	

	public String getHasChildren() {
		return hasChildren;
	}

	public void setHasChildren(String hasChildren) {
		this.hasChildren = hasChildren;
	}

	@Override
	public String toString() {
		return "Result [id=" + id + ", name=" + name + ", nodes=" + children + ", hasChildren=" + hasChildren +"]";
	}

}

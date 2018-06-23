package com.p3.archon.jsonparser.bean;

import java.util.ArrayList;
import java.util.List;

public class Result {
	Long id;
	String name;
	List<Nodes> nodes = new ArrayList<Nodes>();
	
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
	public List<Nodes> getNodes() {
		return nodes;
	}
	public void setNodes(List<Nodes> nodes) {
		this.nodes = nodes;
	}
	
	@Override
	public String toString() {
		return "Result [id=" + id + ", name=" + name + ", nodes=" + nodes + "]";
	}
	
}

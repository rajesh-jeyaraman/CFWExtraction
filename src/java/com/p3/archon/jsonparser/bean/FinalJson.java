package com.p3.archon.jsonparser.bean;

import java.util.ArrayList;
import java.util.List;

public class FinalJson {

	List<Nodes> nodes = new ArrayList<Nodes>();

	public List<Nodes> getNodes() {
		return nodes;
	}

	public void setNodes(List<Nodes> nodes) {
		this.nodes = nodes;
	}

	@Override
	public String toString() {
		return "FinalJson [nodes=" + nodes + "]";
	}

}

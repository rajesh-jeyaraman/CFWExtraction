package com.p3.archon.jsonparser.bean;

import java.util.ArrayList;
import java.util.List;

public class FinalJson {

	List<Children> children = new ArrayList<Children>();

	public List<Children> getChildren() {
		return children;
	}

	public void setChildren(List<Children> children) {
		this.children = children;
	}

	@Override
	public String toString() {
		return "FinalJson [nodes=" + children + "]";
	}

}

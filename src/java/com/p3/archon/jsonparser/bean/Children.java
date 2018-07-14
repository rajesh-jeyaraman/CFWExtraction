package com.p3.archon.jsonparser.bean;

import java.util.ArrayList;
import java.util.List;

public class Children {
	String frompath;
	String datatype;
	String name;
	boolean search;
	boolean result;
	Long id;
	String filename;	
	List<Children> children = new ArrayList<Children>();
	String minoccurance;
	String maxoccurance;
	String hasChildren;
	String conditionvalue;
	String conditionpath;
    String condition;
    boolean iscondition;

	public String getFrompath() {
		return frompath;
	}

	public void setFrompath(String frompath) {
		this.frompath = frompath;
	}

	public String getDatatype() {
		return datatype;
	}

	public void setDatatype(String datatype) {
		this.datatype = datatype;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isSearch() {
		return search;
	}

	public void setSearch(boolean search) {
		this.search = search;
	}

	public boolean isResult() {
		return result;
	}

	public void setResult(boolean result) {
		this.result = result;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public List<Children> getChildren() {
		return children;
	}

	public void setChildren(List<Children> children) {
		this.children = children;
	}

	public String getMinoccurance() {
		return minoccurance;
	}

	public void setMinoccurance(String minoccurance) {
		this.minoccurance = minoccurance;
	}

	public String getMaxoccurance() {
		return maxoccurance;
	}

	public void setMaxoccurance(String maxoccurance) {
		this.maxoccurance = maxoccurance;
	}	

	public String getHasChildren() {
		return hasChildren;
	}

	public void setHasChildren(String hasChildren) {
		this.hasChildren = hasChildren;
	}
	

	public String getConditionvalue() {
		return conditionvalue;
	}

	public void setConditionvalue(String conditionvalue) {
		this.conditionvalue = conditionvalue;
	}

	public String getConditionpath() {
		return conditionpath;
	}

	public void setConditionpath(String conditionpath) {
		this.conditionpath = conditionpath;
	}

	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

	public boolean isIscondition() {
		return iscondition;
	}

	public void setIscondition(boolean iscondition) {
		this.iscondition = iscondition;
	}

	@Override
	public String toString() {
		return "Children [frompath=" + frompath + ", datatype=" + datatype + ", name=" + name + ", search=" + search
				+ ", result=" + result + ", id=" + id + ", filename=" + filename + ", children=" + children
				+ ", minoccurance=" + minoccurance + ", maxoccurance=" + maxoccurance + ", hasChildren=" + hasChildren
				+ ", conditionvalue=" + conditionvalue + ", conditionpath=" + conditionpath + ", condition=" + condition
				+ ", iscondition=" + iscondition + "]";
	}
/*
	@Override
	public String toString() {
		return "Nodes [frompath=" + frompath + ", datatype=" + datatype + ", name=" + name + ", search=" + search
				+ ", result=" + result + ", id=" + id + ", filename=" + filename + ", nodes=" + children + ", hasChildren="+ hasChildren +"]";
	}
*/

}

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

	@Override
	public String toString() {
		return "Nodes [frompath=" + frompath + ", datatype=" + datatype + ", name=" + name + ", search=" + search
				+ ", result=" + result + ", id=" + id + ", filename=" + filename + ", nodes=" + children + "]";
	}

}

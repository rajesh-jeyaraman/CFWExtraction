package com.p3.archon.xmlsipautomater.processor.bean;

import java.util.ArrayList;
import java.util.List;

public class ArchonXml {

	private String baseFolder;
	private String fileContent;
	private List<String> attachement = new ArrayList<>();

	public String getBaseFolder() {
		return baseFolder;
	}

	public void setBaseFolder(String baseFolder) {
		this.baseFolder = baseFolder;
	}

	public List<String> getAttachement() {
		return attachement;
	}

	public void setAttachement(List<String> attachement) {
		this.attachement = attachement;
	}

	public String getFileContent() {
		return fileContent;
	}

	public void setFileContent(String fileContent) {
		this.fileContent = fileContent;
	}

}

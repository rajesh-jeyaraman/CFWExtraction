package com.p3.archon.AuditReport.bean;

import java.util.List;
import java.util.Map;

public class CaseBean {
	private String caseNumber;
	private String caseType;
	private String count;
	private List<String> fileList;
	private String status;
	private List<ErrorBean> errorList;
	private Map<String, String> errorFileMap;
	
	public String getCaseNumber() {
		return caseNumber;
	}
	public void setCaseNumber(String caseNumber) {
		this.caseNumber = caseNumber;
	}
	public String getCaseType() {
		return caseType;
	}
	public void setCaseType(String caseType) {
		this.caseType = caseType;
	}
	public String getCount() {
		return count;
	}
	public void setCount(String count) {
		this.count = count;
	}
	public List<String> getFileList() {
		return fileList;
	}
	public void setFileList(List<String> fileList) {
		this.fileList = fileList;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public List<ErrorBean> getErrorList() {
		return errorList;
	}
	public void setErrorList(List<ErrorBean> errorList) {
		this.errorList = errorList;
	}
	public Map<String, String> getErrorFileMap() {
		return errorFileMap;
	}
	public void setErrorFileMap(Map<String, String> errorFileMap) {
		this.errorFileMap = errorFileMap;
	}
}

package com.p3.archon.AuditReport.bean;

import java.util.List;

public class ArchiveResponseBean {
	
	private List<CaseBean> caseList;
	private long totalFileCount;
	private long successFileCount;
	private long totalCaseCount;
	private long successCaseCount;
	
	public List<CaseBean> getCaseList() {
		return caseList;
	}
	public void setCaseList(List<CaseBean> caseList) {
		this.caseList = caseList;
	}
	public long getTotalFileCount() {
		return totalFileCount;
	}
	public void setTotalFileCount(long totalFileCount) {
		this.totalFileCount = totalFileCount;
	}
	public long getSuccessFileCount() {
		return successFileCount;
	}
	public void setSuccessFileCount(long successFileCount) {
		this.successFileCount = successFileCount;
	}
	public long getTotalCaseCount() {
		return totalCaseCount;
	}
	public void setTotalCaseCount(long totalCaseCount) {
		this.totalCaseCount = totalCaseCount;
	}
	public long getSuccessCaseCount() {
		return successCaseCount;
	}
	public void setSuccessCaseCount(long successCaseCount) {
		this.successCaseCount = successCaseCount;
	}

}

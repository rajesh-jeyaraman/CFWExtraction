package XMLFileHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NotificationBean {
	private String caseNumber;
	private int fileCount;
	private ArrayList<String> files;
	private String caseType;
	private boolean errorFlg;
	private ArrayList<String> errorList = null;
	private ArrayList<String> errorFileList = null;
	
	public NotificationBean() {
		caseNumber = "";
		fileCount = 0;
		files = new ArrayList<String>();
		caseType = "";
		errorFlg = false;
		errorList = new ArrayList<String>();
		errorFileList = new ArrayList<String>();
	}
	
	public String getCaseNumber() {
		return caseNumber;
	}
	public void setCaseNumber(String caseNumber) {
		this.caseNumber = caseNumber;
	}
	public int getFileCount() {
		return fileCount;
	}
	public void setFileCount(int fileCount) {
		this.fileCount = fileCount;
	}
	public ArrayList<String> getFiles() {
		return files;
	}
	public void setFiles(ArrayList<String> files) {
		this.files = files;
	}
	public void addFile(String fileName) {
		files.add(fileName);
	}

	public String getCaseType() {
		return caseType;
	}

	public void setCaseType(String caseType) {
		this.caseType = caseType;
	}

	public boolean isErrorFlg() {
		return errorFlg;
	}

	public void setErrorFlg(boolean errorFlg) {
		this.errorFlg = errorFlg;
	}
	
	
	
}

package XMLFileHandler;

import java.util.ArrayList;

public class NotificationBean {
	private String caseNumber;
	private int fileCount;
	private ArrayList<String> files;
	
	public NotificationBean() {
		caseNumber = "";
		fileCount = 0;
		files = new ArrayList<String>();
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
	
	
	
}
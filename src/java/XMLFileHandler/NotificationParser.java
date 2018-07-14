package XMLFileHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

public class NotificationParser {
	
	private int totalCases;
	private ArrayList<NotificationBean> caseList = null;
	private String fileName = null;
	private DocumentBuilderFactory factory = null;
    private DocumentBuilder builder = null;
    private Document doc = null;
    private XPathFactory xpathfactory = null;
    private XPath xpath = null;
		
	public NotificationParser(String fileName) {
		this.fileName = fileName;
		caseList = new ArrayList<NotificationBean>();
	}
	
	public static void test(String args[])throws Exception{
		System.out.println("I am in NotificationParser : Main()");
		NotificationParser p = new NotificationParser("/Users/admin/Documents/test/data/Notification.xml");
		p.parseFile();
				
	}
	
	public void parseFile() throws Exception {
		// Creating a dom object to search xpath during extraction.
		factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false); // never forget this!
        builder = factory.newDocumentBuilder();
        doc = builder.parse(fileName);
        xpathfactory = XPathFactory.newInstance();
        xpath = xpathfactory.newXPath();
        
        XPathExpression expr = xpath.compile("/ArchiveNotify/CaseList/Case");
        
        Object result = expr.evaluate(doc, XPathConstants.NODESET);
        NodeList nodes = (NodeList) result;
        
	    for(int i=0; i < nodes.getLength(); ++i) {
	    	Node n = nodes.item(i);
	    	NotificationBean caseDtls = getCase(n);
	    	caseList.add(caseDtls);
	    }
	    printCaseList();
	}
	
	private NotificationBean getCase(Node n) throws Exception{
		NotificationBean bean = new NotificationBean();
		NodeList nodes = n.getChildNodes();
		int fileCount = 0;
		for(int i=0; i<nodes.getLength(); ++i) {
			Node child = nodes.item(i);

			if(child.getNodeName().equalsIgnoreCase("CaseNumber")== true) {
				bean.setCaseNumber(child.getTextContent().trim());				
			}
			if(child.getNodeName().equalsIgnoreCase("FileList")==true) {
				NodeList fileList = child.getChildNodes();
				for(int j=0; j< fileList.getLength(); ++j ) {
					Node file = fileList.item(j);
					if(file.getNodeName().equalsIgnoreCase("FileName")==true) {						
						bean.addFile(file.getTextContent().trim());
						++fileCount;
					}
				}
			}
			if(child.getNodeName().equalsIgnoreCase("CaseType")==true) {
				bean.setCaseType(child.getTextContent().trim());				
			}
		}
		bean.setFileCount(fileCount);
		
		//Check if case number matches with File names
		
		//File count matches with Number of files in file name list
		
		return bean;
	}
	
	private void printCaseList() {
		for(NotificationBean b: caseList) {
			System.out.println("CaseNumber:" + b.getCaseNumber());
			System.out.println("CaseType:" + b.getCaseType());
			System.out.println("Count:" + b.getFileCount());
			for(String file: b.getFiles()) {
				System.out.println("FileName:" + file);
			}
		}
	}
	
	private ArrayList<String> getFileList(Node fileList) throws Exception {
		ArrayList<String> files = new ArrayList<String>();
		NodeList nodes = fileList.getChildNodes();
		
	    for(int i=0; i < nodes.getLength(); ++i) {
	    	Node n = nodes.item(i);
	    	System.out.println(n.getNodeName() + n.getNodeValue());
	    } 
		return files;		
	}
	
	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public int getTotalCases() {
		return totalCases;
	}
	public void setTotalCases(int totalCases) {
		this.totalCases = totalCases;
	}
	public ArrayList<NotificationBean> getCaseList() {
		return caseList;
	}
	public void setCaseList(ArrayList<NotificationBean> caseList) {
		this.caseList = caseList;
		
	}
	

}

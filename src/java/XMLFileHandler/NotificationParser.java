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
		NotificationParser p = new NotificationParser("/Users/admin/Documents/test/Details/Notification.xml");
		p.parseFile();
				
	}
	
	public void parseFile() throws Exception {
		// Creating a dom object to search xpath during extraction.
		factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true); // never forget this!
        builder = factory.newDocumentBuilder();
        doc = builder.parse(fileName);
        xpathfactory = XPathFactory.newInstance();
        xpath = xpathfactory.newXPath();
        
        XPathExpression expr = xpath.compile("/NOTIFY/CASE");
        
        Object result = expr.evaluate(doc, XPathConstants.NODESET);
        NodeList nodes = (NodeList) result;
        
	    for(int i=0; i < nodes.getLength(); ++i) {
	    	Node n = nodes.item(i);
	    	NotificationBean caseDtls = getCase(n);
	    	caseList.add(caseDtls);
	    }
	    printCaseList();
	}
	
	private NotificationBean getCase(Node n) {
		NotificationBean bean = new NotificationBean();
		NodeList nodes = n.getChildNodes();
		int fileCount = 0;
		for(int i=0; i<nodes.getLength(); ++i) {
			Node child = nodes.item(i);
			String str = child.getNodeName();
			String val = child.getTextContent();
			
			if(child.getNodeName().equalsIgnoreCase("CASENUMBER")== true) {
				bean.setCaseNumber(val);				
			}
			if(child.getNodeName().equalsIgnoreCase("FILENAME")==true) {
				bean.addFile(val);
				++fileCount;
			}
		}
		bean.setFileCount(fileCount);
		
		return bean;
	}
	
	private void printCaseList() {
		for(NotificationBean b: caseList) {
			System.out.println("CASENUMBER:" + b.getCaseNumber());
			System.out.println("COUNT:" + b.getFileCount());
			for(String file: b.getFiles()) {
				System.out.println("FILENAME:" + file);
			}
		}
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

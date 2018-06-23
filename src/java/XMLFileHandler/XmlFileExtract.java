package XMLFileHandler;

import java.io.*;
import java.util.ArrayList;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;


public class XmlFileExtract {
	
	private String inConfigFileName = null;	// Jason Format
	private ArrayList<String> fileList = null;
	private String TEXT = "/text()";		// To append the string in xpath
	private String RATESYMBOL ="@";
	
	public XmlFileExtract(String configFileName, String fileName) {
		this.inConfigFileName = configFileName;
		this.fileList = new ArrayList<String>();
		this.fileList.add(fileName);
	}
	
	public XmlFileExtract(String configFileName, ArrayList<String> fileNameList) {
		this.inConfigFileName = configFileName;
		this.fileList = fileNameList;
	}
	
	public ArrayList<String> getXpathListFromConfigFile() throws Exception{
		
		ConfigFileParserJson caseConfigFile = new ConfigFileParserJson(inConfigFileName);
		
		//TO DO: Redefine the xpath. 
		return caseConfigFile.getXpathList();
	}
	
	private ArrayList<NameValuePair> getExtractedXpathList(String inputFileName) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true); // never forget this!
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(inputFileName);
 
        XPathFactory xpathfactory = XPathFactory.newInstance();
        XPath xpath = xpathfactory.newXPath();
        
        ArrayList<String> confList = getXpathListFromConfigFile();
        ArrayList<NameValuePair> extractedList = new ArrayList<NameValuePair>();
        
        for(String path: confList ) {
        	if(path.contains(TEXT) ==false && path.contains(RATESYMBOL) == false){
        		path = path.concat(TEXT);
        	}
        	XPathExpression expr = xpath.compile(path);
        	Object result = expr.evaluate(doc,  XPathConstants.NODESET);
        	NodeList nodes = (NodeList) result;
        	for(int i = 0; i <nodes.getLength(); i++) {
        		extractedList.add(new NameValuePair(path,nodes.item(i).getNodeValue()));
        	/*	System.out.println(path);
        		System.out.println(nodes.item(i).getNodeValue());
        		System.out.println("***");
        	*/
        	}
        }
        
        return extractedList;
	}
	
	public void extractDataToFile(String outFileName) throws Exception {
		
		for( String file : fileList) {
			ArrayList<NameValuePair> dataList = getExtractedXpathList(file);
			for(NameValuePair data : dataList) {
				System.out.println(data.getName());
				System.out.println(data.getValue());
				System.out.println("----");
			}
		}
		
	}
	
}

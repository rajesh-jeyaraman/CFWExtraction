package XMLFileHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;


import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.impl.inst2xsd.Inst2Xsd;
import org.apache.xmlbeans.impl.inst2xsd.Inst2XsdOptions;
import org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument;

public class Test {

	public static void DOMmain(String[] args) throws Exception {
		
		String fileName = "/Users/admin/eclipse-workspace/CFW/src/InputConfig.json";
		String dataFile = "/Users/admin/Documents/CFW_DeceasedCase.xml";
		String outputXml = "Sample.xml";
	
	/*	ConfigFileParserJson caseConfigFile = new ConfigFileParserJson(fileName);
		ArrayList<String> xpaths = caseConfigFile.getXpathList();
		
		for(String x : xpaths) {
			System.out.println(x);
		}
		*/
		
		XmlFileExtract xmlHandler = new XmlFileExtract(fileName, dataFile);
		xmlHandler.extractDataToFile(outputXml);
		
	}
	
public static void SAXmain(String[] args) throws Exception {
    	
    	SAXParserFactory spf = SAXParserFactory.newInstance();
        SAXParser sp = spf.newSAXParser();
        XMLReader xr = sp.getXMLReader();
        
        XMLParser fch = new XMLParser(xr);
        ArrayList<NameValuePair> xpathList = new ArrayList<NameValuePair>();
        XMLParser.xpathList = xpathList;
        
        //xr.setContentHandler(new FragmentContentHandler(xr));
        xr.setContentHandler(fch);
        xr.parse(new InputSource(new FileInputStream("/Users/admin/Documents/CFW_DeceasedCase.xml")));
        
        for(NameValuePair xpath: xpathList) {
        	System.out.println(xpath.getName() + "=" + xpath.getValue());
        }
        /*
         File file = new File("/Users/admin/Documents/testFile.txt");
       
	     //Create the file
	     if (file.createNewFile()){
	     System.out.println("File is created!");
	     }else{
	     System.out.println("File already exists.");
	     }
	      
	     //Write Content
	     FileWriter writer = new FileWriter(file);
	     writer.write(s);
	     writer.close();
	     */
    }

	public static void Testmain (String[] args) throws Exception {
		//String str = "/pagedata[1]/pxFlow[1][@REPEATINGTYPE='PageGroup']/rowdata[1][@REPEATINGINDEX='DeceasedEventProcess']/pxSubscript[1]";
		String configFile = "";
		//String dataFile = "/Users/admin/Documents/CFW-DE-20131212-109_case.xml";
		String dataFile = "/Users/admin/Documents/J/JPMC/3rd set of case files/CFW-DE-20131212-109_case.xml";
		
		//ArrayList<NameValuePair> configXpaths = getXpathListFromXml(configFile);
		ArrayList<NameValuePair> dataXpaths = getXpathListFromXml(dataFile);
		ArrayList<NameValuePair> finalXpathList = new ArrayList<NameValuePair>();
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true); // never forget this!
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse("/Users/admin/eclipse-workspace/CFW/XMLFileHandler/SearchField.xml");
 
        XPathFactory xpathfactory = XPathFactory.newInstance();
        XPath xpath = xpathfactory.newXPath();
 
		for(NameValuePair data: dataXpaths) {
			String str = removeAttr(data.getName());
			   
			// XPathExpression expr = xpath.compile("/pagedata/pxFlow/rowdata/pxSubscript/text()");
	        XPathExpression expr = xpath.compile(str);
	        
	        Object result = expr.evaluate(doc, XPathConstants.NODESET);
	        NodeList nodes = (NodeList) result;
	        if(nodes.getLength()>0) {
	        	finalXpathList.add(data);
	        	//System.out.println(data.getName()+"=" + data.getValue());
	        }
		}
		/*
		ArrayList<NameValuePair> list = new ArrayList<NameValuePair>();
		list.add(new NameValuePair("/pagedata[1]/DateOfDeath[1]","20131205T060000.000 GMT"));
		list.add(new NameValuePair("/pagedata[1]/IncomingAccountNumber[1]","00000000000000632782835"));
		list.add(new NameValuePair("/pagedata[1]/Customers[1]/rowdata[1]/CustomerName[1]","LEC CO INC"));
		list.add(new NameValuePair("/pagedata[1]/Customers[1]/rowdata[1]/pxObjClass[1]","JPMC-Retail-CFW-Data-Document"));
		list.add(new NameValuePair("/pagedata[1]/Customers[1]/rowdata[2]/pxObjClass[1]","JPMC-Retail-CFW-Data-Document2"));
		list.add(new NameValuePair("/pagedata[1]/Customers[2]/rowdata[1]/pxObjClass[1]","JPMC-Retail-CFW-Data-Document3"));
		list.add(new NameValuePair("/pagedata[1]/Documents[1]/rowdata[1]/SSNTIN[1]","311749635"));
		*/
		
		XPathUtils.createXML(finalXpathList, "output.xml");
		
	//	ArrayList<NameValuePair> list = new ArrayList<NameValuePair>();
	//	list.add(new NameValuePair("/pagedata[1]/Customers[1][@REPEATINGTYPE='PageList']/rowdata[1][@REPEATINGINDEX='1']/CustomerName[1]","LEC CO INC"));
		
	//	XPathUtils.createXML(list, "/Users/admin/eclipse-workspace/CFW/XMLFileHandler/output.xml" );
		XmlToXsd("output.xml","schema.xsd");
	}
	
	public static String removeAttr(String input) {
			String str = "";
			char ATTRSTART = '[';
			char ATTREND = ']';
			boolean skip = false;
			for(int i = 0 ;  i< input.length();++i ) {
				if(input.charAt(i) == ATTRSTART) {
					skip = true;
				}
				if(skip == false) {
					str +=input.charAt(i);
				}
				if(input.charAt(i) == ATTREND) {
					skip = false;
				}
			}
			return str;
	}
	
	public static ArrayList<NameValuePair> getXpathListFromXml(String fileName)throws Exception{
		
	   	SAXParserFactory spf = SAXParserFactory.newInstance();
        SAXParser sp = spf.newSAXParser();
        XMLReader xr = sp.getXMLReader();
        
        XMLParser fch = new XMLParser(xr);
        ArrayList<NameValuePair> xpathList = new ArrayList<NameValuePair>();
        XMLParser.xpathList = xpathList;
        
        //xr.setContentHandler(new FragmentContentHandler(xr));
        xr.setContentHandler(fch);
        xr.parse(new InputSource(new FileInputStream(fileName)));
        
        for(NameValuePair xpath: xpathList) {
        	//System.out.println(xpath.getName() + "=" + xpath.getValue());
        }
        
        return xpathList;
	}
	
	public static void XmlToXsd(String xmlFileName, String xsdFileName) throws Exception {
		
		final Inst2XsdOptions options = new Inst2XsdOptions();
	    options.setDesign(Inst2XsdOptions.DESIGN_RUSSIAN_DOLL);
	   
	    XmlObject[] xml = null;
		try {
			xml = new XmlObject[] {XmlObject.Factory.parse(new File(xmlFileName))};
			
		} catch (XmlException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    final SchemaDocument[] schemaDocs = Inst2Xsd.inst2xsd(xml, options);
	    System.out.println(schemaDocs[0]);
	}
	
	public static void main (String[] args) throws Exception {
		simulateXmlFile();
	}
	public static void simulateXmlFile() {
		String file1 = "/Users/admin/Documents/test/data/batch1/Processed/CFW-DE-20170112-3_case.xml";
		String file2 = "/Users/admin/Documents/test/data/batch1/Processed/CFW-DE-20170112-3-1_account.xml";
		String file3 = "/Users/admin/Documents/test/data/batch1/Processed/CFW-DE-20170112-3-2_account.xml";
		int srlNum = 1000;
		int count = 2000;
		String prefix = "GEN-DE-20180625_";
	
		try {
			for(int i = 0; i < count; ++i ) {
				String dataStr = getFileData(file1);
				String caseNum = prefix + srlNum;
				String str = dataStr.replace("CFW-DE-20170112-3", caseNum);
				String fileName = prefix + srlNum +"_case.xml";
				writeFile(str, fileName);
				
				String dataStr2 = getFileData(file2);
				String str2 = dataStr2.replace("CFW-DE-20170112-3", caseNum);
				String fileName2 = prefix + srlNum +"-1_account.xml";
				writeFile(str2, fileName2);
				
				
				String dataStr3 = getFileData(file3);
				String str3 = dataStr2.replace("CFW-DE-20170112-3", caseNum);
				String fileName3 = prefix + srlNum +"-2_account.xml";
				writeFile(str3, fileName3);
				
				++srlNum;
				System.out.println("Generated set " + (i + 1));
			}
				//System.out.println(str);
		}
		catch(Exception e) {
			System.out.println(e.getMessage());
		}
	}
	public static String getFileData(String file) throws Exception{
		FileInputStream fs = new FileInputStream(file);
		Reader fr = new InputStreamReader(fs);

		String str = "";
		int ch = (char)fr.read();
		
		while(ch != -1) {
			str += (char)ch;
			ch = fr.read();
		}
		fr.close();
		//System.out.println("xml content is:"+str);
		return str;
	}
	public static void writeFile(String str, String fileName) throws Exception{
		String dir = "/Users/admin/Documents/test/data/batch2/" + fileName;
		FileOutputStream fs = new FileOutputStream(dir);
		Writer fw = new OutputStreamWriter(fs);
		
		fw.write(str);
		fw.close();
		
	}

}

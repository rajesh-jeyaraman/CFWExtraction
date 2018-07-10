package XMLFileHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
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

import com.p3.archon.jsonparser.JsonProcessor;

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
		
		System.out.println("I am in main");
		//testJsonParserCode();  
		testDate(args);
		
	}
	public static void testJsonParserCode() throws Exception{
		
		JsonProcessor.readJsonWithObjectMapper("/Users/admin/Documents/test/config/case.json");
		ConfigFileParserJson xpath = new ConfigFileParserJson("FinalResult.json");
		ArrayList<NameValuePair> topath = new ArrayList<NameValuePair>();
		
		for(NameValuePair s: xpath.getXpathList() ) {
			System.out.println(s.getTopath());
			topath.add(new NameValuePair(s.getTopath(),"DATA"));
		}
		
		//Create an xml document with collated xpath list
		String xmlOutput = XPathUtils.createXML(topath, "temp.xml");
		System.out.println(xmlOutput);
	}
	

		
		public static String iaDateFormat(String date_vals){
			String IA_formatter_date = date_vals.substring(0,4) + "-" +date_vals.substring(4,6) + "-" + date_vals.substring(6,8)
										+ " " + date_vals.substring(9,11) +":"+date_vals.substring(11,13) +":"+ date_vals.substring(13,15);
			return IA_formatter_date;
		}
		
		public static void testDate(String args[]){
		//	if(args.length == 0){
		//		System.out.println("Date input needed.");
		//		System.exit();
		//	}
		//	String given_date = args[0];
			String given_date = "20170112T142055.597 GMT";
			String[] date_vals = given_date.split("[.]");
			System.out.println(iaDateFormat(date_vals[0]));
		}


}

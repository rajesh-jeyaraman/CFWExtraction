package XMLFileHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.IOUtils;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.impl.inst2xsd.Inst2Xsd;
import org.apache.xmlbeans.impl.inst2xsd.Inst2XsdOptions;
import org.apache.xmlbeans.impl.store.Path;
import org.apache.xmlbeans.impl.xb.ltgfmt.TestCase.Files;
import org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import com.p3.archon.xmlsipautomater.PackageMain;
import com.p3.archon.jsonparser.JsonProcessor;
import com.p3.archon.xmlsipautomater.PackageMain;

public class XmlMain {

	private  XmlFileExtratorConfig config = null;
	private  String configFileName = null;
	private  ArrayList<NameValuePair> configXpathList = null;
	
	private DocumentBuilderFactory factory = null;
    private DocumentBuilder builder = null;
    private Document doc = null;
    private XPathFactory xpathfactory = null;
    private XPath xpath = null;
    int totalFileCount = 0;
    int totalArchiveCount = 0;
	
	public boolean setConfigFileName(String configFileName) {
		if(configFileName != null) {
			this.configFileName = configFileName;
			return true;
		}
		return false;
	}
	
	public XmlFileExtratorConfig getConfig() {
		return config;
	}

	public void setConfig(XmlFileExtratorConfig config) {
		this.config = config;
	}

	public static void main(String[] args) {
		try {
			XmlMain process = new XmlMain();
			// Initial Validation
			if(process.setConfigFileName(args[0]) == false) {
				System.out.println("XMLFileExtractorConfig.json file is missing");
				return;
			}
			
			//Read the configuration setting for this process
			process.setConfig(new XmlFileExtratorConfig(args[0]));
			if(process.getConfig().parseFile() == false)return;
			
			ArrayList<String> dirList = process.getConfig().getDataDirectoryList();
			for(String dir: dirList) {
				process.getConfig().setDataFolderPath(dir);
				process.start();
			}
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
		
		return;
	}
	
	public String getParserConfigAsXml()throws Exception {

		String xmlOutput = null;
	
		//Collate all configuration files into one file and 
		ArrayList<String> fileList = config.getFileList(config.getConfigFolderPath());
		ArrayList<String> configFiles = new ArrayList<String>();
		for(String f: fileList) {
			if(f.toLowerCase().contains(".xml")== true) {
				configFiles.add(f);
			}
		}
		if(configFiles.size() ==0 ) {	// Default.. directory always get's added
			System.out.println("Schema file for extraction is missing in folder " +config.getConfigFolderPath() );
			return xmlOutput; // TODO: throw exception
		}
		
		configXpathList = new ArrayList<NameValuePair>();
		for(String file: configFiles) {
			
			//JsonProcessor.readJsonWithObjectMapper(file);
			ArrayList<NameValuePair> xpathList = getXpathListFromXml(file);
			
			for(NameValuePair xpath: xpathList) {
				configXpathList.add(xpath);
			}
		}
		
		//Create an xml document with collated xpath list
		xmlOutput = XPathUtils.createXML(configXpathList, "temp.xml");
		
		return xmlOutput;
	}
    
	public void initDom(String xml) throws Exception{
		// Creating a dom object to search xpath during extraction.
		factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true); // never forget this!
        builder = factory.newDocumentBuilder();
        doc = builder.parse("temp.xml");
        xpathfactory = XPathFactory.newInstance();
        xpath = xpathfactory.newXPath();
	}
	
	public boolean compareXpathWithConfig(String path) throws Exception{
		boolean isAvail = false;
		
		String str = removeAttr(path);
        XPathExpression expr = xpath.compile(str);
        
        Object result = expr.evaluate(doc, XPathConstants.NODESET);
        NodeList nodes = (NodeList) result;
        
        if(nodes.getLength()>0) {
        	isAvail = true; 		// Xpath matches
        }
		
		return isAvail;
	}
	
	public boolean isProcessTriggerFileExists(String directory) {
		boolean status = false;
		
		//Check if Notification file present in specified folder if so start the process else move to next directory
		File f = new File(directory + config.getProcessTriggerFileName() );  // Bascially Notification.xml file
		if(f.exists() == true) {
			status = true;
		}
		else
		{
			System.out.println("Notification file not present in directory " +directory );
			status = false;
		}
		return status;
	}
	
	public ArrayList<NameValuePair> parseAndExtractData(String xmlFileName) throws Exception {
		
		ArrayList<NameValuePair> dataXpaths = getXpathListFromXml(xmlFileName);		
		ArrayList<NameValuePair> unorderXpathList = new ArrayList<NameValuePair>();

		for(NameValuePair data: dataXpaths) {
			if(compareXpathWithConfig(data.getName()) == true) {
				unorderXpathList.add(data);
			}			
		}							
		return getOrderedList(unorderXpathList, configXpathList);
	}
	
	public void start()throws Exception {
		try {
			
		
			//Create a xml file to perform xpath search during data extraction.
			String xml = getParserConfigAsXml();
			initDom(xml);
			
	        // Data Extraction
			ArrayList<NameValuePair> finaleResponse = new ArrayList<NameValuePair>();

			if(isProcessTriggerFileExists(config.getDataFolderPath()) == false) {	
				System.out.println("Trigger file does not exists hence no processing required.");
				return; // to next directory
			}
			
	        ArrayList<NameValuePair> finalXpathList = new ArrayList<NameValuePair>();
			ArrayList<String> files = config.getDataFileList();
			ArrayList<String> relatedFiles = null;
			ArrayList<String> caseNumbers = new ArrayList<String>();
			
			int batch = 0;
			totalFileCount = files.size();
			totalArchiveCount = 0;
			
			for(String file: files) {
				String caseNumber = null;
				ArrayList<NameValuePair> fileXpathList =null;
				
				try {
					if(config.isDataFile(file)== true) {
						++batch;
						fileXpathList = new ArrayList<NameValuePair>();
						caseNumber = getCaseNumberFromFileName(file);
						relatedFiles = getRelatedFilesForCaseNumber(caseNumber,files);
						fileXpathList = parseAndExtractData(file);  // Based on configuration file, data is extracted.							
						copyFilesToOutDir(relatedFiles, caseNumber);
						addSuccessInArchiveResponse(batch, finaleResponse, caseNumber, relatedFiles);
						
					}
					else {
						System.out.println(file + "Skipping as it is not data file.");
						continue;
					}
				}
				catch(Exception e) {
					System.out.println("Error in parsing xml file :" + file);
					File fn = new File(file);
					NameValuePair error = new NameValuePair(fn.getName(), "Error in parsing Xml:" +e.getMessage() );
					//Move the file and related file to error directory
					//copyFilesToOutDir(relatedFiles);
					addFailureInArchiveResponse(batch, finaleResponse, caseNumber, relatedFiles, error);
					continue;
				}
				addAttachmentTag(fileXpathList, relatedFiles);
				addToFinalXpathList(batch,fileXpathList, finalXpathList);
				createOutXmlFile(batch,fileXpathList, caseNumber);
				caseNumbers.add(caseNumber);
			}
			//createOutXmlFile(finalXpathList);
			createSip();
			renameProcessTriggerFile();
			addTotalFileCount(finaleResponse);
			createResponseFile(finaleResponse);
						
			//Delete output folder
			for(String d: caseNumbers) {
				String outdir = config.getOutputFolderPath() + d;
				File dir = new File(outdir);
				if(dir.exists()) {
					String[]entries = dir.list();
					for(String s: entries){
					    File currentFile = new File(dir.getPath(),s);
					    currentFile.delete();
					}
					dir.delete();
				}
			}
			//Move files to processed folder
			String outDir = config.getDataFolderPath() + "Processed";
			File od = new File(outDir);
			
			if(!od.exists()) {
				if(od.mkdir()) {
					System.out.println(outDir + "Successfully created");
				}
			}
			outDir = outDir + File.separator;
			files.add(config.getDataFolderPath() + "Processed.xml"); // Add the trigger notification also to be moved.
			for(String f: files) {
				File file = new File(f); // Assuming f won't have full path
				file.renameTo(new File(outDir + file.getName()));
			}
			
		}
		catch(Exception e) {
			System.out.println(e.getMessage());
		}
	
	}
	
	private void createResponseFile(ArrayList<NameValuePair> response) {
		//Get count of archive response file. 
		ArrayList<String> fileList = new ArrayList<String>();
		
		File responseFolder = new File(config.getResponseFolderPath());
		File[] files = responseFolder.listFiles();
		for(File file: files) {
			if(file.isFile() == true) {
				if(file.getName().toLowerCase().startsWith("ARCHIVE_RESPONSE") == true){
					fileList.add(responseFolder + file.getName());
				}
			}
		}
		XPathUtils.createXML(response,config.getResponseFolderPath() + "ARCHIVE_RESPONSE_"+files.length +".xml");
	}
		
	private void renameProcessTriggerFile() {
		try {
			//Rename CFWSuccessNotification.xml file
			File file = new File(config.getDataFolderPath() + config.getProcessTriggerFileName());
			file.renameTo(new File(config.getDataFolderPath() + "Processed.xml"));
		}
		catch(Exception e) {
			System.out.println("Exception in renaming "+config.getProcessTriggerFileName() + "file.");
		}				
	}
	
	private void addToFinalXpathList(int batch, ArrayList<NameValuePair> fileXpathList, 
			ArrayList<NameValuePair>  finalXpathList) throws Exception {
		
		for(NameValuePair n: fileXpathList ) {
			String name = "/RECORDs[1]/RECORD["+ String.valueOf(batch) + "]" + n.getName();
			NameValuePair data = new NameValuePair(name, n.getValue());
			finalXpathList.add(data);
		}
	}
	private void createOutXmlFile(int batch, ArrayList<NameValuePair> finalXpathList, String caseNumber) throws Exception {
		// Create out directory
		String outdirectory = config.getOutputFolderPath() + caseNumber + File.separator;
		File od = new File(outdirectory);
		
		if(!od.exists()) {
			if(od.mkdir()) {
				System.out.println(outdirectory + "Successfully created");
			}
		}
		
		//Create output extracted file
		String outputFileName = outdirectory + "ARCHON_GEN_FILE_"+String.valueOf(batch)+".xml";
		XPathUtils.createXML(finalXpathList, outputFileName);
		
		/*
		//Create Schema File
		ArrayList<NameValuePair> xpathForXsd = new ArrayList<NameValuePair>();
		for(NameValuePair n: finalXpathList) {
			String name = "/RECORDs[1]/RECORD[1]" + n.getName();
			//System.out.println(name + "=" + "");
			xpathForXsd.add(new NameValuePair(name, ""));
		}
		
		//Generate schema file
		XPathUtils.createXML(configXpathList, "temp2.xml");
		XmlToXsd("temp2.xml", "pdi-schema.xml"); // ROOTS/ROOT to be added for SI
		*/
	}
	
	private void createSip() throws Exception {
		try {
			//Get count of existing sip file count. Just not to overwrite already existing files during continuos or loop run.  
			ArrayList<String> fileList = new ArrayList<String>();
			
			File sipFolder = new File(config.getSipOutputFolderPath());
			File[] files = sipFolder.listFiles();
			for(File file: files) {
				if(file.isFile() == true) {
					if(file.getName().toLowerCase().contains(".zip") == true){
						fileList.add(sipFolder + file.getName());
					}
				}
			}
			String str = "SET" + fileList.size();
			//SIP Creation
			String folder = config.getOutputFolderPath();
			String holding = config.getHolding();
			String app = config.getAppName();
			String producer = config.getProducer();
			String entity = config.getSipentity();
			String schema = config.getSchema();
			String outputPath = config.getSipOutputFolderPath(); // sip folder path for output
			new PackageMain().start(folder, holding, app, producer, entity, schema, outputPath, str);
		}
		catch(Exception e) {
			System.out.println("Error in creating sip: " + e.getMessage());
		}
	}
	
	private void addAttachmentTag(ArrayList<NameValuePair> finalXpathList, ArrayList<String> files) {
		int i = 1;
		String root= "/" + XPathUtils.getRootElement(finalXpathList.get(0).getName()) ;
		
		for(String file: files) {
			String attachmentPath = root + "[1]" + "/Attachments[1]/attachment[" + String.valueOf(i) + "]";
			String attachmentValue =new File(file).getName();
			NameValuePair data = new NameValuePair(attachmentPath,attachmentValue);
			finalXpathList.add(data);	
		}
		
	}
	
	private void addTotalFileCount(ArrayList<NameValuePair> response) {
		NameValuePair fileCount = new NameValuePair("/ARCHIVE/TOTAL_FILE_COUNT", String.valueOf(--totalFileCount) ); // Notification file removed.
		response.add(fileCount);
		NameValuePair archiveFileCount = new NameValuePair("/ARCHIVE/TOTAL_ARCHIVE_FILE_COUNT", String.valueOf(totalArchiveCount) );
		response.add(archiveFileCount);
		
	}
	
	private void addSuccessInArchiveResponse(int idx, ArrayList<NameValuePair> response,
			String caseNumber, ArrayList<String> relatedFiles) throws Exception{
		
		int i = 0;
		
		NameValuePair casePath = new NameValuePair("/ARCHIVE/CASEDETAILS["+String.valueOf(idx) +"]/CASENUMBER",caseNumber);
		response.add(casePath);
		NameValuePair archiveStatus = new NameValuePair("/ARCHIVE/CASEDETAILS["+String.valueOf(idx) +"]/ARCHIVESTATUS","SUCCESS");
		response.add(archiveStatus);
		
		for(String dataFile: relatedFiles) {
			if( dataFile.contains(caseNumber)  == true) {
				++i;
				response.add(new NameValuePair(("/ARCHIVE/CASEDETAILS["+String.valueOf(idx) +"]/CASEFILES/FILENAME["+ String.valueOf(i) +"]"),dataFile));
			}
			
		}
		response.add(new NameValuePair("/ARCHIVE/CASEDETAILS["+String.valueOf(idx) +"]/ERROR/FILENAME",""));
		response.add(new NameValuePair("/ARCHIVE/CASEDETAILS["+String.valueOf(idx) +"]/ERROR/CODE",""));
		response.add(new NameValuePair("/ARCHIVE/CASEDETAILS["+String.valueOf(idx) +"]/ERROR/DESC",""));
	}
	
	private void addFailureInArchiveResponse(int idx, ArrayList<NameValuePair> response,
			String caseNumber, ArrayList<String> relatedFiles, NameValuePair error) throws Exception{
			
			NameValuePair casePath = new NameValuePair("/ARCHIVE/CASEDETAILS["+String.valueOf(idx) +"]/CASENUMBER",caseNumber);
			response.add(casePath);
			NameValuePair archiveStatus = new NameValuePair("/ARCHIVE/CASEDETAILS["+String.valueOf(idx) +"]/ARCHIVESTATUS","FAIL");
			response.add(archiveStatus);
			int i = 1;
			for(String dataFile: relatedFiles) {
				response.add(new NameValuePair(("/ARCHIVE/CASEDETAILS["+String.valueOf(idx) +"]/CASEFILES/FILENAME["+ String.valueOf(i) +"]"),dataFile));
				++i;
			}
			response.add(new NameValuePair("/ARCHIVE/CASEDETAILS["+String.valueOf(idx) +"]/ERROR[1]/FILENAME",error.getName()));
			response.add(new NameValuePair("/ARCHIVE/CASEDETAILS["+String.valueOf(idx) +"]/ERROR[1]/CODE","400"));
			response.add(new NameValuePair("/ARCHIVE/CASEDETAILS["+String.valueOf(idx) +"]/ERROR[1]/DESC",error.getValue()));
	}
	
	private ArrayList<String> getRelatedFilesForCaseNumber(String caseNumber, ArrayList<String> files){
		ArrayList<String> relatedFiles = new ArrayList<String>();
		for(String f: files) {
			if(f.contains(caseNumber) == true) {
				relatedFiles.add(f);
			}
		}
		return relatedFiles;
	}
	
	private ArrayList<NameValuePair> getOrderedList(ArrayList<NameValuePair> data, ArrayList<NameValuePair> config){
		ArrayList<NameValuePair> orderedList = new ArrayList<NameValuePair>();
		for(NameValuePair n: config) {
			String xpath = removeAttr(n.getName());
			for(NameValuePair d: data) {
				if(xpath.equalsIgnoreCase(removeAttr(d.getName()))==true) {
					orderedList.add(d);
				}
			}
		}
		return orderedList;
	}

	private void copyFileUsingStream(File source, String opf) throws IOException {
	    InputStream is = null;
	    OutputStream os = null;
	    try {
	        is = new FileInputStream(source);
	        os = new FileOutputStream(opf + source.getName()); 
	       
	            IOUtils.copy(is, os);
	            System.out.println("File copied from");
	         }
	    catch(Exception e) {
	    	System.out.println("copyFileUsingStream: " + opf + e.getMessage());
	    }
	         finally {
	             IOUtils.closeQuietly(is);
	             IOUtils.closeQuietly(os);
	         }
	}
	
	private void copyFilesToOutDir(ArrayList<String> files, String caseNumber) throws Exception {
	
		// Create out directory
		String outdirectory = config.getOutputFolderPath() + caseNumber + File.separator;
		File od = new File(outdirectory);
		
		if(!od.exists()) {
			if(od.mkdir()) {
				System.out.println(outdirectory + "Successfully created");
			}
		}
	
		for(String f: files) {
			File file = new File(f);
			copyFileUsingStream(file,outdirectory);
			++totalArchiveCount;
		}
	}
	
	public String getCaseNumberFromFileName(String fileName) {
		String ret = null;
		String onlyFileName = (new File(fileName)).getName();
		int position = onlyFileName.indexOf(config.getDatafileNameIndicator());  // Search _case
		if(position>0) {
			ret = onlyFileName.substring(0, position);
		}
		
		return ret;
	}
	

	public String removeAttr(String input) {
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
	
	public ArrayList<NameValuePair> getXpathListFromXml(String fileName)throws Exception{
		ArrayList<NameValuePair> xpathList = new ArrayList<NameValuePair>();
		
		if(fileName.toLowerCase().contains(".xml") == false)return xpathList;
		
		try {
	   	SAXParserFactory spf = SAXParserFactory.newInstance();
        SAXParser sp = spf.newSAXParser();
        XMLReader xr = sp.getXMLReader();
        
        XMLParser fch = new XMLParser(xr);
         XMLParser.xpathList = xpathList;
        
        //xr.setContentHandler(new FragmentContentHandler(xr));
        xr.setContentHandler(fch);
        xr.parse(new InputSource(new FileInputStream(fileName)));
        
        /*
        for(NameValuePair xpath: xpathList) {
        	//System.out.println(xpath.getName() + "=" + xpath.getValue());
        }
        */
		}
		catch(Exception e) {
			System.out.println("Error in parsing XML file: " + fileName);
			throw e;
		}
        return xpathList;
	}
	
	public void XmlToXsd(String xmlFileName, String xsdFileName) throws Exception {
		
		final Inst2XsdOptions options = new Inst2XsdOptions();
	    options.setDesign(Inst2XsdOptions.DESIGN_VENETIAN_BLIND);
	    options.setSimpleContentTypes(Inst2XsdOptions.SIMPLE_CONTENT_TYPES_STRING);
	  
	    XmlObject[] xml = null;
		try {
			xml = new XmlObject[] {XmlObject.Factory.parse(new File(xmlFileName))};
			
		} catch (XmlException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
	    final SchemaDocument[] schemaDocs = Inst2Xsd.inst2xsd(xml, options);
	    //System.out.println(schemaDocs[0]);
	    PrintWriter out = new PrintWriter(xsdFileName);
		out.println(schemaDocs[0]);
		out.close();
		}
		catch(Exception e) {
			System.out.println("XmlToXsd: Exception.. " + xsdFileName );
		}
	    
	}

}

package XMLFileHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import com.p3.archon.AuditReport.ReportMain;
import com.p3.archon.jsonparser.JsonProcessor;
import com.p3.archon.xmlsipautomater.PackageMain;

public class Processor {

	private static ArrayList<NameValuePair> configXpathList = null;
	private XPath xpath = null;

	private DocumentBuilderFactory factory = null;
	private DocumentBuilder builder = null;
	private Document doc = null;
	private XPathFactory xpathfactory = null;
	private static XmlFileExtratorConfig config = null;

	private int totalFileCount = 0;
	private int totalArchiveCount = 0;
	private int totalCaseCount = 0;

	private ArrayList<NameValuePair> mandatoryXpathList = null;

	public String getJsonConfigAsXml(XmlFileExtratorConfig config) throws Exception{

		String xmlOutput = null;
		this.config = config;

		//Collate all configuration files into one file -- TODO: Revisit this logic
		ArrayList<String> fileList = config.getFileList(config.getConfigFolderPath());
		ArrayList<String> configFiles = new ArrayList<String>();
		for(String f: fileList) {
			if(f.toLowerCase().contains(".json")== true) {
				configFiles.add(f);
			}
		}
		if(configFiles.size() ==0 ) {	// Default.. directory always get's added
			System.out.println("Schema file for extraction is missing in folder " +config.getConfigFolderPath() );
			return xmlOutput; // TODO: throw exception
		}

		configXpathList = new ArrayList<NameValuePair>();
		for(String file: configFiles) {
			JsonProcessor.readJsonWithObjectMapper(file);
			ConfigFileParserJson xpath = new ConfigFileParserJson("FinalResult.json"); 

			for(NameValuePair s: xpath.getXpathList() ) {
				//System.out.println(s.getTopath());
				configXpathList.add(new NameValuePair(s.getTopath(),"DATA"));
			}

			//Create an xml document with collated xpath list
			xmlOutput = XPathUtils.createXML(configXpathList, "temp.xml");
		}
		//printXpath(configXpathList);
		return xmlOutput;

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
			File f = new File(fileName);
			String str = f.getName();
			System.out.println("Error in parsing XML file: " + str);
			throw e;
		}
		return xpathList;
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

	public void start()throws Exception {
		try {

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

			NotificationParser cases = new NotificationParser(config.getDataFolderPath() + config.getProcessTriggerFileName());
			cases.parseFile();

			int batch = 0;
			totalFileCount = 0;
			totalArchiveCount = 0;
			totalCaseCount = 0;

			//for(String file: files) {
			for(NotificationBean caseDtls: cases.getCaseList()) {

				ArrayList<NameValuePair> fileXpathList =new ArrayList<NameValuePair>();
				ArrayList<NameValuePair> dataXpathList =new ArrayList<NameValuePair>();
				boolean dataFileFound = false;
				String caseNumber = caseDtls.getCaseNumber();
				for(String file: caseDtls.getFiles()) {
					files.add(config.getDataFolderPath()+file);
					++totalFileCount;

					try {
						if(config.isDataFile(file,caseDtls.getCaseNumber())== true) {
							dataFileFound = true;
							++batch;
							relatedFiles = caseDtls.getFiles();
							ArrayList<NameValuePair>tempXpathList = parseAndExtractData(config.getDataFolderPath()+file);  // Based on configuration file, data is extracted.
							for(NameValuePair p: tempXpathList) {
								dataXpathList.add(p);
							}
						}
						else {
							System.out.println(file + "Skipping as it is not data file.");
							continue;
						}
					}
					catch(FileNotFoundException e) {
						System.out.println("Error in parsing xml file :" + file);
						File fn = new File(config.getDataFolderPath() + file);
						NameValuePair error = new NameValuePair(fn.getName(), fn.getName() +" File Not found in given location." );
						//Move the file and related file to error directory
						//copyFilesToOutDir(relatedFiles);
						addFailureInArchiveResponse(batch, finaleResponse, caseNumber, relatedFiles, caseDtls.getCaseType(), error, "404");
						break;
					}
					catch(Exception e) {
						System.out.println("Error in parsing xml file :" + file);
						File fn = new File(config.getDataFolderPath() + file);

						NameValuePair error = new NameValuePair(fn.getName(), "Error in parsing Xml:" +e.getMessage() );
						//Move the file and related file to error directory
						//copyFilesToOutDir(relatedFiles);
						addFailureInArchiveResponse(batch, finaleResponse, caseNumber, relatedFiles, caseDtls.getCaseType(), error, "400");
						break;
					}
				}
				if(dataFileFound == false) {
					++batch;
					//String caseNumber = caseDtls.getCaseNumber();
					relatedFiles = caseDtls.getFiles();
					NameValuePair error = new NameValuePair(caseNumber + "_case.xml", "Case file is missing in notification file list" );
					addFailureInArchiveResponse(batch, finaleResponse, caseNumber, relatedFiles, caseDtls.getCaseType(), error, "404");
					break;
				}
				else {
					fileXpathList = getOrderedList(dataXpathList, configXpathList);
					NameValuePair mandatoryErr = CheckMandatoryTags(fileXpathList);
					if( mandatoryErr != null) {
						System.out.println("Error in parsing xml file : " + caseNumber+ "_case.xml");
						File fn = new File(config.getDataFolderPath() + caseNumber + "_case.xml");
						NameValuePair error = new NameValuePair(fn.getName(), "Error in parsing xml file : " + mandatoryErr.getValue() );
						//Move the file and related file to error directory
						//copyFilesToOutDir(relatedFiles);
						addFailureInArchiveResponse(batch, finaleResponse, caseNumber, relatedFiles, caseDtls.getCaseType(), error, "400");
						continue;
					}
					else{
						boolean stat = true;
						for(String f: relatedFiles) {
							try {
								copyFileToOutDir(f, caseNumber);
							}
							catch(FileNotFoundException e) {
								System.out.println("Error in parsing xml file :" + f);
								File fn = new File(config.getDataFolderPath() + f);
								NameValuePair error = new NameValuePair(fn.getName(), fn.getName() +" File Not found in given location." );
								//Move the file and related file to error directory
								//copyFilesToOutDir(relatedFiles);
								addFailureInArchiveResponse(batch, finaleResponse, caseNumber, relatedFiles, caseDtls.getCaseType(), error, "404");
								stat = false;
							}
						}
						if(stat == true) {
							addSuccessInArchiveResponse(batch, finaleResponse, caseNumber, relatedFiles, caseDtls.getCaseType());
							addAttachmentTag(fileXpathList, relatedFiles);
							addToFinalXpathList(batch,fileXpathList, finalXpathList);
							createOutXmlFile(batch,fileXpathList, caseNumber);
							caseNumbers.add(caseNumber);
						}		
					}
				}
			}
			//createOutXmlFile(finalXpathList);
			createSip();
			addTotalFileCount(finaleResponse);
			createResponseFile(finaleResponse);
			renameProcessTriggerFile();

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
					System.out.println(outDir + "Successfully Processed");
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

	public ArrayList<NameValuePair> parseAndExtractData(String xmlFileName) throws Exception{
		ArrayList<NameValuePair> dataXpaths = getXpathListFromXml(xmlFileName);
		ArrayList<NameValuePair> unorderXpathList = new ArrayList<NameValuePair>();
		ConfigFileParserJson xpath = new ConfigFileParserJson("FinalResult.json");
		ArrayList<NameValuePair> xpathList = xpath.getXpathList();
		mandatoryXpathList = xpath.getMandatoryXPaths();
		String root = null;
		root = getRoot(xpathList.get(0).getTopath());

		for(NameValuePair data: dataXpaths) {
			NameValuePair toPath = getMatchingXpath(data, xpathList);

			if(toPath != null) {	
				System.out.println("From Path: " + data.getFrompath() );
				System.out.println("To Path: " + toPath.getTopath());

				String destPath = getNewToXpath(data.getName(), toPath.getTopath(), root);
				data.setName(destPath);

				String str = xpath.getDataType(data);
				if(str.equalsIgnoreCase(DATATYPE.DATETIME) == true) {
					//System.out.println("---" + data.getName() + ":" + data.getValue());
					String val = iaDateTimeFormat(data.getValue());
					data.setValue(val);
				}
				else if(str.equalsIgnoreCase(DATATYPE.DATE)==true) {
					String val = iaDateFormat(data.getValue());
					data.setValue(val);
				}
				unorderXpathList.add(data);
			}
		}

		return unorderXpathList; //TODO ; Replace with topath list.
	}

	private void addFailureInArchiveResponse(int idx, ArrayList<NameValuePair> response,
			String caseNumber, ArrayList<String> relatedFiles, String caseType, NameValuePair error, String errorCode) throws Exception{
		addFailureInArchiveResponse(idx,response,caseNumber,relatedFiles, caseType, error, errorCode, 1);
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

	public NameValuePair CheckMandatoryTags(ArrayList<NameValuePair> dataList) throws Exception	{
		NameValuePair error = null;

		if(mandatoryXpathList == null) {
			ConfigFileParserJson xpath = new ConfigFileParserJson("FinalResult.json"); 
			xpath.getXpathList();
			mandatoryXpathList = xpath.getMandatoryXPaths();
		}

		//TODO; Check for mandatory tags
		for(NameValuePair tag: mandatoryXpathList) {
			String str = removeAttr(tag.getName());
			boolean tagFound = false;
			for(NameValuePair data: dataList) {
				String dataPath = removeAttr(data.getName());
				System.out.println(dataPath);
				System.out.println(data.getValue());
				if(str.equals(dataPath)) {
					if(data.getValue().trim() != "") {
						tagFound = true;
					}
					break;
				}
			}
			if(tagFound == false) {
				error = new NameValuePair(tag.getName(), tag.getName() + " - Mandatory Element is not found in the input data file. ");
				break;
			}
		}

		return error;
	}

	private void addSuccessInArchiveResponse(int idx, ArrayList<NameValuePair> response,
			String caseNumber, ArrayList<String> relatedFiles, String caseType) throws Exception{

		int i = 0;

		NameValuePair casePath = new NameValuePair("/ArchiveResponse/CaseList/Case["+String.valueOf(idx) +"]/CaseNumber",caseNumber);
		response.add(casePath);
		NameValuePair caseTypePath = new NameValuePair("/ArchiveResponse/CaseList/Case[" +String.valueOf(idx)+"]/CaseType", caseType);
		response.add(caseTypePath);
		NameValuePair countPath = new NameValuePair("/ArchiveResponse/CaseList/Case["+String.valueOf(idx) +"]/Count",String.valueOf(relatedFiles.size()));
		response.add(countPath);

		totalArchiveCount += relatedFiles.size();
		++totalCaseCount;

		for(String dataFile: relatedFiles) {
			if( dataFile.contains(caseNumber)  == true) {
				++i;
				response.add(new NameValuePair(("/ArchiveResponse/CaseList/Case["+String.valueOf(idx) +"]/FileList/FileName["+ String.valueOf(i) +"]"),dataFile));
			}

		}
		NameValuePair archiveStatus = new NameValuePair("/ArchiveResponse/CaseList/Case["+String.valueOf(idx) +"]/Status","SUCCESS");
		response.add(archiveStatus);
		//response.add(new NameValuePair("/RESPONSE/CASE["+String.valueOf(idx) +"]/ERROR/FILENAME",""));
		//response.add(new NameValuePair("/RESPONSE/CASE["+String.valueOf(idx) +"]/ERROR/CODE",""));
		//response.add(new NameValuePair("/RESPONSE/CASE["+String.valueOf(idx) +"]/ERROR/DESC",""));
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

		//printXpath(finalXpathList);
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

	private void addTotalFileCount(ArrayList<NameValuePair> response) {
		NameValuePair fileCount = new NameValuePair("/ArchiveResponse/TotalFileCount", String.valueOf(totalFileCount) ); // Notification file removed.
		response.add(fileCount);
		NameValuePair archiveFileCount = new NameValuePair("/ArchiveResponse/SuccessFileCount", String.valueOf(totalArchiveCount) );
		response.add(archiveFileCount);
		NameValuePair caseCount = new NameValuePair("/ArchiveResponse/TotalCaseCount", String.valueOf(totalCaseCount));
		response.add(caseCount);
	}

	private void createResponseFile(ArrayList<NameValuePair> response) {
		//Get count of archive response file. 
		ArrayList<String> fileList = new ArrayList<String>();

		File responseFolder = new File(config.getResponseFolderPath());
		File[] files = responseFolder.listFiles();
		for(File file: files) {
			if(file.isFile() == true) {
				if(file.getName().toLowerCase().startsWith("ArchiveResponse") == true){
					fileList.add(responseFolder + file.getName());
				}
			}
		}
		String responseFileAbsPath = config.getResponseFolderPath() + "ArchiveResponse_"+files.length +".xml";
		XPathUtils.createXML(response,responseFileAbsPath);

		//Create PDF report for Audit purpose
		ReportMain.reportMain(responseFileAbsPath, config.getJobId(), config.getSipOutputFolderPath());

		//Copy notifiction file for audit purpose
		copyNotifyFileToSipFolder();
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

	public String getRoot(String xpath) {
		String[] configPathList = xpath.split("/");
		return  configPathList[1];  // index 0 will be null value
	}

	public NameValuePair getMatchingXpath(NameValuePair data, ArrayList<NameValuePair> xpathList){
		NameValuePair match = null;
		for(NameValuePair p: xpathList) {
			String dataXpath = removeAttr(data.getName());
			String configXpath = removeAttr(p.getName());
			if(dataXpath.equals(configXpath)==true) {
				match = p;
			}
		}
		return match;
	}

	public String getNewToXpath(String fromPath, String toPath, String root) {   

		String[] strs = fromPath.split("/");
		String res = "/"+ root;

		for(int i=2; i<strs.length;++i) {
			res = res + "/" + strs[i];			
		}

		if(toPath.equals(removeAttr(res))!= true) {
			res = toPath;
		}
		return res;
	}

	public static String iaDateTimeFormat(String date_vals){
		String IA_formatter_date = date_vals.substring(0,4) + "-" +date_vals.substring(4,6) + "-" + date_vals.substring(6,8)
		+ "T" + date_vals.substring(9,11) +":"+date_vals.substring(11,13) +":"+ date_vals.substring(13,19);
		return IA_formatter_date;
	}

	public static String iaDateFormat(String date_vals){
		String IA_formatter_date = date_vals.substring(0,4) + "-" +date_vals.substring(4,6) + "-" + date_vals.substring(6,8);									
		return IA_formatter_date;
	}

	private void addFailureInArchiveResponse(int idx, ArrayList<NameValuePair> response,
			String caseNumber, ArrayList<String> relatedFiles, String caseType, NameValuePair error, String errorCode, int errorCnt) throws Exception{
		String errIdx = String.valueOf(errorCnt);

		NameValuePair casePath = new NameValuePair("/ArchiveResponse/CaseList/Case["+String.valueOf(idx) +"]/CaseNumber",caseNumber);
		response.add(casePath);
		NameValuePair caseTypePath = new NameValuePair("/ArchiveResponse/CaseList/Case[" +String.valueOf(idx)+"]/CaseType", caseType);
		response.add(caseTypePath);
		NameValuePair countPath = new NameValuePair("/ArchiveResponse/CaseList/Case["+String.valueOf(idx) +"]/Count",String.valueOf(relatedFiles.size()));
		response.add(countPath);

		++totalCaseCount;

		int i = 1;
		for(String dataFile: relatedFiles) {
			response.add(new NameValuePair(("/ArchiveResponse/CaseList/Case["+String.valueOf(idx) +"]/FileList/FileName["+ String.valueOf(i) +"]"),dataFile));
			++i;
		}
		NameValuePair archiveStatus = new NameValuePair("/ArchiveResponse/CaseList/Case["+String.valueOf(idx) +"]/Status","FAIL");
		response.add(archiveStatus);
		response.add(new NameValuePair("/ArchiveResponse/CaseList/Case["+String.valueOf(idx) +"]/ErrorList/Error["+errIdx+"]/FileName",error.getName()));
		response.add(new NameValuePair("/ArchiveResponse/CaseList/Case["+String.valueOf(idx) +"]/ErrorList/Error["+errIdx+"]/Code",errorCode));
		response.add(new NameValuePair("/ArchiveResponse/CaseList/Case["+String.valueOf(idx) +"]/ErrorList/Error["+errIdx+"]/Desc",error.getValue()));
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
			throw e;
		}
		finally {
			IOUtils.closeQuietly(is);
			IOUtils.closeQuietly(os);
		}
	}

	private void copyNotifyFileToSipFolder() {
		try {

			File notifyFile = new File(config.getDataFolderPath() + config.getProcessTriggerFileName()); // By this time notification file name would have got renamed.
			copyFileUsingStream(notifyFile, config.getSipOutputFolderPath());
		}
		catch(Exception e) {
			System.out.println(e.getMessage());
		}
	}

	private void copyFileToOutDir(String file, String caseNumber) throws Exception {


		// Create out directory
		String outdirectory = config.getOutputFolderPath() + caseNumber + File.separator;
		File od = new File(outdirectory);

		if(!od.exists()) {
			if(od.mkdir()) {
				System.out.println(outdirectory + "Successfully created");
			}
		}

		File f = new File(config.getDataFolderPath() + file);
		copyFileUsingStream(f,outdirectory);

		/*
		for(String f: files) {
			File file = new File(config.getDataFolderPath() + f);
			copyFileUsingStream(file,outdirectory);

		}
		 */
	}

}

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
	public static String NOTIFICATIONFILE = "CFWSuccessNotification.xml";
	public static String DATAFILEIDENTIFIER = "_case.xml";

	public static void main(String[] args) {
		try {
			// Initial Validation
			String fileName = args[0];
			if ( fileName == null) {
				System.out.println("XMLFileExtractorConfig.json file is missing");
				return;
			}
			
			XmlFileExtratorConfig config = new XmlFileExtratorConfig(fileName);
			if(config.parseFile() == false)return;
			
			//Collate all configuration files into one file and 
			ArrayList<String> fileList = config.getFileList(config.getConfigFolderPath());
			ArrayList<String> configFiles = new ArrayList<String>();
			for(String f: fileList) {
				if(f.toLowerCase().contains(".xml")== true) {
					configFiles.add(f);
				}
			}
			if(configFiles.size() <1) {	// Default.. directory always get's added
				System.out.println("Schema file for extraction is missing in folder " +config.getConfigFolderPath() );
				return;
			}
			
			ArrayList<NameValuePair> configXpathList = new ArrayList<NameValuePair>();
			for(String file: configFiles) {
				
				//JsonProcessor.readJsonWithObjectMapper(file);
				ArrayList<NameValuePair> xpathList = getXpathListFromXml(file);
				
				for(NameValuePair xpath: xpathList) {
					configXpathList.add(xpath);
				}
			}
			
			//Create an xml document with collated xpath list
			String xml = XPathUtils.createXML(configXpathList, "temp.xml");
			//System.out.println(xml);
			/*
			PrintWriter out = new PrintWriter("temp.xml");
			out.println(xml);
			out.close();
			*/
			// Creating a dom object to search xpath during extraction.
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	        factory.setNamespaceAware(true); // never forget this!
	        DocumentBuilder builder = factory.newDocumentBuilder();
	        Document doc = builder.parse("temp.xml");
	 
	        XPathFactory xpathfactory = XPathFactory.newInstance();
	        XPath xpath = xpathfactory.newXPath();
	 
	        // Data Extraction
			ArrayList<String> directories = config.getDataDirectoryList();
			for(String directory: directories) {
		        ArrayList<NameValuePair> finalXpathList = new ArrayList<NameValuePair>();
		        ArrayList<NameValuePair> unorderXpathList = new ArrayList<NameValuePair>();
				
				ArrayList<String> files = config.getFileList(directory);
				ArrayList<String> fileNames = config.getFileNameList(directory);
				boolean readyForArchive = false;
				ArrayList<NameValuePair> errorList = new ArrayList<NameValuePair>();
				String caseNumber = null;
				for(String file: files) {
					
					if( isSuccessNotification(file) == true) {
						readyForArchive = true;
						caseNumber = getCaseNumber(file);
						
					}
				}
				
				// Skip the folders that are yet to be notified.
				if( caseNumber == null) {
					System.out.println("Either notification file not present or case number unavailable in directory" +directory );
					continue; //next directory
				}
				
				if(readyForArchive == true) {
					for(String file: files) {
						try {
						if(file.contains(NOTIFICATIONFILE) != true && file.contains(".xml")==true) {
							
							if(file.contains(DATAFILEIDENTIFIER)==true) {
								System.out.println(file + "being processed....");
								
								ArrayList<NameValuePair> dataXpaths = getXpathListFromXml(file);						
								for(NameValuePair data: dataXpaths) {
									String str = removeAttr(data.getName());
							        XPathExpression expr = xpath.compile(str);
							        
							        Object result = expr.evaluate(doc, XPathConstants.NODESET);
							        NodeList nodes = (NodeList) result;
							        if(nodes.getLength()>0) {
							        	unorderXpathList.add(data);
							        	//System.out.println(data.getName()+"=" + data.getValue());
							        }
								}							
								finalXpathList = getOrderedList(unorderXpathList, configXpathList);
								}
								
							}
							else {
								System.out.println(file + "Skipping as it is not data file.");
							}
						}
						catch(Exception e) {
							System.out.println("Error in parsing xml file :" + file);
							File fn = new File(file);
							NameValuePair error = new NameValuePair(fn.getName(), "Error in parsing Xml:" +e.getMessage() );
							errorList.add(error);
							continue;
						}
							// Create out directory
							String outdirectory = config.getOutputFolderPath() + caseNumber + config.getFilePathSeperator();
							File od = new File(outdirectory);
							
							if(!od.exists()) {
								if(od.mkdir()) {
									System.out.println(outdirectory + "Successfully created");
								}
							}
							
							if(file.contains(NOTIFICATIONFILE) != true && file.contains(".xml")==true) {
								//Copy file to destination only if no errors in processing all the files. 
								File source = new File(file);
								copyFileUsingStream(source,config.getOutputFolderPath()+caseNumber);
							}
							
						}
						
					}
				if(errorList.size() >0) {
					int i = 0;
					
					ArrayList<NameValuePair> response = new ArrayList<NameValuePair>();
					NameValuePair casePath = new NameValuePair("/ARCHIVE/CASEDETAILS/CASENUMBER",caseNumber);
					response.add(casePath);
					NameValuePair archiveStatus = new NameValuePair("/ARCHIVE/CASEDETAILS/ARCHIVESTATUS","FAIL");
					response.add(archiveStatus);
					
					for(String dataFile: fileNames) {
						if(dataFile.contains(NOTIFICATIONFILE) != true && dataFile.contains(".xml")==true) {
							++i;
							response.add(new NameValuePair(("/ARCHIVE/CASEDETAILS/CASEFILES/FILENAME["+ String.valueOf(i) +"]"),dataFile));
						}
						
					}
					int j = 0;
					for(NameValuePair error: errorList) {
						++j;
					response.add(new NameValuePair("/ARCHIVE/CASEDETAILS/ERROR["+String.valueOf(j)+"]/FILENAME",error.getName()));
					response.add(new NameValuePair("/ARCHIVE/CASEDETAILS/ERROR["+String.valueOf(j)+"]/CODE","400"));
					response.add(new NameValuePair("/ARCHIVE/CASEDETAILS/ERROR["+String.valueOf(j)+"]/DESC",error.getValue()));
					}
					// Archive response file				
					XPathUtils.createXML(response,config.getResponseFolderPath() + "ARCHIVE_RESPONSE" + caseNumber + ".xml");
					continue;
				}
				
				String outdirectory = config.getOutputFolderPath() + caseNumber + config.getFilePathSeperator();
				String outputFileName = outdirectory + "ARCHON_GEN_FILE.xml";
				String schemaFileName = config.getOutputFolderPath() + "pdi-schema.xsd";
				// Add file names in output xml
				String root= "/" + XPathUtils.getRootElement(finalXpathList.get(0).getName()) ;
				
				int i = 0;
				
				ArrayList<NameValuePair> response = new ArrayList<NameValuePair>();
				NameValuePair casePath = new NameValuePair("/ARCHIVE/CASEDETAILS/CASENUMBER",caseNumber);
				response.add(casePath);
				NameValuePair archiveStatus = new NameValuePair("/ARCHIVE/CASEDETAILS/ARCHIVESTATUS","SUCCESS");
				response.add(archiveStatus);
				
				for(String dataFile: fileNames) {
					if(dataFile.contains(NOTIFICATIONFILE) != true && dataFile.contains(".xml")==true) {
						++i;
						String attachmentPath = root + "[1]" + "/Attachments[1]/attachment[" + String.valueOf(i) + "]";
						String attachmentValue =dataFile;
						NameValuePair data = new NameValuePair(attachmentPath,attachmentValue);
						finalXpathList.add(data);
						response.add(new NameValuePair(("/ARCHIVE/CASEDETAILS/CASEFILES/FILENAME["+ String.valueOf(i) +"]"),dataFile));
					}
					
				}
				response.add(new NameValuePair("/ARCHIVE/CASEDETAILS/ERROR/FILENAME",""));
				response.add(new NameValuePair("/ARCHIVE/CASEDETAILS/ERROR/CODE",""));
				response.add(new NameValuePair("/ARCHIVE/CASEDETAILS/ERROR/DESC",""));
				
				
				//System.out.println(outputFileName);
				//Create output extracted file
				ArrayList<NameValuePair> xpathForXsd = new ArrayList<NameValuePair>();
				for(NameValuePair n: finalXpathList) {
					String name = "/RECORDs[1]/RECORD[1]" + n.getName();
					//System.out.println(name + "=" + "");
					xpathForXsd.add(new NameValuePair(name, ""));
				}
				XPathUtils.createXML(finalXpathList, outputFileName);
				
				//Generate schema file
				XPathUtils.createXML(configXpathList, "temp2.xml");
				XmlToXsd("temp2.xml", schemaFileName); // ROOTS/ROOT to be added for SIP
				
				
				//SIP Creation
				String folder = config.getOutputFolderPath();
				String holding = config.getHolding();
				String app = config.getAppName();
				String producer = config.getProducer();
				String entity = config.getSipentity();
				String schema = config.getSchema();
				String outputPath = config.getSipOutputFolderPath(); // sip folder path for output
				new PackageMain().start(folder, holding, app, producer, entity, schema, outputPath);
				
				// Archive response file				
				XPathUtils.createXML(response,config.getResponseFolderPath() + "ARCHIVE_RESPONSE" + caseNumber + ".xml");
				
				try {
				//Rename CFWSuccessNotification.xml file
				File file = new File(directory + NOTIFICATIONFILE);
				file.renameTo(new File(directory + "Archived.xml"));
				}
				catch(Exception e) {
					System.out.println("Exception in renaming CFWSuccessNotification.xml file.");
				}				
				
				System.out.println(caseNumber + "Success");
			}
			
		}
		catch(Exception e) {
			System.out.println(e.getMessage());
		}
	
	}
	private static ArrayList<NameValuePair> getOrderedList(ArrayList<NameValuePair> data, ArrayList<NameValuePair> config){
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

	private static void copyFileUsingStream(File source, String opf) throws IOException {
	    InputStream is = null;
	    OutputStream os = null;
	    try {
	        is = new FileInputStream(source);
	        os = new FileOutputStream(opf + File.separator + source.getName()); 
	       
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
	public static String getCaseNumber(String notificationFileName) throws Exception{
		String caseNumber = null;
		if(notificationFileName.contains(NOTIFICATIONFILE)== false) {
			return caseNumber;
		}
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true); // never forget this!
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(notificationFileName);
 
        XPathFactory xpathfactory = XPathFactory.newInstance();
        XPath xpath = xpathfactory.newXPath();
        XPathExpression expr = xpath.compile("/CFW/CASENUMBER/text()");
	
        Object result = expr.evaluate(doc, XPathConstants.NODESET);
        NodeList nodes = (NodeList) result;
        for (int i = 0; i < nodes.getLength(); i++) {
        	caseNumber = nodes.item(i).getNodeValue();
        }
	
		return caseNumber;
	}
	public static boolean isSuccessNotification(String notificationFileName) throws Exception {
		boolean readyForArchive = false;
		if(notificationFileName.contains(NOTIFICATIONFILE)== false) {
			return readyForArchive;
		}
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true); // never forget this!
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(notificationFileName);
 
        XPathFactory xpathfactory = XPathFactory.newInstance();
        XPath xpath = xpathfactory.newXPath();
        XPathExpression expr = xpath.compile("/CFW/READYFORARCHIVE/text()");
	
        Object result = expr.evaluate(doc, XPathConstants.NODESET);
        NodeList nodes = (NodeList) result;
        for (int i = 0; i < nodes.getLength(); i++) {
        	String str = nodes.item(i).getNodeValue();
        	if(str.compareToIgnoreCase("TRUE") == 0) {
        		readyForArchive = true;
        	}
        }
	
		return readyForArchive;
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
	
	public static void XmlToXsd(String xmlFileName, String xsdFileName) throws Exception {
		
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

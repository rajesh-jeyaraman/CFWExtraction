package com.p3.archon.AuditReport;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.XMLEvent;

import com.p3.archon.AuditReport.bean.ArchiveResponseBean;
import com.p3.archon.AuditReport.bean.CaseBean;
import com.p3.archon.AuditReport.bean.ErrorBean;

public class ArchiveResponseParser {
 
	private static final String ARCHIVERESPONSE = "ArchiveResponse";

	private static final String CASELIST = "CaseList";
	private static final String CASE = "Case";
	private static final String CASENUMBER = "CaseNumber";
	private static final String CASETYPE = "CaseType";

	private static final String COUNT = "Count";
	private static final String FILELIST = "FileList";
	private static final String FILENAME = "FileName";

	private static final String STATUS = "Status";

	private static final String ERRORLIST = "ErrorList";
	private static final String ERROR = "Error";
	private static final String CODE = "Code";
	private static final String DESCRIPTION = "Desc";

	private static final String TOTALFILECOUNT = "TotalFileCount";
	private static final String SUCCESSFILECOUNT = "SuccessFileCount";
	private static final String TOTALCASECOUNT = "TotalCaseCount";

	public static ArchiveResponseBean xmlParser(File file)
			throws UnsupportedEncodingException, FileNotFoundException, XMLStreamException {

		boolean archiveResonseFlag = false;

		boolean caseListFlag = false;
		boolean caseFlag = false;
		boolean caseNumberFlag = false;
		boolean caseTypeFlag = false;

		boolean countFlag = false;
		boolean fileListFlag = false;
		boolean fileNameFlag = false;
		boolean errorFileFlag = false;

		boolean statusFlag = false;
		boolean errorlistFlag = false;
		boolean errorFlag = false;
		boolean codeFlag = false;
		boolean descriptionFlag = false;

		boolean totalFilecountFlag = false;
		boolean successfilecountFlag = false;
		boolean totalcasecountFlag = false;

		List<CaseBean> caselist = new ArrayList<>();
		List<String> fileList = new ArrayList<>();
		List<ErrorBean> errorList = new ArrayList<>();
		Map<String, String> errorFileMap = new HashMap<>();
		
		ArchiveResponseBean arb = new ArchiveResponseBean();
		CaseBean cb = new CaseBean();
		ErrorBean eb = new ErrorBean();
		long successCaseCount = 0;
		String errorFile = null;
		String errorFileMsg = null;
		String caseNumber = null;

		XMLInputFactory factory = XMLInputFactory.newInstance();
		XMLEventReader reader = factory.createXMLEventReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
		while (reader.hasNext()) {
			XMLEvent event = reader.nextEvent();
			switch (event.getEventType()) {
			case XMLStreamConstants.START_ELEMENT:
				String startElement = event.asStartElement().getName().getLocalPart();
				switch (startElement) {
				case ARCHIVERESPONSE:
					archiveResonseFlag = true;
					break;
				case CASELIST:
					caselist = new ArrayList<>();
					if (archiveResonseFlag)
						caseListFlag = true;
					break;
				case CASE:
					cb = new CaseBean();
					if (caseListFlag)
						caseFlag = true;
					break;
				case CASENUMBER:
					if (caseFlag)
						caseNumberFlag = true;
					break;
				case CASETYPE:
					if (caseFlag)
						caseTypeFlag = true;
					break;
				case COUNT:
					if (caseFlag)
						countFlag = true;
					break;
				case FILELIST:
					fileList = new ArrayList<>();
					if (caseFlag)
						fileListFlag = true;
					break;
				case FILENAME:
					if (caseFlag && fileListFlag)
						fileNameFlag = true;
					else if (caseFlag && errorFlag)
						errorFileFlag = true;
					break;
				case STATUS:
					if (caseFlag)
						statusFlag = true;
					break;
				case ERRORLIST:
					errorFileMap = new HashMap<>();
					errorList = new ArrayList<>();
					if (caseFlag)
						errorlistFlag = true;
					break;
				case ERROR:
					eb = new ErrorBean();
					if (errorlistFlag)
						errorFlag = true;
					break;
				case CODE:
					if (errorFlag)
						codeFlag = true;
					break;
				case DESCRIPTION:
					if (errorFlag)
						descriptionFlag = true;
					break;
				case TOTALFILECOUNT:
					if (archiveResonseFlag)
						totalFilecountFlag = true;
					break;
				case SUCCESSFILECOUNT:
					if (archiveResonseFlag)
						successfilecountFlag = true;
					break;
				case TOTALCASECOUNT:
					if (archiveResonseFlag)
						totalcasecountFlag = true;
					break;
				}
				break;
			case XMLStreamConstants.CHARACTERS:
				Characters characters = event.asCharacters();
				if (archiveResonseFlag) {
					if (caseListFlag) {
						if (caseFlag) {
							if (caseNumberFlag) {
								caseNumber = characters.getData();
								cb.setCaseNumber(characters.getData());
							} else if (caseTypeFlag) {
								cb.setCaseType(characters.getData());
							} else if (countFlag) {
								cb.setCount(characters.getData());
							} else if (fileListFlag) {
								if (fileNameFlag) {
									fileList.add(characters.getData());
								}
							} else if (statusFlag) {
								cb.setStatus(characters.getData());
								if (characters.getData().equalsIgnoreCase("SUCCESS")) {
									successCaseCount++;
								}
							} else if (errorlistFlag) {
								if (errorFlag) {
									if (codeFlag) {
										eb.setCode(Integer.parseInt(characters.getData()));
									} else if (errorFileFlag) {
										eb.setFileName(characters.getData());
										errorFile = characters.getData();
									} else if (descriptionFlag) {
										eb.setErrorMessage(characters.getData());
										errorFileMsg = characters.getData();
									}
								}	
							}
						}
					} else if (totalFilecountFlag) {
						arb.setTotalFileCount(Long.parseLong(characters.getData()));
					} else if (successfilecountFlag) {
						arb.setSuccessFileCount(Long.parseLong(characters.getData()));
					} else if (totalcasecountFlag) {
						arb.setTotalCaseCount(Long.parseLong(characters.getData()));
					}
				}
				break;
			case XMLStreamConstants.END_ELEMENT:
				String endEleName = event.asEndElement().getName().getLocalPart();
				switch (endEleName) {
				case ARCHIVERESPONSE:
					archiveResonseFlag = false;
					break;
				case CASELIST:
					arb.setSuccessCaseCount(successCaseCount);
					arb.setCaseList(caselist);
					if (archiveResonseFlag)
						caseListFlag = false;
					break;
				case CASE:
					caselist.add(cb);
					if (caseListFlag)
						caseFlag = false;
					break;
				case CASENUMBER:
					if (caseFlag)
						caseNumberFlag = false;
					break;
				case CASETYPE:
					if (caseFlag)
						caseTypeFlag = false;
					break;
				case COUNT:
					if (caseFlag)
						countFlag = false;
					break;
				case FILELIST:
					cb.setFileList(fileList);
					if (caseFlag)
						fileListFlag = false;
					break;
				case FILENAME:
					if (caseFlag && fileListFlag)
						fileNameFlag = false;
					else if (caseFlag && errorFlag)
						errorFileFlag = false;
					break;
				case STATUS:
					if (caseFlag)
						statusFlag = false;
					break;
				case ERRORLIST:
					cb.setErrorFileMap(errorFileMap);
					cb.setErrorList(errorList);
					if (caseFlag)
						errorlistFlag = false;
					break;
				case ERROR:
					errorList.add(eb);
					errorFileMap.put(caseNumber+"/"+errorFile, errorFileMsg);
					if (errorlistFlag)
						errorFlag = false;
					break;
				case CODE:
					if (errorFlag)
						codeFlag = false;
					break;
				case DESCRIPTION:
					if (errorFlag)
						descriptionFlag = false;
					break;
				case TOTALFILECOUNT:
					if (archiveResonseFlag)
						totalFilecountFlag = false;
					break;
				case SUCCESSFILECOUNT:
					if (archiveResonseFlag)
						successfilecountFlag = false;
					break;
				case TOTALCASECOUNT:
					if (archiveResonseFlag)
						totalcasecountFlag = false;
					break;
				}
				break;
			}
		}
		return arb;
	}

}

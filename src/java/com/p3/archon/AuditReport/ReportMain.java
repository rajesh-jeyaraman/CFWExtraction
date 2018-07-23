package com.p3.archon.AuditReport;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

import javax.xml.stream.XMLStreamException;

import com.p3.archon.AuditReport.bean.ArchiveResponseBean;

public class ReportMain {
	
	public static void reportMain(String fileName, String uuid, String outputPath) {
		ArchiveResponseBean response = null;
		try {
			response = ArchiveResponseParser.xmlParser(new File(fileName));
		} catch (UnsupportedEncodingException | FileNotFoundException | XMLStreamException e) {
			e.printStackTrace();
		}

		Common com = new Common(uuid);
		com.generateReport(outputPath, response);
	}

}

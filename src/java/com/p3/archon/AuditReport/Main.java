package com.p3.archon.AuditReport;

public class Main {
	
	public static void main(String[] args) {

		String fileName = "/Users/omjigupta/Downloads/ArchiveResponse_18.xml";
		String outputPath = "/Users/omjigupta/output";
		String uuid = "a7633dd3-da9d-4288-a08b-d3d5546974de";

		ReportMain.reportMain(fileName, uuid, outputPath);

	}
}

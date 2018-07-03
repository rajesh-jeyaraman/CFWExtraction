package com.p3.archon.xmlsipautomater;

import java.io.File;
import java.io.IOException;

import com.p3.archon.xmlsipautomater.processor.PackageProcessor;

public class PackageMain {

	public static void sipmain(String[] args) throws IOException {

		String folder = "/Users/malik/Documents/CFW/SIPSAMPLE";
		String holding = "holding1";
		String app = "testapp";
		String producer = "archon";
		String entity = "test";
		String schema = "com:ia-xml-schema:1.0";
		String outputPath = "/Users/malik/Documents/CFW/SIPSAMPLEOutput";
		new PackageMain().start(folder, holding, app, producer, entity, schema, outputPath,"");
	}

	public void start(String folder, String holding, String app, String producer, String entity, String schema,
			String outputPath, String sipPrefix) throws IOException {
		new File(outputPath).mkdirs();
		PackageProcessor p = new PackageProcessor(folder, holding, app, producer, entity, schema, outputPath, sipPrefix);
		try {
			p.start(p.getBatchAssembler());
		} finally {
			p.getBatchAssembler().end();
		}
	}

}

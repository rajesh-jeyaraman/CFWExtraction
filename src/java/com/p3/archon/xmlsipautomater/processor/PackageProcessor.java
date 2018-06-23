package com.p3.archon.xmlsipautomater.processor;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.opentext.ia.sdk.sip.BatchSipAssembler;
import com.p3.archon.xmlsipautomater.helpers.Constants;
import com.p3.archon.xmlsipautomater.processor.bean.ArchonXml;
import com.p3.archon.xmlsipautomater.utilities.DirectoryWalker;

public class PackageProcessor extends SipCreator {

	private String basepath;

	public PackageProcessor(String folder, String holding, String app, String producer, String entity, String schema,
			String outputPath) {
		super(holding, app, producer, entity, schema, outputPath);
		this.basepath = folder;
	}

	public void start(BatchSipAssembler<ArchonXml> batchAssembler) throws IOException {
		List<String> folders = new DirectoryWalker().walkDir(basepath);
		for (String caseFolders : folders) {
			processCase(caseFolders, batchAssembler);
		}
	}

	private void processCase(String caseFolders, BatchSipAssembler<ArchonXml> batchAssembler) throws IOException {
		ArchonXml jr = new ArchonXml();
		File mainFile = null;
		for (String caseFile : new DirectoryWalker().walk(caseFolders)) {
			String file = new File(caseFile).getName();
			if (file.startsWith(Constants.MAIN_FILE_PREFIX))
				mainFile = new File(caseFile);
		}

		if (mainFile == null)
			return;

		jr.setFileContent(IOUtils.toString(new FileReader(mainFile)));
		jr.setBaseFolder(new File(caseFolders).getPath());

		List<String> attachments = getAttachments(caseFolders);
		jr.getAttachement().addAll(attachments);

		batchAssembler.add(jr);
	}

	private List<String> getAttachments(String caseFolders) throws IOException {
		List<String> attachments = new ArrayList<String>();
		for (String caseFile : new DirectoryWalker().walk(caseFolders)) {
			String file = new File(caseFile).getName();
			if (!file.startsWith(Constants.MAIN_FILE_PREFIX))
				attachments.add(file);
		}
		return attachments;
	}

}

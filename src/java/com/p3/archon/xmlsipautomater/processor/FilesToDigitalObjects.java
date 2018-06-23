package com.p3.archon.xmlsipautomater.processor;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Stream;


import com.opentext.ia.sdk.sip.DigitalObject;
import com.opentext.ia.sdk.sip.DigitalObjectsExtraction;
import com.p3.archon.xmlsipautomater.helpers.Constants;
import com.p3.archon.xmlsipautomater.processor.bean.ArchonXml;

public class FilesToDigitalObjects implements DigitalObjectsExtraction<ArchonXml> {

	@Override
	public Iterator<? extends DigitalObject> apply(ArchonXml jr) {
		File dir = new File(jr.getBaseFolder());
		Stream<File> list = Arrays.stream(dir.listFiles()).sorted((a, b) -> a.getName().compareTo(b.getName()));
		return list.filter(file -> !file.getName().startsWith(Constants.MAIN_FILE_PREFIX))
				.map(file -> DigitalObject.fromFile(file.getName(), file)).iterator();
	}
}

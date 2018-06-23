package com.p3.archon.xmlsipautomater.processor;

import java.io.File;
import java.net.URI;
import java.util.Map;

import com.opentext.ia.sdk.sip.BatchSipAssembler;
import com.opentext.ia.sdk.sip.ContentInfo;
import com.opentext.ia.sdk.sip.DefaultPackagingInformationFactory;
import com.opentext.ia.sdk.sip.OneSipPerDssPackagingInformationFactory;
import com.opentext.ia.sdk.sip.PackagingInformation;
import com.opentext.ia.sdk.sip.PackagingInformationFactory;
import com.opentext.ia.sdk.sip.PdiAssembler;
import com.opentext.ia.sdk.sip.SequentialDssIdSupplier;
import com.opentext.ia.sdk.sip.SipAssembler;
import com.opentext.ia.sdk.sip.SipSegmentationStrategy;
import com.opentext.ia.sdk.sip.XmlPdiAssembler;
import com.opentext.ia.sdk.support.io.FileSupplier;
import com.p3.archon.xmlsipautomater.helpers.Constants;
import com.p3.archon.xmlsipautomater.processor.bean.ArchonXml;

public class SipCreator {

	private BatchSipAssembler<ArchonXml> batchAssembler;

	public SipCreator(String holding, String app, String producer, String entity, String schema, String outputPath) {

		PackagingInformation prototype = PackagingInformation.builder().dss().holding(holding).application(app)
				.producer(producer).entity(entity).schema(schema).end().build();

		PackagingInformationFactory factory = new OneSipPerDssPackagingInformationFactory(
				new DefaultPackagingInformationFactory(prototype), new SequentialDssIdSupplier("ex6dss", 1));

		PdiAssembler<ArchonXml> pdiAssembler = new XmlPdiAssembler<ArchonXml>(URI.create(schema), "RECORD") {
			@Override
			protected void doAdd(ArchonXml jr, Map<String, ContentInfo> hashes) {
				getBuilder().xml(jr.getFileContent().replace("&amp;", "&amp;amp;").replace("&quot;", "&amp;quot;")
						.replace("&lt;", "&amp;lt;").replace("&gt;", "&amp;gt;").replace("&apos;", "&amp;apos;"));
			}
		};

		SipAssembler<ArchonXml> assembler = SipAssembler.forPdiAndContent(factory, pdiAssembler,
				new FilesToDigitalObjects());
		SipSegmentationStrategy<ArchonXml> segmentationStrategy = SipSegmentationStrategy
				.byMaxAius(Constants.SPLIT_SIZE);
		setBatchAssembler(new BatchSipAssembler<>(assembler, segmentationStrategy,
				FileSupplier.fromDirectory(new File(outputPath), Constants.SIP_PREFIX_NAME, Constants.ZIP)));
	}

	public BatchSipAssembler<ArchonXml> getBatchAssembler() {
		return batchAssembler;
	}

	public void setBatchAssembler(BatchSipAssembler<ArchonXml> batchAssembler) {
		this.batchAssembler = batchAssembler;
	}

}

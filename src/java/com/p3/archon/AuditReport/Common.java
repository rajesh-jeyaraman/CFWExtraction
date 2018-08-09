package com.p3.archon.AuditReport;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.xhtmlrenderer.pdf.ITextRenderer;

import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.p3.archon.AuditReport.bean.ArchiveResponseBean;
import com.p3.archon.AuditReport.bean.CaseBean;

public class Common {

	private static final String TEMP_AUDIT_FILE = "tempAudit.pdf";
	private static final String XML_AUDIT_REPORT_PREFIX = "Summary Report_";
	private static final String PDF_EXT = ".pdf";
	private static final String ENCODING = "UTF-8";

	private String uuid;

	public Common(String uuid) {
		this.uuid = uuid;
	}

	public void generateReport(String outputPath, ArchiveResponseBean arb) {
		StringBuffer sb = new StringBuffer();

		sb.append("<td colspan=\"1\" align=\"center\"><b>Total Case Count</b></td>");
		sb.append("<td colspan=\"1\" align=\"center\"><b>Success Case Count</b></td>");
		sb.append("<td colspan=\"1\" align=\"center\"><b>Total File Count</b></td>");
		sb.append("<td colspan=\"1\" align=\"center\"><b>Success File Count</b></td>");
		sb.append("</tr></thead><tbody>");

		sb.append("<tr>");
		sb.append("<td colspan=\"1\" align=\"center\">").append(arb.getTotalCaseCount()).append("</td>");
		sb.append("<td colspan=\"1\" align=\"center\">").append(arb.getSuccessCaseCount()).append("</td>");
		sb.append("<td colspan=\"1\" align=\"center\">").append(arb.getTotalFileCount()).append("</td>");
		sb.append("<td colspan=\"1\" align=\"center\">").append(arb.getSuccessFileCount()).append("</td>");
		sb.append("</tr>");

		sb.append("</tbody></table>");
		sb.append("<br></br>");

		sb.append("<b><font style=\"size:14px\">Case Summary</font></b><br></br><br></br>");
		sb.append("<table class=\"table table-bordered table-striped\"><thead><tr>");
		
		sb.append("<td colspan=\"1\" align=\"center\"><b>S.No.</b></td>");
		sb.append("<td colspan=\"1\" align=\"center\"><b>Case Number</b></td>");
		sb.append("<td colspan=\"1\" align=\"center\"><b>Case Type</b></td>");
		sb.append("<td colspan=\"1\" align=\"center\"><b>File List</b></td>");
		sb.append("<td colspan=\"1\" align=\"center\"><b>Status</b></td>");
		sb.append("<td colspan=\"1\" align=\"center\"><b>Remarks</b></td>");
		sb.append("</tr></thead><tbody>");

		int serialNumber = 1;
		for (CaseBean row : arb.getCaseList()) {
			for (String file :row.getFileList()) {
				sb.append("<tr>");
				sb.append("<td colspan=\"1\" align=\"center\">").append(serialNumber++).append("</td>");
				sb.append("<td colspan=\"1\" align=\"left\">").append(row.getCaseNumber()).append("</td>");
				sb.append("<td colspan=\"1\" align=\"left\">").append(row.getCaseType()).append("</td>");
				sb.append("<td colspan=\"1\" align=\"left\">").append(file).append("</td>");
				sb.append("<td colspan=\"1\" align=\"center\">").append(row.getStatus()).append("</td>");
				
				if (row.getErrorList() == null) {
					sb.append("<td colspan=\"1\" align=\"left\">").append("-").append("</td>");
				} else {
					if (row.getErrorFileMap().containsKey(row.getCaseNumber()+"/"+file)) {
						sb.append("<td colspan=\"1\" align=\"left\">").append(row.getErrorFileMap().get(row.getCaseNumber()+"/"+file)).append("</td>");
					} else {
						sb.append("<td colspan=\"1\" align=\"left\">").append("-").append("</td>");
					}
				}	
				sb.append("</tr>");
			}
			if (row.getErrorFileMap() != null) {
				for (String key : row.getErrorFileMap().keySet()) {
					if (!row.getFileList().contains(key.split("/")[1])) {
						sb.append("<tr>");
						sb.append("<td colspan=\"1\" align=\"center\">").append(serialNumber++).append("</td>");
						sb.append("<td colspan=\"1\" align=\"left\">").append(row.getCaseNumber()).append("</td>");
						sb.append("<td colspan=\"1\" align=\"left\">").append(row.getCaseType()).append("</td>");
						sb.append("<td colspan=\"1\" align=\"left\">").append(key.split("/")[1]).append("</td>");
						sb.append("<td colspan=\"1\" align=\"center\">").append(row.getStatus()).append("</td>");
						sb.append("<td colspan=\"1\" align=\"left\">").append(row.getErrorFileMap().get(key)).append("</td>");
						sb.append("</tr>");
					}
				}
			}	
		}
		
		sb.append("</tbody></table>");
		sb.append("<br></br>");

		sb = createHeader(sb);

		sb.append("<br></br>");
		sb.append("<br></br>");
		sb.append("<br></br>");
		sb.append("<br></br>");
		sb.append("<p>Job Reference Id : " + uuid + "</p>");
		sb.append("<p>Generated with Archon</p>");
		sb.append("<p>Generated Date : " + new Date() + "</p>");
		sb.append("<p><i><font size=\"8px\">This is a system generated report.</font></i></p>");
		sb.append("</body></html>");
		createAuditReport(sb, outputPath);
	}

	protected StringBuffer createHeader(StringBuffer sb) {
		StringBuffer sfb = new StringBuffer();
		sfb.append(
				"<html><head><style> @page {  size: 18in 12in; } table {  background-color: transparent;}caption {  padding-top: 8px;  padding-bottom: 8px;  color: #777;  text-align: left;}th {  text-align: left;}.table {  width: 100%;  max-width: 100%;  margin-bottom: 20px;}.table > thead > tr > th,.table > tbody > tr > th,.table > tfoot > tr > th,.table > thead > tr > td,.table > tbody > tr > td,.table > tfoot > tr > td {  padding: 8px;  line-height: 1.42857143;  vertical-align: top;  border-top: 1px solid #ddd;}.table > thead > tr > th {  vertical-align: bottom;  border-bottom: 2px solid #ddd;}.table > caption + thead > tr:first-child > th,.table > colgroup + thead > tr:first-child > th,.table > thead:first-child > tr:first-child > th,.table > caption + thead > tr:first-child > td,.table > colgroup + thead > tr:first-child > td,.table > thead:first-child > tr:first-child > td {  border-top: 0;}.table > tbody + tbody {  border-top: 2px solid #ddd;}.table .table {  background-color: #fff;}.table-condensed > thead > tr > th,.table-condensed > tbody > tr > th,.table-condensed > tfoot > tr > th,.table-condensed > thead > tr > td,.table-condensed > tbody > tr > td,.table-condensed > tfoot > tr > td {  padding: 5px;}.table-bordered {  border: 1px solid #ddd;}.table-bordered > thead > tr > th,.table-bordered > tbody > tr > th,.table-bordered > tfoot > tr > th,.table-bordered > thead > tr > td,.table-bordered > tbody > tr > td,.table-bordered > tfoot > tr > td {  border: 1px solid #ddd;}.table-bordered > thead > tr > th,.table-bordered > thead > tr > td {  border-bottom-width: 2px;}.table-striped > tbody > tr:nth-of-type(odd) {  background-color: #f9f9f9;}.table-hover > tbody > tr:hover {  background-color: #f5f5f5;}table col[class*=\"col-\"] {  position: static;  display: table-column;  float: none;}table td[class*=\"col-\"],table th[class*=\"col-\"] {  position: static;  display: table-cell;  float: none;}.table > thead > tr > td.active,.table > tbody > tr > td.active,.table > tfoot > tr > td.active,.table > thead > tr > th.active,.table > tbody > tr > th.active,.table > tfoot > tr > th.active,.table > thead > tr.active > td,.table > tbody > tr.active > td,.table > tfoot > tr.active > td,.table > thead > tr.active > th,.table > tbody > tr.active > th,.table > tfoot > tr.active > th {  background-color: #f5f5f5;}.table-hover > tbody > tr > td.active:hover,.table-hover > tbody > tr > th.active:hover,.table-hover > tbody > tr.active:hover > td,.table-hover > tbody > tr:hover > .active,.table-hover > tbody > tr.active:hover > th {  background-color: #e8e8e8;}.table > thead > tr > td.success,.table > tbody > tr > td.success,.table > tfoot > tr > td.success,.table > thead > tr > th.success,.table > tbody > tr > th.success,.table > tfoot > tr > th.success,.table > thead > tr.success > td,.table > tbody > tr.success > td,.table > tfoot > tr.success > td,.table > thead > tr.success > th,.table > tbody > tr.success > th,.table > tfoot > tr.success > th {  background-color: #dff0d8;}.table-hover > tbody > tr > td.success:hover,.table-hover > tbody > tr > th.success:hover,.table-hover > tbody > tr.success:hover > td,.table-hover > tbody > tr:hover > .success,.table-hover > tbody > tr.success:hover > th {  background-color: #d0e9c6;}.table > thead > tr > td.info,.table > tbody > tr > td.info,.table > tfoot > tr > td.info,.table > thead > tr > th.info,.table > tbody > tr > th.info,.table > tfoot > tr > th.info,.table > thead > tr.info > td,.table > tbody > tr.info > td,.table > tfoot > tr.info > td,.table > thead > tr.info > th,.table > tbody > tr.info > th,.table > tfoot > tr.info > th {  background-color: #d9edf7;}.table-hover > tbody > tr > td.info:hover,.table-hover > tbody > tr > th.info:hover,.table-hover > tbody > tr.info:hover > td,.table-hover > tbody > tr:hover > .info,.table-hover > tbody > tr.info:hover > th {  background-color: #c4e3f3;}.table > thead > tr > td.warning,.table > tbody > tr > td.warning,.table > tfoot > tr > td.warning,.table > thead > tr > th.warning,.table > tbody > tr > th.warning,.table > tfoot > tr > th.warning,.table > thead > tr.warning > td,.table > tbody > tr.warning > td,.table > tfoot > tr.warning > td,.table > thead > tr.warning > th,.table > tbody > tr.warning > th,.table > tfoot > tr.warning > th {  background-color: #fcf8e3;}.table-hover > tbody > tr > td.warning:hover,.table-hover > tbody > tr > th.warning:hover,.table-hover > tbody > tr.warning:hover > td,.table-hover > tbody > tr:hover > .warning,.table-hover > tbody > tr.warning:hover > th {  background-color: #faf2cc;}.table > thead > tr > td.danger,.table > tbody > tr > td.danger,.table > tfoot > tr > td.danger,.table > thead > tr > th.danger,.table > tbody > tr > th.danger,.table > tfoot > tr > th.danger,.table > thead > tr.danger > td,.table > tbody > tr.danger > td,.table > tfoot > tr.danger > td,.table > thead > tr.danger > th,.table > tbody > tr.danger > th,.table > tfoot > tr.danger > th {  background-color: #f2dede;}.table-hover > tbody > tr > td.danger:hover,.table-hover > tbody > tr > th.danger:hover,.table-hover > tbody > tr.danger:hover > td,.table-hover > tbody > tr:hover > .danger,.table-hover > tbody > tr.danger:hover > th {  background-color: #ebcccc;}.table-responsive {  min-height: .01%;  overflow-x: auto;}@media screen and (max-width: 767px) {  .table-responsive {    width: 100%;    margin-bottom: 15px;    overflow-y: hidden;    -ms-overflow-style: -ms-autohiding-scrollbar;    border: 1px solid #ddd;  }  .table-responsive > .table {    margin-bottom: 0;  }  .table-responsive > .table > thead > tr > th,  .table-responsive > .table > tbody > tr > th,  .table-responsive > .table > tfoot > tr > th,  .table-responsive > .table > thead > tr > td,  .table-responsive > .table > tbody > tr > td,  .table-responsive > .table > tfoot > tr > td {    white-space: nowrap;  }  .table-responsive > .table-bordered {    border: 0;  }  .table-responsive > .table-bordered > thead > tr > th:first-child,  .table-responsive > .table-bordered > tbody > tr > th:first-child,  .table-responsive > .table-bordered > tfoot > tr > th:first-child,  .table-responsive > .table-bordered > thead > tr > td:first-child,  .table-responsive > .table-bordered > tbody > tr > td:first-child,  .table-responsive > .table-bordered > tfoot > tr > td:first-child {    border-left: 0;  }  .table-responsive > .table-bordered > thead > tr > th:last-child,  .table-responsive > .table-bordered > tbody > tr > th:last-child,  .table-responsive > .table-bordered > tfoot > tr > th:last-child,  .table-responsive > .table-bordered > thead > tr > td:last-child,  .table-responsive > .table-bordered > tbody > tr > td:last-child,  .table-responsive > .table-bordered > tfoot > tr > td:last-child {    border-right: 0;  }  .table-responsive > .table-bordered > tbody > tr:last-child > th,  .table-responsive > .table-bordered > tfoot > tr:last-child > th,  .table-responsive > .table-bordered > tbody > tr:last-child > td,  .table-responsive > .table-bordered > tfoot > tr:last-child > td {    border-bottom: 0;  }}</style></head><body><center><b><font style=\"size:14px\">XML Extraction Report</font></b></center><br></br><br></br><table class=\"table table-bordered table-striped\"><thead><tr>");
		sfb.append(sb.toString());
		return sfb;
	}

	public void createAuditReport(StringBuffer sb, String path) {
		try {
			String inputfile = path + File.separator + "outAudit.html";
			String tempfile = path + File.separator + TEMP_AUDIT_FILE;
			String finalfile = path + File.separator + XML_AUDIT_REPORT_PREFIX + uuid.substring(0, 8) + PDF_EXT;
			PrintWriter writer = new PrintWriter(inputfile, ENCODING);
			writer.println(sb.toString());
			writer.close();

			String url = new File(inputfile).toURI().toURL().toString();
			OutputStream os = new FileOutputStream(new File(tempfile));
			ITextRenderer renderer = new ITextRenderer();
			renderer.setDocument(url);
			renderer.layout();
			renderer.createPDF(os);
			os.close();

			PdfReader pdfReader = new PdfReader(tempfile);
			PdfStamper pdfStamper = new PdfStamper(pdfReader, new FileOutputStream(new File(finalfile)));
			PdfContentByte content = pdfStamper.getUnderContent(pdfReader.getNumberOfPages());
			Image image = Image.getInstance("archon.png");
			image.scaleToFit(300f, 300f);
			image.setAbsolutePosition(950f, 20f);
			content.addImage(image);
			pdfStamper.close();
			pdfReader.close();

			FileUtils.forceDeleteOnExit(new File(inputfile));
			FileUtils.forceDeleteOnExit(new File(tempfile));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

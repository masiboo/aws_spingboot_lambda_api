package org.iprosoft.trademarks.aws.artefacts.service.reporter;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.iprosoft.trademarks.aws.artefacts.model.dto.ArtefactOutput;
import org.iprosoft.trademarks.aws.artefacts.model.dto.BatchOutput;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

@Slf4j
@Getter
public class ExtentReportManager {

	public static ExtentSparkReporter sparkReporter;

	private static ExtentReports report;

	private static String makeReportPath() {
		DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy--hh-mm-ss");
		String htmlReportName = "aws-chancellery-indexed-file-report-"
				+ dateFormat.format(Calendar.getInstance().getTime()) + ".html";
		log.info("htmlReportName: " + htmlReportName);
		String htmlReportPth = "/tmp" + File.separator + htmlReportName;
		log.info("htmlReportPth: " + htmlReportPth);
		return htmlReportPth;
	}

	public static File makeReport(List<BatchOutput> artefactBatchList) {
		configExtentSparkReporter();

		for (BatchOutput batchOutput : artefactBatchList) {
			ExtentTest test = report.createTest("BatchOutput ID: " + batchOutput.getId());

			// Add details to the report in a key-value format
			if (batchOutput.getBatchSequence() != null) {
				addKeyValueDetails(test, "BatchSequence", batchOutput.getBatchSequence());
			}
			if (batchOutput.getLockedDate() != null) {
				addKeyValueDetails(test, "LockedDate", batchOutput.getBatchSequence());
			}
			if (batchOutput.getCreationDate() != null) {
				addKeyValueDetails(test, "CreationDate", batchOutput.getCreationDate());
			}
			if (batchOutput.getLastModificationDate() != null) {
				addKeyValueDetails(test, "LastModificationDate", batchOutput.getLastModificationDate());
			}
			if (batchOutput.getStatus() != null) {
				addKeyValueDetails(test, "Status", batchOutput.getStatus());
			}
			if (batchOutput.getOperator() != null) {
				addKeyValueDetails(test, "Operator", batchOutput.getOperator().toString());
			}
			if (batchOutput.getLockedBy() != null) {
				addKeyValueDetails(test, "LockedBy", batchOutput.getLockedBy().toString());
			}
			if (batchOutput.getLastModUser() != null) {
				addKeyValueDetails(test, "LastModUser", batchOutput.getLastModUser().toString());
			}
			if (batchOutput.getRequestType() != null) {
				addKeyValueDetails(test, "RequestType", batchOutput.getRequestType());
			}
			if (batchOutput.getUser() != null) {
				addKeyValueDetails(test, "User", batchOutput.getUser());
			}
			if (batchOutput.getArtefacts() != null && !Objects.requireNonNull(batchOutput.getArtefacts()).isEmpty()) {
				addKeyValueDetails(test, "Artefacts", getPrettyJson(batchOutput.getArtefacts()));
			}
			if (batchOutput.getReportDate() != null) {
				addKeyValueDetails(test, "ReportDate", batchOutput.getReportDate());
			}
			if (batchOutput.getReportUrl() != null) {
				addKeyValueDetails(test, "ReportUrl", batchOutput.getReportUrl());
			}
		}
		// Write the report to the file
		report.flush();
		log.info("Report crated: " + sparkReporter.getFile().getAbsolutePath());
		File reportHtmlFile = null;
		try {
			reportHtmlFile = new File(modifyReport(sparkReporter.getFile().getAbsolutePath()));
			log.info("After modification done, time to prepare to return to caller IndexedFileReportGeneratorImpl.");
			if (Objects.requireNonNull(reportHtmlFile).createNewFile()) {
				reportHtmlFile.deleteOnExit();
				log.info("Report created successfully: " + reportHtmlFile.getAbsolutePath());
			}
			else {
				log.info("Report already exists in path: " + reportHtmlFile.getAbsolutePath());
				reportHtmlFile.deleteOnExit();
			}
		}
		catch (IOException e) {
			log.error("IOException when trying to create report: " + e.getMessage());
		}
		log.info("Return to caller IndexedFileReportGeneratorImpl.");
		return reportHtmlFile;
	}

	private static void configExtentSparkReporter() {
		sparkReporter = new ExtentSparkReporter(makeReportPath());
		sparkReporter.config().setTimelineEnabled(false);
		sparkReporter.config().setTheme(Theme.STANDARD);
		sparkReporter.config().setTimeStampFormat("yyyy-MM-dd_HH-mm-ss");
		sparkReporter.config().setJs("document.querySelectorAll('.timestamp').forEach(e => e.style.display = 'none');");
		report = new ExtentReports();
		report.attachReporter(sparkReporter);
	}

	private static String getPrettyJson(List<ArtefactOutput> artefacts) {
		ObjectMapper objectMapper = new ObjectMapper();

		// Enable pretty-printing
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

		// Convert the list of ArtefactOutput objects to JSON
		String prettyJson = null;
		try {
			prettyJson = objectMapper.writeValueAsString(artefacts);
		}
		catch (JsonProcessingException e) {
			log.error("objectMapper.writeValueAsString error: " + e.getMessage());
		}
		if (prettyJson != null) {
			prettyJson = "<pre>" + prettyJson + "</pre>";
		}
		return prettyJson;
	}

	private static void addKeyValueDetails(ExtentTest test, String key, String value) {
		test.info("<b>" + key + ":</b> " + value);
	}

	private static String modifyReport(String filePath) {
		try {
			File inputFile = new File(filePath);
			log.info("modifyReport found filePath: {}", inputFile.getAbsolutePath());
			Document doc = Jsoup.parse(inputFile, "UTF-8");
			// Remove the "Tags" section
			Elements categoryContainers = doc.select("div.category-container");
			for (Element element : categoryContainers) {
				log.info("div.category-container element removed: {}", element);
				element.remove();
			}

			// Remove the Log events section
			Elements logEventContainers = doc.select("div.col-md-6 > div.card:has(h6.card-title:contains(Log events))");
			for (Element element : logEventContainers) {
				Objects.requireNonNull(element.parent()).remove();
			}

			// Remove Author section
			Element authorRowDiv = doc.select("div.row:has(p:contains(Author))").first();
			if (authorRowDiv != null) {
				log.info("div.row:has(p:contains(Author)) element removed: {}", authorRowDiv);
				authorRowDiv.remove();
			}

			// Remove the "Tests" section
			Elements testsContainers = doc.select("div.card:has(h6.card-title:contains(Tests))");
			for (Element element : testsContainers) {
				log.info("div.card:has(h6.card-title:contains(Tests)) element removed: {}", authorRowDiv);
				element.remove();
			}

			// Remove the "Tests Passed" section
			Elements testsPassedContainers = doc
				.select("div.col-md-3 > div.card > div.card-body:has(p.text-pass:contains(Tests Passed))");
			for (Element element : testsPassedContainers) {
				element.parent().parent().remove();
			}

			// Remove the "Tests Failed" section
			Elements testsFailedContainers = doc
				.select("div.col-md-3 > div.card > div.card-body:has(p.text-fail:contains(Tests Failed))");
			for (Element element : testsFailedContainers) {
				element.parent().parent().remove();
			}

			// Remove the Timestamp column
			Elements tables = doc.select("table");
			for (Element table : tables) {
				Elements headers = table.select("th");
				int timestampIndex = -1;
				for (int i = 0; i < headers.size(); i++) {
					if (headers.get(i).text().equalsIgnoreCase("Timestamp")) {
						timestampIndex = i;
						headers.get(i).remove(); // Remove the header
						break;
					}
				}

				// Remove the cells in the "Timestamp" column
				if (timestampIndex != -1) {
					Elements rows = table.select("tr");
					for (Element row : rows) {
						Elements cells = row.select("td");
						if (cells.size() > timestampIndex) {
							cells.get(timestampIndex).remove();
						}
					}
				}
			}

			// Remove specific nav-item dropdown elements for category and author
			Elements navItems = doc.select(".nav-item.dropdown");
			for (Element item : navItems) {
				if (item.attr("onclick").contains("category-view") || item.attr("onclick").contains("author-view")) {
					item.remove();
				}
			}

			// Remove the Status column and all "info" data
			for (Element table : tables) {
				// Find the index of the "Status" column
				Elements headers = table.select("th");
				int statusIndex = -1;
				for (int i = 0; i < headers.size(); i++) {
					if (headers.get(i).text().equalsIgnoreCase("Status")) {
						statusIndex = i;
						headers.get(i).remove(); // Remove the header
						break;
					}
				}

				// Remove the cells in the "Status" column
				if (statusIndex != -1) {
					Elements rows = table.select("tr");
					for (Element row : rows) {
						Elements cells = row.select("td");
						if (cells.size() > statusIndex) {
							cells.get(statusIndex).remove();
						}
					}
				}
			}

			// Remove <li><a href="#"><span class="font-size-14">Tests</span></a></li>
			Elements testItems = doc.select("li:has(a:has(span.font-size-14:contains(Tests)))");
			for (Element item : testItems) {
				item.remove();
			}

			// Change text "Pass" to "Info" in <span class="badge pass-bg log
			// float-right">
			Elements passSpans = doc.select("span.badge.pass-bg.log.float-right");
			for (Element span : passSpans) {
				if (span.text().equals("Pass")) {
					span.text("Info");
				}
			}

			// Remove <i class="fa fa-exclamation-circle"></i>, <i class="fa
			// fa-user"></i>, and <i class="fa fa-tag"></i>
			Elements iconsToRemove = doc.select("i.fa.fa-exclamation-circle, i.fa.fa-user, i.fa.fa-tag");
			for (Element icon : iconsToRemove) {
				icon.remove();
			}

			// Change all occurrences of Details to REPORT DETAILS
			Elements thElements = doc.select("th.details-col");
			for (Element thElement : thElements) {
				thElement.text("REPORT DETAILS");
			}

			// Remove all extra <span> elements
			Elements badges = doc.select("span.badge.badge-default");
			for (Element badge : badges) {
				badge.remove();
			}
			// Remove all extra <span> elements
			Elements uriAnchors = doc.select("span.uri-anchor.badge.badge-default");
			for (Element uriAnchor : uriAnchors) {
				uriAnchor.remove();
			}

			// Write the modified HTML back to a file
			FileWriter writer = new FileWriter(filePath);
			writer.write(doc.html());
			writer.close();
			log.info("Removed extra sections from report successfully: " + filePath);
		}
		catch (IOException e) {
			log.error("IOException: " + e.getMessage());
		}
		log.info("Modification done successfully return: {}", filePath);
		return filePath;
	}

}

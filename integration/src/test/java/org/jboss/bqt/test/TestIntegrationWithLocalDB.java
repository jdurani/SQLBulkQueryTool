/*
 * JBoss, Home of Professional Open Source.
 * See the COPYRIGHT.txt file distributed with this work for information
 * regarding copyright ownership.  Some portions may be licensed
 * to Red Hat, Inc. under one or more contributor license agreements.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301 USA.
 */
package org.jboss.bqt.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import junit.framework.Assert;

import org.jboss.bqt.client.TestClient;
import org.jboss.bqt.core.util.FileUtils;
import org.jboss.bqt.core.util.UnitTestUtil;
import org.jboss.bqt.framework.ConfigPropertyNames;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


@SuppressWarnings("nls")
public class TestIntegrationWithLocalDB {
	
	

	@BeforeClass
    public static void beforeEach() throws Exception {  		
		System.setProperty("project.data.path", UnitTestUtil.getTestDataPath());
		
		System.setProperty("output.dir", UnitTestUtil.getTestOutputPath() + File.separator + "sqltest" );
		
		System.setProperty(ConfigPropertyNames.CONFIG_FILE, UnitTestUtil.getTestDataPath() + File.separator + "localconfig.properties");		
		
    }

	@Test
	public void testBQTClientExecutionResultSetModeSQL() {
		
		System.setProperty("result.mode", "sql" );
		
		TestClient tc = new TestClient();
		tc.runTest();
	
	}

	@Test
	public void testBQTClientExecutionResultSetModeGenerate() {
		
		System.setProperty("result.mode", "generate" );
		
		TestClient tc = new TestClient();
		tc.runTest();
		
		String outputdir = System.getProperty("output.dir");
		File other = new File(outputdir + File.separator + "h2_scenario" + File.separator + "generate" + File.separator +
				"h2_queries" + File.separator + "expected_results" + File.separator + "h2_other_scenario");

		assertFalse("No expected results should have been generated for these types of queriest", other.exists());
	}

	@Test
	public void testBQTClientExecutionResultSetModeCompare() {
		
		System.setProperty("result.mode", "compare" );
		
		TestClient tc = new TestClient();
		tc.runTest();

		String outputdir = System.getProperty("output.dir");
		
		File compareErrors = new File(outputdir + File.separator + "h2_scenario" + File.separator + "errors_for_compare" );
		
		File[] errorFiles = FileUtils.findAllFilesInDirectory(compareErrors.getAbsolutePath());
		
		assertTrue("Compare Has Error Files", (errorFiles == null || errorFiles.length == 0) );
		
	
	}	

	/**
	 * Compare failure messages in XML error files created in COMPARE mode.
	 *
	 * @param expectedErrorXml expected .err XML file
	 * @param errorXml actual .err XML file
	 * @throws SAXException one of the files is not valid XML
	 * @throws IOException one of the files cannot be read or does not exist
	 */
	private void compareErrorFiles(File expectedErrorXml, File errorXml) throws SAXException, IOException {
		try {
			Document expectedDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(expectedErrorXml);
			Document actualDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(errorXml);

			// list of messages inside the error file, same number = can compare
			NodeList nListExpected = expectedDoc.getElementsByTagName("failureMessage");
			NodeList nListActual = actualDoc.getElementsByTagName("failureMessage");
			assertTrue("Different number of failure messages", nListExpected.getLength() == nListActual.getLength());

			for (int i = 0; i < nListExpected.getLength(); i++) {
				Node nodeExpected = nListExpected.item(i);
				Node nodeActual = nListActual.item(i);
				assertEquals("Different failure message", nodeExpected.getTextContent(),
						nodeActual.getTextContent());
			}
		} catch (ParserConfigurationException e) {
			throw new Error(e);
		}
	}

	/**
	 * Negative test-case to ensure that:
	 * <li>the error XML files are created, well-formed and contains correct failure messages</li>
	 * <li>the comparison engine is correctly detecting multiple test-case failures</li>
	 *
	 * @throws SAXException in case of malformed XML
	 * @throws IOException error reading the file, or the file does not exist
	 */
	@Test
	public void testBQTClientExecutionResultSetModeCompareNo() throws SAXException, IOException {
		// different scenario
		System.setProperty(ConfigPropertyNames.CONFIG_FILE,
				UnitTestUtil.getTestDataPath() + File.separator + "localconfig_error.properties");

		System.setProperty("result.mode", "compare");

		TestClient tc = new TestClient();
		tc.runTest();

		// list of actual error files
		String outputdir = System.getProperty("output.dir");
		File compareErrors = new File(
				outputdir + File.separator + "h2_error_scenario" + File.separator + "errors_for_compare");

		File[] errorFiles = FileUtils.findAllFilesInDirectoryHavingExtension(compareErrors.getAbsolutePath(), ".err");
		Arrays.sort(errorFiles);

		// list of expected error files
		File expectedFailures = new File(
				UnitTestUtil.getTestDataPath() + File.separator + "query_sets" + File.separator + "h2_error_queries"
				+ File.separator + "expected_failures");

		File[] expectedErrorFiles = FileUtils.findAllFilesInDirectoryHavingExtension(expectedFailures.getAbsolutePath(),
				".err");
		Arrays.sort(expectedErrorFiles);

		// loaded and same number, we can compare one by one
		assertTrue("No error files created", errorFiles != null && errorFiles.length > 0);
		assertTrue("No expected error files", expectedErrorFiles != null && expectedErrorFiles.length > 0);
		assertEquals("Different number of error files", expectedErrorFiles.length, errorFiles.length);

		for (int i = 0; i < expectedErrorFiles.length; i++) {
			compareErrorFiles(expectedErrorFiles[i], errorFiles[i]);
		}
	}

	@Test
	public void testBQTClientExecutionResultSetModeNone() {
		
		System.setProperty("result.mode", "none" );
		
		TestClient tc = new TestClient();
		tc.runTest();
	
	}	
}

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
package org.jboss.bqt.client.resultmode;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.jboss.bqt.client.TestProperties;
import org.jboss.bqt.client.api.ExpectedResults;
import org.jboss.bqt.client.api.ExpectedResultsReader;
import org.jboss.bqt.client.api.QueryScenario;
import org.jboss.bqt.client.api.QueryWriter;
import org.jboss.bqt.core.exception.FrameworkException;
import org.jboss.bqt.core.exception.MultiTestFailedException;
import org.jboss.bqt.core.exception.QueryTestFailedException;
import org.jboss.bqt.framework.TestCase;
import org.jboss.bqt.framework.TestResult;
import org.jboss.bqt.framework.TransactionAPI;
import org.teiid.core.util.ArgCheck;

/**
 * The Compare Result Mode controls the process for comparing actual results against the expected
 * results.
 *  
 * @author vhalbert
 *
 */
public class Compare extends QueryScenario {

	/**
	 * @param scenarioName
	 * @param queryProperties
	 */
	public Compare(String scenarioName, Properties queryProperties) {
		super(scenarioName, queryProperties);

	}	
	
	@Override
	public boolean isCompare() {
		return true;
	}
	
	@Override
	public String getResultsMode()
	{
		return TestProperties.RESULT_MODES.COMPARE;
	}
	
	@Override
	public synchronized QueryWriter getQueryWriter() {
		return null;
	}

	/**
	 * @throws QueryTestFailedException 
	 * @throws  FrameworkException
	 *
	 */
	@Override
	public void handleTestResult(TestCase testCase, TransactionAPI transaction) throws FrameworkException,
			QueryTestFailedException {

		List<ExpectedResultsReader> readers = this.getExpectedResultsReaders(testCase);

		ArgCheck.isNotNull(readers);
		ArgCheck.isTrue(readers.size() > 0, "No Expected Results Readers");

		TestResult tr = testCase.getTestResult();

		for (ExpectedResultsReader reader : readers) {
			ExpectedResults es = reader.getExpectedResults(testCase.getActualTest());

			List<Throwable> exs = new ArrayList<Throwable>();
			try {
				reader.compareResults(testCase, transaction, es, isOrdered(tr.getQuery()));
			} catch (MultiTestFailedException mtf) {
				tr.setFailureMessage(mtf.getMessage());
				for(Throwable t : mtf.getFailures()) {
					exs.add(t);
				}
			} catch (QueryTestFailedException qtf) {
				tr.setFailureMessage(qtf.getMessage());
				exs.add(qtf);
			}

			if (exs.size() > 0) {
				// there was some failure to the test
				tr.setStatus(TestResult.RESULT_STATE.TEST_EXCEPTION);

				// create an error file that also contains the expected results
				if (es.getExpectedResultsFile() == null) {
					getErrorWriter().generateErrorFile(tr, exs);
				} else {
					getErrorWriter().generateErrorFile(testCase, es, transaction, exs);
				}

				// in case of exception with more failures, generate additional file with all messages
				if (exs.size() > 1) {
					getErrorWriter().generateErrorMessagesFile(tr, exs);
				}
			}
		}
	}
	
	private boolean isOrdered(String sql) {
		if (sql == null) return false;

		if (sql.toLowerCase().indexOf(" order by ") > 0) {
			return true;
		}
		return false;

	}
}

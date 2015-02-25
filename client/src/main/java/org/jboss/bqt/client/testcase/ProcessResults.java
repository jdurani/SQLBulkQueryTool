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

package org.jboss.bqt.client.testcase;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import org.jboss.bqt.client.ClientPlugin;
import org.jboss.bqt.client.QuerySQL;
import org.jboss.bqt.client.QueryTest;
import org.jboss.bqt.client.TestProperties;
import org.jboss.bqt.client.TestResultsSummary;
import org.jboss.bqt.client.api.QueryScenario;
import org.jboss.bqt.core.exception.FrameworkException;
import org.jboss.bqt.core.exception.FrameworkRuntimeException;
import org.jboss.bqt.core.exception.QueryTestFailedException;
import org.jboss.bqt.framework.AbstractQuery;
import org.jboss.bqt.framework.ConfigPropertyLoader;
import org.jboss.bqt.framework.FrameworkPlugin;
import org.jboss.bqt.framework.TestCase;
import org.jboss.bqt.framework.TestCaseLifeCycle;
import org.jboss.bqt.framework.TestResult;
import org.jboss.bqt.framework.TransactionAPI;
import org.jboss.bqt.framework.connection.ConnectionStrategyFactory;
import org.jboss.bqt.framework.util.AssertResults;

/**
 * ProcessResults is a TestCase that will process the results of a query test.  
 * For which the QueryScenario will be asked to handle any results generated
 * by the test case.
 * 
 */
public class ProcessResults implements TestCaseLifeCycle {

	private QueryScenario scenario = null;
	
	private TransactionAPI trans;
	
	private AbstractQuery abQuery;

	public ProcessResults(QueryScenario scenario) {
		super();
		this.scenario = scenario;
		
	}
	
	public String getTestName() {
		return scenario.getQuerySetName() + ":" + scenario.getQueryScenarioIdentifier();
	}
	
	
	public void setup(TransactionAPI transaction) {
		this.trans = transaction;
		abQuery = ((AbstractQuery) trans);
		
	}
	
	public void runTestCase() {
		
		Exception scenarioFailException = null;
		boolean next = true;
		
		//------- ping -------
		scenarioFailException = pingDS(scenario.getQueryScenarioIdentifier());
		next = scenarioFailException == null;
		//------- end ping -------
		
		//------- scenario limit -------
		//only one limit per scenario
		String timeForOneQueryProp = ConfigPropertyLoader.getInstance().getProperty(TestProperties.TIME_FOR_ONE_QUERY);
		int timeForOneQuery;
		if(timeForOneQueryProp == null || timeForOneQueryProp.isEmpty()){
			timeForOneQuery = -1;
			FrameworkPlugin.LOGGER.warn("Time for one query is not set [scenario: {}].", scenario.getQueryScenarioIdentifier());
		} else {
			try{
				timeForOneQuery = Integer.parseInt(timeForOneQueryProp);
			} catch (NumberFormatException ex){
				FrameworkPlugin.LOGGER.warn("Unparsable time for one query [scenario: {}, time: {}].", scenario.getQueryScenarioIdentifier(), timeForOneQueryProp);
				timeForOneQuery = -1;
			}
		}
		
		//Get number of queries
		int numOfQueries = 0;
		if(timeForOneQuery > 0){
			for(String qsid : scenario.getQuerySetIDs()){
				numOfQueries += scenario.getQueries(qsid).size();
			}
		}
		//------- end scenario limit -------
		Iterator<String> qsetIt = scenario.getQuerySetIDs().iterator();
		
		TestResultsSummary summary = this.scenario.getTestResultsSummary();

		FrameworkRuntimeException fre = null;
		
		try {

			long expectedEndTime = timeForOneQuery < 0 ? -1l : (((long)timeForOneQuery) * numOfQueries + System.currentTimeMillis());
			// iterate over the query set ID's, which there
			// should be 1 for each file to be processed
			while (qsetIt.hasNext() && next) {
				String querySetID = null;
				querySetID = qsetIt.next();

				ClientPlugin.LOGGER.info("Start TestResult:  QuerySetID [{}]", querySetID);

				final List<QueryTest> queryTests = scenario.getQueries(querySetID);

				// the iterator to process the query tests
				Iterator<QueryTest> queryTestIt = queryTests.iterator();

				long beginTS = System.currentTimeMillis();

				while (queryTestIt.hasNext() && next) {
					QueryTest q = queryTestIt.next();
					
					TestResult testResult = new TestResult(q.getQuerySetID(), q.getQueryID());
					
					TestCase testcase = new TestCase(q);
					testcase.setTestResult(testResult);
					
					ClientPlugin.LOGGER.debug("Test: QuerySetID [{} - {}]", testResult.getQuerySetID(), testResult.getQueryID());

					testResult.setResultMode(this.scenario.getResultsMode());
					testResult.setStatus(TestResult.RESULT_STATE.TEST_PRERUN);
					
					try {
						abQuery.before(testcase);

						if(expectedEndTime >=0 
								&& expectedEndTime < System.currentTimeMillis()){ //TODO - test it
							next = false;
							throw new FrameworkRuntimeException(FrameworkException.ErrorCodes.SCENARIO_ABORTED,
									"Scenario aborted - maximum time exceeded.");
						}
						
						executeTest(testcase);
					} catch (QueryTestFailedException qtfe) {
						// dont set on testResult, handled in transactionAPI
						
					} catch (Exception rme) {
						if (ClientPlugin.LOGGER.isDebugEnabled()) {
							rme.printStackTrace();
						}
						abQuery.setApplicationException(rme);
						
						if(rme instanceof FrameworkRuntimeException){
							String code = ((FrameworkRuntimeException) rme).getCode();
							if(FrameworkException.ErrorCodes.SERVER_CONNECTION_EXCEPTION.equals(code)
									|| FrameworkException.ErrorCodes.DB_CONNECTION_EXCEPTION.equals(code)
									|| FrameworkException.ErrorCodes.SCENARIO_ABORTED.equals(code)){
								next = false;
								scenarioFailException = rme;
							}
						}
					} finally {
						abQuery.after();
					}
						
					after(testcase);
					
					trans.cleanup();
					if(Thread.currentThread().isInterrupted()){
						ClientPlugin.LOGGER.info("Thread has been interrupted.");
						next = false;
						scenarioFailException = new FrameworkRuntimeException(FrameworkException.ErrorCodes.BQT_INTERRUPTED, "BQT thread has been interrupted.");
					}
				
				}

				long endTS = System.currentTimeMillis();

				ClientPlugin.LOGGER.info("End TestResult: QuerySetID [{}]", querySetID);

				try {
					summary.printResults(querySetID, beginTS, endTS);
				} catch (Exception e) {
					fre = new FrameworkRuntimeException(e);
					throw fre;
				}

			}

		} finally {
			try {
				summary.printTotals(numOfQueries);
				if(scenarioFailException != null){
					summary.printServerConnectionException(scenarioFailException);
				}
				summary.cleanup();
			} catch (Exception e) {
				if (fre == null) {
					throw new FrameworkRuntimeException(e);
				}
				throw fre;

			}
		}
		if(scenarioFailException != null){
			throw new FrameworkRuntimeException(scenarioFailException);
		}
	}
	
	public void cleanup() {
		if (trans != null) {
			trans.cleanup();
		}
		
		trans = null;

		this.scenario = null;
	}


	public void executeTest(TestCase testcase) throws Exception {
		
		QueryTest test = (QueryTest) testcase.getActualTest();
		TestResult testResult = testcase.getTestResult();
		
		QuerySQL[] queries =  test.getQueries();
		
		int l = queries.length;
		
		boolean resultModeNone = scenario.isNone();
	
		
		// multiple queries cannot be processed as a single result
		// therefore, only the NONE result mode is valid
		if (l > 1) {
			resultModeNone = true;
			testResult.setResultMode(TestProperties.RESULT_MODES.NONE);
			if (!scenario.isNone()) {
				ClientPlugin.LOGGER.info("Overriding ResultMode to NONE, multiple queries for QueryID [" + testResult.getQueryID() + "]");
			}

		} 

		for (int i = 0; i < l; i++) {
			QuerySQL qsql = queries[i];
			testResult.setQuery(qsql.getSql());
			
			// if runtimes or rowcounts are greater than 1, then no expected results will
			// be processed, therefore, resultmode is set to NONE for this query
			if ( !resultModeNone && (qsql.getRunTimes() > 1 || qsql.getRowCnt() > 0)) {
				resultModeNone = true;
				testResult.setResultMode(TestProperties.RESULT_MODES.NONE);
				ClientPlugin.LOGGER.info("Overriding ResultMode to NONE due to runtimes or rowcount for QueryID [" + testResult.getQueryID() + "]");
			}
			
			ClientPlugin.LOGGER.debug("Expecting - ID: " + test.getQuerySetID() + "  -  "
					+ test.getQueryID() + "ResultMode: " + (resultModeNone ? "NONE" : scenario.getResultsMode()) + ", numtimes: " +
					qsql.getRunTimes() + " rowcount: "  + qsql.getRowCnt() + " updatecnt: " + 
					qsql.getUpdateCnt());
						
			for (int r = 0; r < qsql.getRunTimes(); r++) {

				abQuery.execute(testResult.getQuery(), qsql.getParms(), qsql.getPayLoad());
				// check for NONE first, because it can be changed based on conditions
				// NOTE: isSQL() isn't processed in this class and therefore isn't looked for
				if (resultModeNone) {
						if (qsql.getRowCnt() >= 0) {
							testResult.setRowCount(abQuery.getRowCount());					
							AssertResults.assertRowCount(testResult, qsql.getRowCnt());
						} else if (qsql.getUpdateCnt() >= 0) {
							AssertResults.assertUpdateCount(testResult, qsql.getUpdateCnt());
						}		

				} else if (scenario.isCompare()) {
					// no rowcount or update counts can be checked in compare
					// because if comparing expected results, the count will be done at that time
					// if its an update, then no expected results would exist, and therefore,
					// the NONE option should be used for update checks.
					
				} else if (scenario.isGenerate()) {
					// do nothing
				}	
			}			
		}		
	}
	
	private void after(TestCase testcase) {
		
		FrameworkRuntimeException lastT = null;
		try {
			
			if (testcase.getTestResult().isFailure()) {
				testcase.getTestResult().setStatus(TestResult.RESULT_STATE.TEST_EXCEPTION);
			} else {
				testcase.getTestResult().setStatus(TestResult.RESULT_STATE.TEST_SUCCESS);
			}
	
			// if the test was NOT originally resultMode = NONE, but was changed because 
			// of certain conditions, then need to handle the test results if an error occurs
			if (testcase.getTestResult().getResultMode().equalsIgnoreCase(TestProperties.RESULT_MODES.NONE) && 
					! this.scenario.isNone()) {
				if (testcase.getTestResult().getStatus() == TestResult.RESULT_STATE.TEST_EXCEPTION) {
						this.scenario.getErrorWriter().generateErrorFile(testcase, null, (TransactionAPI) null, testcase.getTestResult().getException());
				}
			} else {
				this.scenario.handleTestResult(testcase, trans);
			}
						
			this.scenario.getTestResultsSummary().addTest(testcase.getTestResult().getQuerySetID(), testcase.getTestResult());

		} catch (FrameworkRuntimeException t) {
			lastT = t;
		} catch (Exception t) {
			t.printStackTrace();
			lastT = new FrameworkRuntimeException(t.getMessage());
		} finally {
		
		// call at the end to close resultset and statements
			if (lastT != null) throw lastT;
		}

	}
	
	private Exception pingDS(String scenario){
		String pingQuery = ConfigPropertyLoader.getInstance().getProperty(TestProperties.PING_QUERY);
		if(pingQuery == null || pingQuery.isEmpty()){
			FrameworkPlugin.LOGGER.warn("Ping-query not set [scenario: {}]", scenario);
			return null;
		}
		Connection con;
		try{
			con = ConnectionStrategyFactory.createConnectionStrategy().getConnection();
		} catch (FrameworkException ex){
			return new FrameworkRuntimeException(ex);
		}
		try{
			FrameworkPlugin.LOGGER.debug("Trying ping-query {} [scenario {}]", pingQuery, scenario);
			con.prepareStatement(pingQuery).execute();
		} catch (SQLException ex){
			return new FrameworkRuntimeException(ex, FrameworkException.ErrorCodes.PING_QUERY_FAILED,
					"Ping-query did not succeed [scenario : " + scenario + ", query: " + pingQuery + "].");
		} finally {
			try{
				con.close();
			} catch (SQLException ex){
				//ignore
			}
		}
		return null;
	}

}








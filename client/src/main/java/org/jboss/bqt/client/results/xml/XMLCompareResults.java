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

package org.jboss.bqt.client.results.xml;

import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.jboss.bqt.client.ClientPlugin;
import org.jboss.bqt.client.QueryTest;
import org.jboss.bqt.client.TestProperties;
import org.jboss.bqt.client.api.ExpectedResults;
import org.jboss.bqt.client.results.ExpectedResultsHolder;
import org.jboss.bqt.client.util.ListNestedSortComparator;
import org.jboss.bqt.client.xml.TagNames;
import org.jboss.bqt.core.exception.FrameworkRuntimeException;
import org.jboss.bqt.core.exception.MultiTestFailedException;
import org.jboss.bqt.core.exception.QueryTestFailedException;
import org.jboss.bqt.core.util.ExceptionUtil;
import org.jboss.bqt.core.util.ObjectConverterUtil;
import org.jboss.bqt.framework.ConfigPropertyLoader;
import org.jboss.bqt.framework.TestCase;
import org.jboss.bqt.framework.TestResult;
import org.teiid.core.util.Base64;

public class XMLCompareResults {
	private static String newline = System.getProperty("line.separator"); //$NON-NLS-1$
	
	private static double exceed_percent = -0.99999;
	private static long exec_minumin_time = -1;
	private static BigDecimal allowedDivergence = null;
	private static boolean allowedDivergenceIsZero = false;

	private XMLCompareResults(Properties props) {
		
		String exceed_per = props.getProperty(TestProperties.PROP_EXECUTE_EXCEED_PERCENT);
		String exec_min = props.getProperty(TestProperties.PROP_EXECUTE_TIME_MINEMUM);
		
		
		if (exceed_per != null && exceed_per.trim().length() > 0) {
			ClientPlugin.LOGGER.debug(" ======== " + TestProperties.PROP_EXECUTE_EXCEED_PERCENT + " is set to " + exceed_per);
			exceed_percent =  Double.parseDouble(exceed_per);		
		}
		
		if (exec_min != null && exec_min.trim().length() > 0) {
			ClientPlugin.LOGGER.debug(" ======== " + TestProperties.PROP_EXECUTE_EXCEED_PERCENT + " is set to " + exceed_per);
			exec_minumin_time =  Long.parseLong(exec_min);
		}		
		// if exceed percent was set and exec time was not, set exec time to minimum of 1 mil
		if (exceed_percent > 0 && exec_minumin_time < 0) exec_minumin_time = 1;
	
	}

	
	public static XMLCompareResults create(Properties properties) {
		return new XMLCompareResults(properties);
	}
	

	/**
	 * Compare the results of a query with those that were expected.
	 * @param testcase 
	 * @param expResults 
	 * @param resultSet 
	 * @param isOrdered 
	 * 
	 * @throws QueryTestFailedException
	 *             If comparison fails.
	 */
	public void compareResults(final TestCase testcase, ExpectedResults expResults,
			final ResultSet resultSet, final boolean isOrdered) throws QueryTestFailedException {
		
		final String eMsg = "CompareResults Error: "; //$NON-NLS-1$
		
//		if (expResults.isExceptionExpected()) {
//			testcase.getTestResult().setStatus(TestResult.RESULT_STATE.TEST_EXPECTED_EXCEPTION);
//		}

		ExpectedResultsHolder expectedResults = (ExpectedResultsHolder) expResults;
		ExpectedResultsHolder actualResults = null;

		switch (testcase.getTestResult().getStatus()) {
		case TestResult.RESULT_STATE.TEST_EXCEPTION:

			if (!expResults.isExceptionExpected()) {
				
				throw new QueryTestFailedException(testcase.getTestResult().getException(),
				eMsg
						+ "TestResult resulted in unexpected exception " + testcase.getTestResult().getFailureMessage()); //$NON-NLS-1$
				
			}
//		case TestResult.RESULT_STATE.TEST_EXPECTED_EXCEPTION:
//
//			if (!expectedResults.isExceptionExpected()) {
//				// The actual exception was expected, but the expected results
//				// was not
//				throw new QueryTestFailedException(
//						eMsg
//								+ "The actual result was an exception, but the Expected results wasn't an exception.  Actual exception: '" //$NON-NLS-1$
//								+ testcase.getTestResult().getExceptionMsg() + "'"); //$NON-NLS-1$
//			}
			
			testcase.getTestResult().setStatus(TestResult.RESULT_STATE.TEST_EXPECTED_EXCEPTION);
			// We got an exception that we expected - convert actual exception
			// to ResultsHolder
			actualResults = new ExpectedResultsHolder(TagNames.Elements.EXCEPTION, (QueryTest) testcase.getActualTest());
	
			actualResults = convertException(testcase.getTestResult().getException(), actualResults);
			actualResults.setExecutionTime(testcase.getTestResult().getExecutionTime());
			
//			// DEBUG:
//			ClientPlugin.LOGGER.info("*** EXPECTED EXC: Expected Results (holder): " +
//			 expectedResults);
//		//	 DEBUG:
//			 ClientPlugin.LOGGER.info("*** EXPECTED EXC: Actual Results (ResultSet): " +
//			 actualResults);

			compareExceptions(actualResults,  expectedResults, eMsg);

			break;

		default:

			// Convert results to ResultsHolder
			actualResults = new ExpectedResultsHolder(TagNames.Elements.QUERY_RESULTS, (QueryTest) testcase.getActualTest());
			actualResults.setExecutionTime(testcase.getTestResult().getExecutionTime());

//			// DEBUG:
//			ClientPlugin.LOGGER.info("*** 2 Expected Results (holder): " +
//			 expectedResults);
//		//	 DEBUG:
//			 ClientPlugin.LOGGER.info("*** 2 Actual Results (ResultSet): " +
//			 actualResults);
			
			convertResults(resultSet, testcase.getTestResult().getUpdateCount(), actualResults);

			if (expResults.isExceptionExpected()) {
				throw new QueryTestFailedException(eMsg + expectedResults.getExceptionClassName()
						+ " expected but not thrown, returned " + actualResults.getRows().size() + " rows.");
			}

			if (expectedResults.getRows().size() > 0) {
				compareResults(testcase, actualResults, expectedResults, eMsg, isOrdered);
			} else if (actualResults.getRows().size() > 0) {
				throw new QueryTestFailedException(
						eMsg + "Expected results indicated no results, but actual shows " + actualResults.getRows().size() + " rows."); //$NON-NLS-1$	      		    		      		    
			} else if (expectedResults.getUpdCount() > -1){
			    // update count
			    if(expectedResults.getUpdCount() != actualResults.getUpdCount()){
			        throw new QueryTestFailedException("Expected update count: " + expectedResults.getUpdCount() + ", actual update count: "
			                + actualResults.getUpdCount() + ".");
			    }
			}

			// DEBUG:
			// debugOut.println("*** Actual Results (holder): " +
			// actualResults);

			// Compare expected results with actual results, record by record

			break;

		}
	}

	/**
	 * The use of REMOVE_PREFIX is a hack to strip down the message so that it matches
	 * what is currently in use.
	 * @param actualException 
	 * @param actualResults 
	 * @return ResultsHolder
	 */
	
	private static ExpectedResultsHolder convertException(final Throwable actualException,
			final ExpectedResultsHolder actualResults) {
		actualResults.setExceptionClassName(actualException.getClass()
				.getName());
		
		actualResults.setExceptionMsg(ExceptionUtil.getExceptionMessage(actualException));
		
		return actualResults;
	}


	/**
	 * Helper to convert results into records and record first batch response
	 * time.
	 * 
	 * @param results
	 * @param batchSize
	 * @param resultsHolder
	 *            Modified - results added by this method.
	 * @return List of sorted results.
	 * @throws QueryTestFailedException
	 *             replaced SQLException.
	 */
	private static final long convertResults(final ResultSet results,
			final long batchSize, ExpectedResultsHolder resultsHolder)
			throws QueryTestFailedException {

		long firstBatchResponseTime = 0;
		final List<List<Object>> records = new ArrayList<List<Object>>();
		final List<String> columnTypeNames = new ArrayList<String>();
		final List<String> columnTypes = new ArrayList<String>();

		final ResultSetMetaData rsMetadata;
		final int colCount;

		if(results == null){
		    resultsHolder.setUpdCount((int)batchSize);
		} else {
			// Get column info
			try {
				rsMetadata = results.getMetaData();
				colCount = rsMetadata.getColumnCount();
				// Read types of all columns
				for (int col = 1; col <= colCount; col++) {
					columnTypeNames.add(rsMetadata.getColumnName(col));
					columnTypes.add(rsMetadata.getColumnTypeName(col));
				}
			} catch (SQLException qre) {
				throw new QueryTestFailedException(qre,
						"Can't get results metadata: " + qre.getMessage()); //$NON-NLS-1$
			}
	
			// Get rows
			try {
				// Read all the rows
				for (int row = 0; results.next(); row++) {
					final List<Object> currentRecord = new ArrayList<Object>(colCount);
					// Read values for this row
					for (int col = 1; col <= colCount; col++) {
						currentRecord.add(results.getObject(col));
					}
					records.add(currentRecord);
					// If this row is the (fetch size - 1)th row, record first batch
					// response time
					if (row == batchSize) {
						firstBatchResponseTime = System.currentTimeMillis();
					}
				}
			} catch (SQLException qre) {
				throw new QueryTestFailedException(qre,
						"Can't get results: " + qre.getMessage()); //$NON-NLS-1$
			}
			// Set info on resultsHolder
			resultsHolder.setRows(records);
			resultsHolder.setIdentifiers(columnTypeNames);
			resultsHolder.setTypes(columnTypes);
		}
		return firstBatchResponseTime;
	}
	
	/**
	 * Added primarily for public access to the compare code for testing.
	 * @param testCase 
	 * 
	 * @param actualResults
	 * @param expectedResults
	 * @param eMsg
	 * @param isOrdered
	 * @throws QueryTestFailedException
	 */
	private static void compareResults(final TestCase testCase, final ExpectedResultsHolder actualResults,
			final ExpectedResultsHolder expectedResults, final String eMsg,
			boolean isOrdered) throws QueryTestFailedException {
		// if (actualResults.isException() && expectedResults.isException()) {
		// // Compare exceptions
		// compareExceptions(actualResults, expectedResults, eMsg);
		// } else

		// if (actualResults.isResult() && expectedResults.isResult()) {
		// Compare results
	    final List<List<Object>> originalExpectedRows = cloneList(expectedResults.getRows());
	    final List<List<Object>> originalActualRows = cloneList(actualResults.getRows());
		if (isOrdered == false && actualResults.hasRows()
				&& expectedResults.hasRows()) {
			// If the results are not ordered, we can sort both
			// results and expected results to compare record for record
			// Otherwise, actual and expected results are already assumed
			// to be in same order

			// sort the sortedResults in ascending order
			final List<List<Object>> actualRows = actualResults.getRows();
			sortRecords(actualRows, true);
			actualResults.setRows(actualRows);

			// sort the expectedResults with ascending order
			final List<List<Object>> expectedRows = expectedResults.getRows();
			sortRecords(expectedRows, true);
			expectedResults.setRows(expectedRows);
		}
		try{
    		compareResultSets(actualResults.getRows(), originalActualRows,
    		        actualResults.getTypes(), actualResults.getIdentifiers(),
    		        expectedResults.getRows(), originalExpectedRows,
    				expectedResults.getTypes(), expectedResults.getIdentifiers(),
    				eMsg);
	    } finally {
            expectedResults.setRows(originalExpectedRows);
            actualResults.setRows(originalActualRows);
	    }
		
		long a = actualResults.getExecutionTime();
		long e = expectedResults.getExecutionTime();

		
		if (exec_minumin_time > 0 && e > exec_minumin_time &&  a > e) {
				double allowediff = e * (exceed_percent / 100);
				ClientPlugin.LOGGER.info("EXEC MIN TIME: " + exec_minumin_time + "  EXEC PER: " + exceed_percent + "  expected exec time: " + expectedResults.getExecutionTime());
				ClientPlugin.LOGGER.info("   expected exec time: " + e + "  actual exec time: " + a);
				if ( ( a - allowediff) > e ) {
					String msg = "Actual: " + a + " Expected: " + e + 
					" Diff: " + (a - e) +
					" Allowed %: " + (exceed_percent / 100) + " (" + allowediff + ")";
					QueryTestFailedException f = new QueryTestFailedException(eMsg
							+ msg) ; //$NON-NLS-1$
					
					testCase.getTestResult().setException(f);
					testCase.getTestResult().setFailureMessage(msg);
					testCase.getTestResult().setStatus(TestResult.RESULT_STATE.TEST_EXECUTION_TIME_EXCEEDED_EXCEPTION);
				}			
		
		}
	}

	private static <T> List<T> cloneList(List<T> list){
	    if(list == null){
	        return null;
	    }
	    return new ArrayList<T>(list);
	    
	}
	
	/**
	 * sort one result that is composed of records of all columns
	 * @param records 
	 * @param ascending 
	 */
	private static void sortRecords(List<List<Object>> records, boolean ascending) {
		// if record's size == 0, don't need to sort
		if (records.size() != 0) {
			int nFields = records.get(0).size();
			int[] params = new int[  ( nFields > 3 ? 3 : nFields ) ];
			for (int k = 0, j = 0; k < params.length; k++, j++) {
				params[j] = k;

			}
			if (nFields > 0) {
				Collections.sort(records, new ListNestedSortComparator(
					params, ascending));
			}
		}
	}

	private static void compareExceptions(final ExpectedResultsHolder actualResults,
			final ExpectedResultsHolder expectedResults, String eMsg)
			throws QueryTestFailedException {

		final String expectedExceptionClass = expectedResults
				.getExceptionClassName();
		final String expectedExceptionMsg = expectedResults.getExceptionMsg()
				.toLowerCase();
		final String actualExceptionClass = actualResults
				.getExceptionClassName();
		final String actualExceptionMsg = actualResults.getExceptionMsg()
				.toLowerCase();

		if (actualExceptionClass == null) {
			// We didn't get an actual exception, we should have
			throw new QueryTestFailedException(eMsg + "Expected exception: " //$NON-NLS-1$
					+ expectedExceptionClass + " but got none."); //$NON-NLS-1$
		}
		// Compare exception classes
		if (!expectedExceptionClass.equals(actualExceptionClass)) {
			throw new QueryTestFailedException(eMsg
					+ "Got wrong exception, expected \"" //$NON-NLS-1$
					+ expectedExceptionClass + "\" but got \"" + //$NON-NLS-1$
					actualExceptionClass + "\""); //$NON-NLS-1$
		}
		// Compare exception messages
		if (expectedResults.isExceptionContains() ) {
			if (actualExceptionMsg.indexOf(expectedExceptionMsg) > -1) {
				throw new QueryTestFailedException(
						eMsg
								+ "Expected exception message " + expectedExceptionMsg + " is not contained in actual exception of " + actualExceptionMsg); //$NON-NLS-1$				
			}
			
		} else if (expectedResults.isExceptionStartsWith()) {
			if (!actualExceptionMsg.startsWith(expectedExceptionMsg)) {
				throw new QueryTestFailedException(
						eMsg
								+ "Actual exception message " + actualExceptionMsg + " does not start with the expected exception of " + expectedExceptionMsg); //$NON-NLS-1$				
			}
		} else if (expectedResults.isExceptionRegex()) {
			try {
				Pattern p = Pattern.compile(expectedExceptionMsg, Pattern.DOTALL);
				if (!p.matcher(actualExceptionMsg).find()) {
					throw new QueryTestFailedException(eMsg + "Actual exception message " + actualExceptionMsg
							+ " does not match regex pattern " + expectedExceptionMsg);
				}
			} catch (PatternSyntaxException e) {
				throw new FrameworkRuntimeException(eMsg + "Invalid exception message regex pattern: " + e.getMessage());
			}
		} else {
			if (!expectedExceptionMsg.equals(actualExceptionMsg)) {

				// Give it another chance by comparing w/o line separators
				if (!compareStrTokens(expectedExceptionMsg, actualExceptionMsg)) {
					throw new QueryTestFailedException(
							eMsg
									+ "Got expected exception but with wrong message. Got " + actualExceptionMsg); //$NON-NLS-1$
				}
			}
		}
	}

	private static boolean compareStrTokens(String expectedStr, String gotStr) {
		String[] expectedTokens = StringUtils.split(expectedStr, newline);
		String[] gotTokens = StringUtils.split(gotStr, newline);
        
        if (expectedTokens.length != gotTokens.length)
            return false;  
		
		for (int i = 0; i < expectedTokens.length; i++) {
			String expected = expectedTokens[i];
			String got = gotTokens[i];
			if (!expected.equals(got)) {
				return false;
			}
		}
		return true;
	}

	private static void compareResultColumn(Object actualValue, Object expectedValue, int row, int col,
	        int actualResultRow, int expectedResultRow, final String eMsg) throws QueryTestFailedException {

	    actualResultRow += 1;
	    expectedResultRow += 1;
		// DEBUG:
		// debugOut.println(" Col: " +(col +1) + ": expectedValue:[" +
		// expectedValue + "] actualValue:[" + actualValue +
		// "]");

		// Compare these values
		if ((expectedValue == null && actualValue != null) || (actualValue == null && expectedValue != null)) {
			// Compare nulls
			throw new QueryTestFailedException(eMsg + "Value mismatch at row " + (row + 1) //$NON-NLS-1$
					+ " and column " + (col + 1) //$NON-NLS-1$
					+ " (row in actual result: " + actualResultRow + ", row in expected result: " + expectedResultRow + ")"
					+ ": expected = [" //$NON-NLS-1$
					+ (expectedValue != null ? expectedValue : "null") + "], actual = [" //$NON-NLS-1$
					+ (actualValue != null ? actualValue : "null") + "]"); //$NON-NLS-1$

		}

		if (expectedValue == null && actualValue == null) {
			return;
		}

        if (actualValue instanceof Clob) {
            Clob c = (Clob) actualValue;
            try {
                actualValue = ObjectConverterUtil.convertToString(c.getAsciiStream());

            } catch (Throwable e) {
                throw new QueryTestFailedException(e);
            }
            expectedValue = expectedValue.toString();
        } else if (actualValue instanceof SQLXML) {
            SQLXML s = (SQLXML) actualValue;
            try {
                actualValue = ObjectConverterUtil.convertToString(s.getBinaryStream());

            } catch (Throwable e) {
                throw new QueryTestFailedException(e);
            }
            expectedValue = expectedValue.toString();
        } else if (actualValue.getClass().getName().startsWith("[B")) {
            actualValue = Base64.encodeBytes((byte[]) actualValue);
        } else if(actualValue.getClass().getName().startsWith("[java.lang.Byte")){
            actualValue = Base64.encodeBytes(ArrayUtils.toPrimitive((Byte[]) actualValue));
        } else if (actualValue instanceof Blob) {
            Blob b = (Blob) actualValue;
            try {
                byte[] ba = ObjectConverterUtil.convertToByteArray(b.getBinaryStream());

                actualValue = Base64.encodeBytes(ba);
            } catch (Throwable e) {
                throw new QueryTestFailedException(e);
            }
            expectedValue = expectedValue.toString();
        }
		
		if((expectedValue instanceof BigDecimal
						&& actualValue instanceof BigDecimal)
				|| (expectedValue instanceof Double
				        && actualValue instanceof Double)
				|| (expectedValue instanceof Float
                        && actualValue instanceof Float)){
		    boolean fail = false;
		    // big decimal
		    if(expectedValue instanceof BigDecimal){
                BigDecimal expV = (BigDecimal)expectedValue;
                BigDecimal actV = (BigDecimal)actualValue;
                if(expV.compareTo(actV) != 0){
                    if(allowedDivergenceIsZero){
                        fail = true; //not equals and divergence is zero;
                    } else {
                        fail =     expV.add(allowedDivergence).compareTo(actV) < 0
                                || expV.subtract(allowedDivergence).compareTo(actV) > 0;
                    }
                }
            }
		    // double
		    if(expectedValue instanceof Double){
                Double expV = (Double)expectedValue;
                Double actV = (Double)actualValue;
                if(expV.doubleValue() != actV.doubleValue()){
                    if(allowedDivergenceIsZero){
                        fail = true; //not equals and divergence is zero;
                    } else {
                        fail =     expV.doubleValue() + allowedDivergence.doubleValue() < actV.doubleValue()
                                || expV.doubleValue() - allowedDivergence.doubleValue() > actV.doubleValue();
                    }
                }
            }
		    // float
		    if(expectedValue instanceof Float){
		        Float expV = (Float)expectedValue;
		        Float actV = (Float)actualValue;
                if(expV.floatValue() != actV.floatValue()){
                    if(allowedDivergenceIsZero){
                        fail = true; //not equals and divergence is zero;
                    } else {
                        fail =     expV.floatValue() + allowedDivergence.floatValue() < actV.floatValue()
                                || expV.floatValue() - allowedDivergence.floatValue() > actV.floatValue();
                    }
                }
            }
			if(fail){
				throw new QueryTestFailedException(eMsg
						+ "Value mismatch at row " + (row + 1) //$NON-NLS-1$
						+ " and column " + (col + 1) //$NON-NLS-1$
						+ " (row in actual result: " + actualResultRow + ", row in expected result: " + expectedResultRow + ")"
						+ ": expected = [" //$NON-NLS-1$
						+ expectedValue + "], actual = [" //$NON-NLS-1$
						+ actualValue + "] {allowed divergence: " + allowedDivergence + "}"); //$NON-NLS-1$ $NON-NLS-2$
			} else {
				return; // columns have been compared
			}
		}
		
		// Compare values with equals
		if (!expectedValue.equals(actualValue)) {
			// DEBUG:

			if (expectedValue instanceof java.sql.Date) {
				expectedValue = expectedValue.toString();
				actualValue = actualValue.toString();

			} else if (expectedValue instanceof java.sql.Time) {
				expectedValue = expectedValue.toString();
				actualValue = actualValue.toString();

			}

			if (expectedValue instanceof String) {
				final String expectedString = (String) expectedValue;

				if (!(actualValue instanceof String)) {
					throw new QueryTestFailedException(eMsg + "Value (types) mismatch at row " + (row + 1) //$NON-NLS-1$
							+ " and column " + (col + 1) //$NON-NLS-1$
							+ " (row in actual result: " + actualResultRow + ", row in expected result: " + expectedResultRow + ")"
							+ ": expected = [" //$NON-NLS-1$
							+ expectedValue + ", (String) ], actual = [" //$NON-NLS-1$
							+ actualValue + ", (" + actualValue.getClass().getName() + ") ]"); //$NON-NLS-1$
				}

				// Check for String difference
				assertStringsMatch(expectedString, (String) actualValue, (row + 1), (col + 1), actualResultRow, expectedResultRow, eMsg);

			} else {

				throw new QueryTestFailedException(eMsg + "Value mismatch at row " + (row + 1) //$NON-NLS-1$
						+ " and column " + (col + 1) //$NON-NLS-1$
						+ " (row in actual result: " + actualResultRow + ", row in expected result: " + expectedResultRow + ")"
						+ ": expected = [" //$NON-NLS-1$
						+ expectedValue + "], actual = [" //$NON-NLS-1$
						+ actualValue + "]"); //$NON-NLS-1$

			}
		}
	}

	/**
	 * Compare actual results, identifiers and types with expected. <br>
	 * <strong>Note </strong>: result list are expected to match element for
	 * element.</br>
	 * 
	 * @param actualResults
	 * @param actualDatatypes
	 * @param actualIdentifiers
	 * @param expectedResults
	 * @param expectedDatatypes
	 * @param expectedIdentifiers
	 * @param eMsg
	 * @throws QueryTestFailedException
	 *             If comparison fails.
	 */
	private static void compareResultSets(
	        final List<List<Object>> actualResults, final List<List<Object>> originalActualResults, 
			final List<String> actualDatatypes, final List<String> actualIdentifiers,
			final List<List<Object>> expectedResults, final List<List<Object>> originalExpectedResults,
			final List<String> expectedDatatypes, final List<String> expectedIdentifiers,
			final String eMsg)
			throws QueryTestFailedException {
		// Compare column names and types
		compareIdentifiers(actualIdentifiers, expectedIdentifiers,
				actualDatatypes, expectedDatatypes);

		// Walk through records and compare actual against expected
		final int actualRowCount = actualResults.size();
		final int expectedRowCount = expectedResults.size();
		final int actualColumnCount = actualIdentifiers.size();


		// Check for less records than in expected results
		if (actualRowCount < expectedRowCount) {
			throw new QueryTestFailedException(eMsg
					+ "Expected " + expectedRowCount + //$NON-NLS-1$
					" records but received only " + actualRowCount); //$NON-NLS-1$
		} else if (actualRowCount > expectedRowCount) {
			// Check also for more records than expected
			throw new QueryTestFailedException(eMsg
					+ "Expected " + expectedRowCount + //$NON-NLS-1$
					" records but received " + actualRowCount); //$NON-NLS-1$
		}

		// DEBUG:
		// debugOut.println("================== Compariing Rows ===================");
		
		if(allowedDivergence == null){ // we do not allow different divergence for different queries
			String allowedDivergenceStr = ConfigPropertyLoader.getInstance().getProperty(TestProperties.ALLOWED_DIVERGENCE);
			if(allowedDivergenceStr == null || allowedDivergenceStr.isEmpty()){
				allowedDivergence = BigDecimal.ZERO;
				allowedDivergenceIsZero = true;
			} else {
				try{
					allowedDivergence = new BigDecimal(allowedDivergenceStr);
					allowedDivergenceIsZero = allowedDivergence.compareTo(BigDecimal.ZERO) == 0;
				} catch (NumberFormatException ex){
					allowedDivergence = BigDecimal.ZERO;
					allowedDivergenceIsZero = true;
				}
			}
		}
		
		MultiTestFailedException multiException = new MultiTestFailedException();
		
		// Loop through rows
		for (int row = 0; row < actualRowCount; row++) {

			// Get actual record
			final List<Object> actualRecord = actualResults.get(row);

			// Get expected record
			final List<Object> expectedRecord = expectedResults.get(row);

			// DEBUG:
			// debugOut.println("Row: " + (row + 1));
			// debugOut.println(" expectedRecord: " + expectedRecord);
			// debugOut.println(" actualRecord: " + actualRecord);
			// Loop through columns
			// Compare actual elements with expected elements column by column
			// in this row
			for (int col = 0; col < actualColumnCount; col++) {
				// Get actual value
				Object actualValue = actualRecord.get(col);
				// Get expected value
				Object expectedValue = expectedRecord.get(col);

				try {
					compareResultColumn(actualValue, expectedValue, row, col,
					        getIndexByRef(originalActualResults, actualRecord),
					        getIndexByRef(originalExpectedResults, expectedRecord),
					        eMsg);
				} catch (QueryTestFailedException e) {
					multiException.addFailure(e);
				}

			} // end loop through columns
		} // end loop through rows

		if (multiException.getTotalFailures() == 1) {
			throw multiException.getFailures().get(0);
		} else if (multiException.getTotalFailures() > 1) {
			throw multiException;
		}
	}

	private static <T> int getIndexByRef(List<T> list, T o){
	    if(list == null){
	        return -1;
	    }
	    int i = 0;
	    for(T t : list){
	        if(t == o){
	            return i;
	        }
	        i++;
	    }
	    return -1;
	}
	
	private static void compareIdentifiers(List<String> actualIdentifiers,
			List<String> expectedIdentifiers, List<String> actualDataTypes,
			List<String> expectedDatatypes) throws QueryTestFailedException {

		// Check sizes
		if (expectedIdentifiers.size() != actualIdentifiers.size()) {
			throw new QueryTestFailedException(
					"Got incorrect number of columns, expected = " + expectedIdentifiers.size() + ", actual = " //$NON-NLS-1$ //$NON-NLS-2$
							+ actualIdentifiers.size());
		}

		// Compare identifier lists only by short name
		for (int i = 0; i < actualIdentifiers.size(); i++) {
			String actualIdent = actualIdentifiers.get(i);
			String expectedIdent = expectedIdentifiers.get(i);
			String actualType = actualDataTypes.get(i);
			String expectedType = expectedDatatypes.get(i);

			// Get short name for each identifier
			String actualShort = getShortName(actualIdent);
			String expectedShort = getShortName(expectedIdent);

			if (!expectedShort.equalsIgnoreCase(actualShort)) {
				throw new QueryTestFailedException(
						"Got incorrect column name at column " + i + ", expected = " + expectedShort + " but got = " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
								+ actualShort);
			}
//			if (actualType.equalsIgnoreCase("xml")) {//$NON-NLS-1$
//				actualType = "string";//$NON-NLS-1$
//			}
//			if (actualType.equalsIgnoreCase("clob")) {//$NON-NLS-1$
//				actualType = "string";//$NON-NLS-1$
//			}

			if (actualType.equalsIgnoreCase("blob")) {
				Class<?> nodeType = TagNames.TYPE_MAP.get(actualType);
				actualType = nodeType.getSimpleName();
			}
			if (!expectedType.equalsIgnoreCase(actualType)) {
				throw new QueryTestFailedException(
						"Got incorrect column type at column " + i + ", expected = " + expectedType + " but got = " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
								+ actualType);
			}
		}
	}

	private static String getShortName(String ident) {
		int index = ident.lastIndexOf("."); //$NON-NLS-1$
		if (index >= 0) {
			return ident.substring(index + 1);
		}
		return ident;
	}

	private static final int MISMATCH_OFFSET = 20;
	private static final int MAX_MESSAGE_SIZE = 50;

	private static void assertStringsMatch(final String expected,
			final String actual, final int row, final int col,
			final int actualResultRow, final int expectedResultRow,
			final String eMsg) throws QueryTestFailedException {
		// TODO: Replace stripCR() with XMLUnit comparison for XML results.
		// stripCR() is a workaround for comparing XML Queries
		// that have '\r'.
		//String expected = stripCR(expectedStr).trim(); // DISABLED, should not do any trimming
		//String actual = stripCR(actualStr).trim();

		String locationText = ""; //$NON-NLS-1$
		int mismatchIndex = -1;

		boolean isequal = Arrays.equals(expected.toCharArray(),
				actual.toCharArray());

		// if (!expected.equals(actual)) {
		if (!isequal) {
			if (expected != null && actual != null) {
				int shortestStringLength = expected.length();
				if (actual.length() < expected.length()) {
					shortestStringLength = actual.length();
				}
				for (int i = 0; i < shortestStringLength; i++) {
					if (expected.charAt(i) != actual.charAt(i)) {
						locationText = "  Strings do not match at character: " + (i + 1) + //$NON-NLS-1$
								". Expected [" + expected.charAt(i)
								+ "] in " + expected + " - but got [" + actual.charAt(i) + "] in " + actual; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						mismatchIndex = i;
						break;
					}
				}
			}

			String expectedPartOfMessage = expected;
			String actualPartOfMessage = actual;
			if (expected.length() + actual.length() > MAX_MESSAGE_SIZE) {
				expectedPartOfMessage = safeSubString(expected, mismatchIndex
						- MISMATCH_OFFSET, mismatchIndex + MISMATCH_OFFSET);
				actualPartOfMessage = safeSubString(actual, mismatchIndex
						- MISMATCH_OFFSET, mismatchIndex + MISMATCH_OFFSET);
			}

			String message = eMsg + "String mismatch at row " + row //$NON-NLS-1$
					+ " and column " + col//$NON-NLS-1$
					+ " (row in actual result: " + actualResultRow + ", row in expected result: " + expectedResultRow + ")"
					+ ". Expected: {0} but was: {1}" + locationText; //$NON-NLS-1$
			message = MessageFormat.format(message, new Object[] {
					expectedPartOfMessage, actualPartOfMessage });
			throw new QueryTestFailedException(message);
		}
	}

	private static String safeSubString(String text, int startIndex, int endIndex) {
		String prefix = "...'"; //$NON-NLS-1$
		String suffix = "'..."; //$NON-NLS-1$

		int actualStartIndex = startIndex;
		if (actualStartIndex < 0) {
			actualStartIndex = 0;
			prefix = "'"; //$NON-NLS-1$
		}
		int actualEndIndex = endIndex;
		if (actualEndIndex > text.length() - 1) {
			actualEndIndex = text.length() - 1;
			if (actualEndIndex < 0) {
				actualEndIndex = 0;
			}
		}
		if (actualEndIndex == text.length() - 1 || text.length() == 0) {
			suffix = "'"; //$NON-NLS-1$
		}

		return prefix + text.substring(actualStartIndex, actualEndIndex)
				+ suffix;
	}

//	private static String stripCR(final String text) {
//		if (text.indexOf('\r') >= 0) {
//			StringBuffer stripped = new StringBuffer(text.length());
//			int len = text.length();
//			for (int i = 0; i < len; i++) {
//				char current = text.charAt(i);
//				if (current != '\r') {
//					stripped.append(current);
//				}
//			}
//			return stripped.toString();
//		}
//		return text;
//	}
}

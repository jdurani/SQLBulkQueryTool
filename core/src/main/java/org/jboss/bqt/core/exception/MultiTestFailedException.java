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

package org.jboss.bqt.core.exception;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Exception to encapsulate multiple test failures.
 */
public class MultiTestFailedException extends QueryTestFailedException {

	/**
	 * @see java.io.Serializable
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Maximum number of failures saved (to prevent excessive memory and file sizes)
	 */
	private static final int savedFailuresLimit = 2000;

	/**
	 * Total number of failures, including those not enlisted.
	 */
	private long totalFailures;

	/**
	 * List of encapsulated failures.
	 */
	private List<QueryTestFailedException> failures = new ArrayList<QueryTestFailedException>();

	/**
	 * New multi-failure exception.
	 */
	public MultiTestFailedException() {
	}

	/**
	 * Returns total number of failures.
	 * @return total number of failures.
	 */
	public long getTotalFailures() {
		return totalFailures;
	}

	/**
	 * Add failure to the list.
	 * @param failure failure to add
	 */
	public void addFailure(QueryTestFailedException failure) {
		if (totalFailures < savedFailuresLimit) {
			failures.add(failure);
		}
		totalFailures++;
	}

	/**
	 * Returns the exception message. If there were more failures, the first message and the comment informing about
	 * more messages is returned.
	 * @see org.jboss.bqt.core.exception.QueryTestFailedException#getMessage()
	 */
	@Override
	public String getMessage() {
		if (failures.size() > 0) {
			return failures.get(0).getMessage() + " (+" + (totalFailures - 1) + " more failures)";
		}

		return super.getMessage();
	}

	/**
	 * Returns list of failures encapsulated by this exception.
	 * @return list of failures-exceptions
	 */
	public List<QueryTestFailedException> getFailures() {
		// in case there were more failures than is the limit, set the last exception to inform about it
		if (totalFailures > savedFailuresLimit) {
			if (failures.size() == savedFailuresLimit) {
				failures.add(null);
			}
			failures.set(failures.size() - 1, new QueryTestFailedException("(+" + (totalFailures - savedFailuresLimit)
					+ " more failures)"));
		}
		return Collections.unmodifiableList(failures);
	}

}
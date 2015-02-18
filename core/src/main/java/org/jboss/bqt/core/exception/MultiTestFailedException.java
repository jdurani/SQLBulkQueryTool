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
	 * List of encapsulated failures.
	 */
	private List<Throwable> failures;

	/**
	 * New exception consisting of multiple test failures.
	 * @param failures list of test failures
	 */
	public MultiTestFailedException(List<Throwable> failures) {
		this.failures = failures;
	}

	/**
	 * Returns the exception message. If there were more failures, the first message and the comment informing about
	 * more messages is returned.
	 * @see org.jboss.bqt.core.exception.QueryTestFailedException#getMessage()
	 */
	@Override
	public String getMessage() {
		if (failures != null) {
			return failures.get(0).getMessage() + " (+" + (failures.size() - 1) + " more failures)";
		}

		return super.getMessage();
	}

	/**
	 * Returns list of failures encapsulated by this exception.
	 * @return list of failures-exceptions
	 */
	public List<Throwable> getFailures() {
		if (failures != null) {
			return failures;
		}

		return new ArrayList<Throwable>();
	}

}
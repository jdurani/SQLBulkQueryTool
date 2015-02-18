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

package org.jboss.bqt.client;

import java.io.File;
import java.util.regex.PatternSyntaxException;

import org.jboss.bqt.core.exception.FrameworkRuntimeException;
import org.jboss.bqt.framework.ConfigPropertyLoader;

/**
 * Decides if certain test scenario should be run or not, based on including/excluding scenario names in system
 * properties. By default, all scenarios are included and none are excluded. For the scenario name to be processed, it
 * has to pass both rules.
 */
public class ExclusionManager {

	/**
	 * Regex pattern matching scenarios to include.
	 */
	private String includePattern;

	/**
	 * Regex pattern matching scenarios to exclude.
	 */
	private String excludePattern;

	/**
	 * Throws exception when the regex pattern is not valid.
	 * @param pattern regular expression to validate
	 * @throws PatternSyntaxException if the regular expression's syntax is invalid
	 */
	private void validatePattern(String pattern) {
		"".matches(pattern); // throws exception
	}

	/**
	 * Sets and validates pattern matching scenarios to include.
	 * @param includePattern regex pattern matching scenarios to include
	 * @throws FrameworkRuntimeException if the regular expression's syntax is invalid
	 */
	private void setIncludePattern(String includePattern) {
		try {
			validatePattern(includePattern);
		} catch (PatternSyntaxException e) {
			throw new FrameworkRuntimeException(e, "Invalid " + TestProperties.PROP_SCENARIO_INCLUDE
					+ " regex pattern: " + e.getMessage());
		}
		this.includePattern = includePattern;
	}

	/**
	 * Sets and validates pattern matching scenarios to exclude.
	 * @param excludePattern regex pattern matching scenarios to exclude
	 * @throws FrameworkRuntimeException if the regular expression's syntax is invalid
	 */
	private void setExcludePattern(String excludePattern) {
		try {
			validatePattern(excludePattern);
		} catch (PatternSyntaxException e) {
			throw new FrameworkRuntimeException(e, "Invalid " + TestProperties.PROP_SCENARIO_EXCLUDE
					+ " regex pattern: " + e.getMessage());
		}
		this.excludePattern = excludePattern;
	}

	/**
	 * New object configured from ConfigPropertyLoader instance.
	 * @param p property loader to load config from
	 * @throws FrameworkRuntimeException if the regular expression's syntax is invalid
	 */
	public ExclusionManager(ConfigPropertyLoader p) {
		String includePatternProperty = p.getProperty(TestProperties.PROP_SCENARIO_INCLUDE);
		if (includePatternProperty != null && includePatternProperty.length() > 0) {
			setIncludePattern(includePatternProperty);
		}

		String excludePatternProperty = p.getProperty(TestProperties.PROP_SCENARIO_EXCLUDE);
		if (excludePatternProperty != null && excludePatternProperty.length() > 0) {
			setExcludePattern(excludePatternProperty);
		}
	}

	/**
	 * New object configured directly using include and exclude regex patterns.
	 * @param includePattern regex pattern matching scenarios to include
	 * @param excludePattern regex pattern matching scenarios to exclude
	 * @throws PatternSyntaxException if the regular expression's syntax is invalid
	 */
	public ExclusionManager(String includePattern, String excludePattern) {
		setIncludePattern(includePattern);
		setExcludePattern(excludePattern);
	}

	/**
	 * Full-string regex pattern match, validating input.
	 * @param str string to match
	 * @param pattern regex pattern
	 * @return true if the string matches, false otherwise
	 */
	private boolean matchesPattern(String str, String pattern) {
		if (str.matches("^(" + pattern + ")$")) {
			return true;
		}

		return false;
	}

	/**
	 * If the scenario given by certain .properties file should be active or not.
	 * @param scenario .properties file with the scenario
	 * @return true if the scenario should run, false otherwise
	 */
	public boolean isScenarioActive(File scenario) {
		String name = scenario.getName();
		if (name.endsWith(".properties")) {
			name = name.substring(0, name.lastIndexOf('.'));
		}

		return isScenarioActive(name);
	}

	/**
	 * If the scenario with given name should be active or not. Name should already be clean of any paths and
	 * extensions.
	 * @param name scenario name
	 * @return true if the scenario should run, false otherwise
	 */
	public boolean isScenarioActive(String name) {
		if (includePattern != null && !matchesPattern(name, includePattern)) {
			return false;
		}

		if (excludePattern != null && matchesPattern(name, excludePattern)) {
			return false;
		}

		return true;
	}
}

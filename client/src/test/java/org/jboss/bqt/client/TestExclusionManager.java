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

import junit.framework.Assert;

import org.jboss.bqt.core.exception.FrameworkRuntimeException;
import org.jboss.bqt.framework.ConfigPropertyLoader;
import org.junit.Test;

/**
 * Test of ExclusionManager class - logic to include/exclude scenarios to run.
 */
public class TestExclusionManager {

	/**
	 * Test the exclusion logic based on regular expression matching.
	 */
	@Test
	public void testExclusions() {
		System.setProperty("bqt.scenario.include", "scenario *[0-9]*");
		System.setProperty("bqt.scenario.exclude", "scenario *1|scenario2");

		ExclusionManager em = new ExclusionManager(ConfigPropertyLoader.getInstance());

		Assert.assertEquals(true, em.isScenarioActive("scenario"));
		Assert.assertEquals(true, em.isScenarioActive(new File("~/path/scenario      2.properties")));
		Assert.assertEquals(false, em.isScenarioActive(new File("~/path/scenario3.extension")));
		Assert.assertEquals(false, em.isScenarioActive("different scenario"));
		Assert.assertEquals(false, em.isScenarioActive(new File("~/path/scenario     1.properties")));
	}

	/**
	 * By default, everything should be included.
	 */
	@Test
	public void testDefault() {
		System.setProperty("bqt.scenario.include", "");
		System.setProperty("bqt.scenario.exclude", "");

		ExclusionManager em = new ExclusionManager(ConfigPropertyLoader.getInstance());

		Assert.assertEquals(true, em.isScenarioActive("scenario"));
		Assert.assertEquals(true, em.isScenarioActive(new File("~/path/scenario3.properties")));
		Assert.assertEquals(true, em.isScenarioActive("different scenario"));
		Assert.assertEquals(true, em.isScenarioActive(new File("~/path/scenario     1.properties")));
	}

	/**
	 * Test the exception when invalid regular expression is passed.
	 */
	@Test(expected = FrameworkRuntimeException.class)
	public void testInvalidFormat() {
		System.setProperty("bqt.scenario.include", "(scenario *[0-9]*]");
		System.setProperty("bqt.scenario.exclude", "scenario *1|scenario2");

		@SuppressWarnings("unused")
		ExclusionManager em = new ExclusionManager(ConfigPropertyLoader.getInstance());
	}

}

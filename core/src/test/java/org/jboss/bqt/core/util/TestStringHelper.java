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

package org.jboss.bqt.core.util;

import junit.framework.Assert;

import org.jboss.bqt.core.exception.FrameworkRuntimeException;
import org.junit.Test;

/**
 * Test class for StringHelper - generic string helper functions.
 */
public class TestStringHelper {

	// ********* T E S T S U I T E M E T H O D S *********

	/**
	 * Tests {@link org.jboss.bqt.core.util.StringHelper}
	 */
	@Test
	public void testSingleParameter() {

		String value = StringHelper.createString("Test {0} replaced", "value");
		Assert.assertEquals("Test value replaced", value); //$NON-NLS-1$
	}

	/**
	 * Tests encoding String as hexadecimal number.
	 */
	@Test
	public void testEncodeHex() {
		Assert.assertEquals("68656c6c6f207376c49b7465", StringHelper.encodeHex("hello světe"));
		Assert.assertEquals("", StringHelper.encodeHex(""));
	}

	/**
	 * Tests decoding String as hexadecimal number.
	 */
	@Test
	public void testDecodeHex() {
		Assert.assertEquals("hello světe", StringHelper.decodeHex("68656c6c6f207376c49b7465"));
		Assert.assertEquals("", StringHelper.decodeHex(""));
	}

	/**
	 * Tests exception when invalid hexadecimal string passed.
	 * @throws FrameworkRuntimeException for invalid hexadecimal input
	 */
	@Test(expected = FrameworkRuntimeException.class)
	public void testDecodeHexError() throws FrameworkRuntimeException {
		StringHelper.decodeHex("68656cs6c6f207376g49b7465");
	}

}

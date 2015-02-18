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

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jboss.bqt.core.CorePlugin;
import org.jboss.bqt.core.exception.FrameworkRuntimeException;

/**
 * This is a common place to put String helper methods.
 */
public final class StringHelper {

	public static interface Constants {
		char LINE_FEED_CHAR = '\n';
		char NEW_LINE_CHAR = LINE_FEED_CHAR;
		char SPACE_CHAR = ' ';

		String EMPTY_STRING = ""; //$NON-NLS-1$

		String[] EMPTY_STRING_ARRAY = new String[0];

		// all patterns below copied from Eclipse's PatternConstructor class.
		final Pattern PATTERN_BACK_SLASH = Pattern.compile("\\\\"); //$NON-NLS-1$
		final Pattern PATTERN_QUESTION = Pattern.compile("\\?"); //$NON-NLS-1$
		final Pattern PATTERN_STAR = Pattern.compile("\\*"); //$NON-NLS-1$
		final Pattern PARAMETER_COUNT_PATTERN = Pattern.compile("\\{(\\d+)\\}");
	}

	
	@SuppressWarnings("null")
	public static boolean isEqual(String arg1, String arg2) {
		if (arg1 == null && arg2 == null) return true;
		if (arg1 == null && arg2 != null) return false;
		if (arg2 == null && arg1 != null) return false;
				
		return arg1.equals(arg2);

	}	
    /**
     * Create a string by substituting the parameters into all key occurrences in the supplied format. The pattern consists of
     * zero or more keys of the form <code>{n}</code>, where <code>n</code> is an integer starting at 1. Therefore, the first
     * parameter replaces all occurrences of "{1}", the second parameter replaces all occurrences of "{2}", etc.
     * <p>
     * If any parameter is null, the corresponding key is replaced with the string "null". Therefore, consider using an empty
     * string when keys are to be removed altogether.
     * </p>
     * <p>
     * If there are no parameters, this method does nothing and returns the supplied pattern as is.
     * </p>
     * 
     * @param pattern the pattern
     * @param parameters the parameters used to replace keys
     * @return the string with all keys replaced (or removed)
     */
    public static String createString( String pattern,
                                       Object... parameters ) {
        ArgCheck.isNotNull(pattern, "pattern");
        if (parameters == null) parameters = Constants.EMPTY_STRING_ARRAY;
        Matcher matcher = Constants.PARAMETER_COUNT_PATTERN.matcher(pattern);
        StringBuffer text = new StringBuffer();
        int requiredParameterCount = 0;
        int parmlength = parameters.length;
        if (parameters.length == 1 && parameters[0] == null) {
        	parmlength = 0;
        }
        boolean err = false;
        while (matcher.find()) {
            int ndx = Integer.valueOf(matcher.group(1));
            if (requiredParameterCount <= ndx) {
                requiredParameterCount = ndx + 1;
            }
            if (ndx >= parameters.length) {
                err = true;
                matcher.appendReplacement(text, matcher.group());
            } else {
                Object parameter = parameters[ndx];

                // Automatically pretty-print arrays
                if (parameter != null && parameter.getClass().isArray()) {
                    parameter = Arrays.asList((Object[])parameter);
                }

                matcher.appendReplacement(text, Matcher.quoteReplacement(parameter == null ? "null" : parameter.toString()));
            }
        }
        if (err || requiredParameterCount < parmlength) {
            throw new IllegalArgumentException(
					CorePlugin.Util
					.getString("StringHelper.requiredToSuppliedParameterMismatch",parameters.length,
                                                                                                   parameters.length == 1 ? "" : "s",
                                                                                                   requiredParameterCount,
                                                                                                   requiredParameterCount == 1 ? "" : "s",
                                                                                                   pattern,
                                                                                                   text.toString()));
        }
        matcher.appendTail(text);

        return text.toString();
	}

	/**
	 * Replace unprintable XML characters with dummy unicode character.
	 * @param str string to filter
	 * @return filtered string
	 */
	public static String replaceXmlUnprintable(String str) {
		String xml10pattern = "[^" + "\u0009\r\n" + "\u0020-\uD7FF" + "\uE000-\uFFFD" + "\ud800\udc00-\udbff\udfff"
				+ "]";
		return str.replaceAll(xml10pattern, "\uFFFD");
	}

	/**
	 * Encode any Java-string to UTF-8 textual hexadecimal representation.
	 * @param s input string to encode
	 * @return encoded hexadecimal sequence
	 */
	public static String encodeHex(String s) {
		try {
			byte[] asBytes = s.getBytes("UTF-8");

			StringBuilder sb = new StringBuilder(asBytes.length * 2);
			for (byte b : asBytes) {
				sb.append(String.format("%02x", Byte.valueOf(b)));
			}

			return sb.toString();
		} catch (UnsupportedEncodingException e) {
			throw new FrameworkRuntimeException("UTF-8 encoding not supported");
		}
	}

	/**
	 * Decode hexadecimally encoded UTF-8 back to Java-string.
	 * @param hex encoded hexadecimal sequence (consisting of 0-9a-fA-F)
	 * @return decoded string
	 * @throws FrameworkRuntimeException when the input string is invalid
	 */
	public static String decodeHex(String hex) throws FrameworkRuntimeException {
		if (hex.length() % 2 == 1) {
			throw new FrameworkRuntimeException("Odd number of hexadecimal characters: " + hex);
		}

		try {
			byte[] asBytes = new byte[hex.length() / 2];
			for (int i = 0; i < asBytes.length; i++) {
				// must be integer for bigger range, the value is unsigned
				asBytes[i] = Integer.valueOf(hex.substring(i * 2, i * 2 + 2), 16).byteValue();
			}

			return new String(asBytes, "UTF-8");
		} catch (NumberFormatException e) {
			throw new FrameworkRuntimeException("Invalid hexadecimal string: " + e.getMessage());
		} catch (UnsupportedEncodingException e) {
			throw new FrameworkRuntimeException("UTF-8 encoding not supported");
		}
	}

}

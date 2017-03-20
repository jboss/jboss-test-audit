/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc. and/or its affiliates, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.test.audit.config;

public final class PropertyKeys {

	/**
	 * If specified, links to the specification will be generated
	 */
	public static final String SPECIFICATION_BASE_URL_PROPERTY = "specification_base_url";
	/**
	 * If specified, links to the test class in GitHub will be generated
	 */
	public static final String GITHUB_BASE_URL_PROPERTY = "github_base_url";
	/**
	 * If specified, links to the test class in fisheye will be generated
	 */
	public static final String FISHEYE_BASE_URL_PROPERTY = "fisheye_base_url";
	/**
	 * If specified, links to the test class in SVN will be generated
	 */
	public static final String SVN_BASE_URL_PROPERTY = "svn_base_url";
	/**
	 * The threshold for which the coverage percentage is a pass if it is equal or greater to this value
	 */
	public static final String PASS_THRESHOLD = "pass_threshold";
	/**
	 * The threshold for which the coverage percentage is a fail if it is equal or lower to this value
	 */
	public static final String FAIL_THRESHOLD = "fail_threshold";
	/**
	 * A comma-separated list of the TestNG test groups that aren't included in the coverage
	 */
	public static final String UNIMPLEMENTED_TEST_GROUPS ="unimplemented_test_groups";
	/**
	 * A comma-separated list of TestNG test groups that are summarised at the end of the report
	 */
	public static final String SUMMARY_TEST_GROUPS = "summary_test_groups";


	private PropertyKeys() {
	}



}

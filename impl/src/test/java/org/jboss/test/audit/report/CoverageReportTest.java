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
package org.jboss.test.audit.report;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;

import org.jboss.test.audit.config.RuntimeProperties;
import org.junit.Test;

public class CoverageReportTest {

	@Test
	public void testGeneratedSectionIdHeaders() throws Exception {

		RuntimeProperties properties = new RuntimeProperties();
		CoverageReport report = new CoverageReport(null, new AuditParser(this
				.getClass().getResourceAsStream("/tck-audit-coverage-report.xml"),
				properties).parse(), null, properties);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		report.generate(out);
		String html = out.toString("utf-8");
		System.out.print(html);

		assertTrue(html
				.contains("<h4 class=\"sectionHeader\" id=\"2\">Section 2 - Concepts <sup>[concepts]</sup></h4>"));
		assertTrue(html
				.contains("<h4 class=\"sectionHeader\" id=\"2.2\">Section 2.2 - Bean types <sup>[bean_types]</sup></h4>"));
	}

}

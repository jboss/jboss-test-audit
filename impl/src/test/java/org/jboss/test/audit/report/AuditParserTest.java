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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.jboss.test.audit.config.RuntimeProperties;
import org.junit.Test;

/**
 *
 * @author Martin Kouba
 *
 */
public class AuditParserTest {

	@Test
	public void testParserWithSectionIdGenerator() throws Exception {
		AuditParser parser = new AuditParser(this.getClass().getResourceAsStream("/tck-audit-coverage-report.xml"), new RuntimeProperties()).parse();
		List<String> sectionIds = new ArrayList<String>();
		sectionIds.add("1");
		sectionIds.add("2");
		sectionIds.add("2.1");
		sectionIds.add("2.2");
		assertEquals(sectionIds, parser.getSectionIds());
	}

	@Test
	public void testHasAssertion() throws Exception {
		AuditParser parser = new AuditParser(this.getClass().getResourceAsStream("/tck-audit-coverage-report.xml"), new RuntimeProperties()).parse();
		assertTrue(parser.hasAssertion("concepts", "a"));
	}

}

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
package org.jboss.test.audit.generate;

import static org.junit.Assert.assertEquals;

import org.jboss.test.audit.generate.SectionIdGenerator;
import org.junit.Test;

public class SectionIdGeneratorTest {

	@Test
	public void testNextId() {

		SectionIdGenerator generator = new SectionIdGenerator();
		int[] levels = new int[] { 1, 2, 2, 1, 2, 2, 3, 1, 1, 2, 3, 4 };
		String[] ids = new String[] { "1", "1.1", "1.2", "2", "2.1", "2.2",
				"2.2.1", "3", "4", "4.1", "4.1.1", "4.1.1.1" };

		for (int i = 0; i < levels.length; i++) {
			String id = generator.nextId(levels[i]);
			assertEquals(ids[i], id);
			System.out.println(id);
		}
	}

}

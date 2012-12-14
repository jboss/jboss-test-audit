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
package org.jboss.test.audit;

import org.jboss.test.audit.report.SectionIdComparator;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit test for {@link SectionIdComparator}.
 *
 * @author Gunnar Morling
 *
 */
public class SectionIdComparatorTest
{
    private SectionIdComparator sectionIdComparator;

    @Before
    public void setupComparator()
    {
        sectionIdComparator = new SectionIdComparator();
    }

    @Test
    public void firstLessThanSecond()
    {
        assertEquals(-1 ,sectionIdComparator.compare(null, "1"));
        assertEquals(-1 ,sectionIdComparator.compare("", "1"));
        assertEquals(-1 ,sectionIdComparator.compare("1", "1.1"));
        assertEquals(-1 ,sectionIdComparator.compare("1", "1.1.1"));
        assertEquals(-1 ,sectionIdComparator.compare("1.1", "1.1.1"));
        assertEquals(-1, sectionIdComparator.compare("1.10", "2"));
        assertEquals(-1, sectionIdComparator.compare("1.11", "2"));
        assertEquals(-1, sectionIdComparator.compare("1", "2"));
        assertEquals(-1, sectionIdComparator.compare("1", "2.1"));
        assertEquals(-1, sectionIdComparator.compare("2", "2.1.1"));
        assertEquals(-1, sectionIdComparator.compare("1", "10"));
    }

    @Test
    public void firstLargerThanSecond()
    {
        assertEquals(1 ,sectionIdComparator.compare("1", null));
        assertEquals(1 ,sectionIdComparator.compare("1", ""));
        assertEquals(1 ,sectionIdComparator.compare("1.1", "1"));
        assertEquals(1 ,sectionIdComparator.compare("1.1.1", "1"));
        assertEquals(1 ,sectionIdComparator.compare("1.1.1", "1.1"));
        assertEquals(1, sectionIdComparator.compare("2", "1.10"));
        assertEquals(1, sectionIdComparator.compare("2", "1.11"));
        assertEquals(1, sectionIdComparator.compare("2", "1"));
        assertEquals(1, sectionIdComparator.compare("2.1", "1"));
        assertEquals(1, sectionIdComparator.compare("2.1.1", "2"));
        assertEquals(1, sectionIdComparator.compare("10", "1"));
    }

    @Test
    public void firstEqualsSecond()
    {
        assertEquals(0 ,sectionIdComparator.compare(null, null));
        assertEquals(0 ,sectionIdComparator.compare(null, ""));
        assertEquals(0 ,sectionIdComparator.compare("", null));
        assertEquals(0 ,sectionIdComparator.compare("", ""));
        assertEquals(0 ,sectionIdComparator.compare("1", "1"));
        assertEquals(0 ,sectionIdComparator.compare("1.1", "1.1"));
        assertEquals(0 ,sectionIdComparator.compare("10", "10"));
    }
}

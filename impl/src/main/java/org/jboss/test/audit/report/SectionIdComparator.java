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

import java.util.Comparator;

/**
 * A {@link Comparator} for section ids. Some examples:
 *
 * <ul>
 * <li>1 &lt; 2</li>
 * <li>1 &lt; 1.1</li>
 * <li>1.10 &lt; 2</li>
 * <li>1.1 &lt; 1.2</li>
 * <li>1.1 &lt; 2.1</li>
 * </ul>
 *
 * @author Gunnar Morling
 *
 */
public class SectionIdComparator implements Comparator<String>
{
    @Override
    public int compare(String sectionId1, String sectionId2)
    {
        String[] parts1 = sectionId1 == null ? new String[]{""} : sectionId1.split("[.]");
        String[] parts2 = sectionId2 == null ? new String[]{""} : sectionId2.split("[.]");

        int maxLength = Math.max(parts1.length, parts2.length);

        for (int i = 0; i < maxLength; i++)
        {
            //if the previous parts are equal, then the longer id is larger
            if(i >= parts1.length && i < parts2.length)
            {
                return -1;
            }
            else if(i < parts1.length && i >= parts2.length)
            {
                return 1;
            }

            //compare the current part and see whether one is larger
            Integer first = parse(parts1[i]);
            Integer second = parse(parts2[i]);

            int partComparison = first.compareTo(second);

            if(partComparison != 0)
            {
                return partComparison;
            }
        }

        return 0;
    }

    private Integer parse(String part)
    {
        return part.isEmpty() ? 0 : Integer.valueOf(part);
    }
}

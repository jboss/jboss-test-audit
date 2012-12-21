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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A generator for section ids. Ids are based on the section hierarchy. Each
 * section must have its "level" defined. Top level sections have level 1. Subsections
 * have level of parent plus one.
 *
 * @author Martin Kouba
 */
public class SectionIdGenerator {

	private Map<Integer, AtomicInteger> levelCounters = new HashMap<Integer, AtomicInteger>(
			5);

	/**
	 * Generate the next section id.
	 *
	 * @param level
	 * @return
	 */
	public String nextId(int level) {

		if (!levelCounters.containsKey(level)) {
			levelCounters.put(level, new AtomicInteger(0));
		}

		int lastValue = levelCounters.get(level).incrementAndGet();

		StringBuilder id = new StringBuilder();
		for (String parentValue : getParentValues(level)) {
			id.append(parentValue);
			id.append(".");
		}
		id.append(lastValue);
		resetChildValues(level);
		return id.toString();
	}

	/**
	 * Reset all the counters.
	 */
	public void reset() {
		this.levelCounters.clear();
	}

	private List<String> getParentValues(int level) {

		List<String> parts = new ArrayList<String>();

		for (Entry<Integer, AtomicInteger> counter : levelCounters.entrySet()) {
			if (counter.getKey() < level) {
				parts.add(counter.getValue().toString());
			}
		}
		return parts;
	}

	private void resetChildValues(int level) {
		for (Entry<Integer, AtomicInteger> counter : levelCounters.entrySet()) {
			if (counter.getKey() > level) {
				counter.getValue().set(0);
			}
		}
	}

}

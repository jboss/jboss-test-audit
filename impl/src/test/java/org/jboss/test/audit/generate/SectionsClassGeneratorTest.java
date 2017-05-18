/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc., and individual contributors
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
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;

import org.junit.Test;

/**
 * @author Martin Kouba
 *
 */
public class SectionsClassGeneratorTest {

	private static final String FILE_SEPARATOR = System
			.getProperty("file.separator");

	@Test
	public void testGenerateSource() throws Exception {

		String source = new SectionsClassGenerator().generateSource(
				this.getClass().getResourceAsStream(
						"/tck-audit-section-class-generator.xml"),
				"org.jboss.cdi.tck.cdi").getValue();
		// System.out.print(source);
		assertTrue(source.contains("1 Architecture"));
		assertTrue(source.contains("2.2.1 Legal bean types"));
		assertTrue(source.contains("3.1 Managed beans"));
		assertTrue(source
				.contains("public static final String ARCHITECTURE = \"architecture\";"));
		assertTrue(source
				.contains("public static final String LEGAL_BEAN_TYPES = \"legal_bean_types\";"));
	}

	@Test
	public void testGenerateToFile() throws Exception {

		String packageBase = "org.jboss.cdi.tck.audit";
		String specId = "cdi";

		File outDir = new File("target" + FILE_SEPARATOR + "generated-sources");
		outDir.mkdirs();

		SectionsClassGenerator generator = new SectionsClassGenerator();
		generator.generateToFile(
				outDir,
				this.getClass().getResourceAsStream(
						"/tck-audit-section-class-generator.xml"), packageBase);

		File sourceFile = new File(outDir,
				generator.packageNameToPath(packageBase + "." + specId)
						+ FILE_SEPARATOR
						+ SectionsClassGenerator.sourceClassName + ".java");
		assertTrue(sourceFile.exists());
		assertTrue(sourceFile.isFile());
	}

	@Test
	public void testPackageNameToPath() {

		SectionsClassGenerator generator = new SectionsClassGenerator();
		generator.setFileSeparator("\\");
		assertEquals(
				"org\\jboss\\test\\audit\\generate\\SectionClassGenerator",
				generator
						.packageNameToPath("org.jboss.test.audit.generate.SectionClassGenerator"));
	}

	// Used only to test the limits of javac
	// @Test
	public void testGeneratedSourceCompilation() throws Exception {

		int sections = 5000;

		StringBuilder xml = new StringBuilder();
		xml.append("<specification id=\"cdi\" generateSectionClass=\"true\" generateSectionIds=\"true\">");
		// Simulate n sections
		for (int i = 0; i < sections; i++) {
			xml.append("<section id=\"concepts");
			xml.append(i);
			xml.append("\" title=\"Concepts ");
			xml.append(i);
			xml.append("\" level=\"1\"/>");
		}
		xml.append("</specification>");

		byte[] bytes = xml.toString().getBytes();
		InputStream in = new ByteArrayInputStream(bytes);

		String source = new SectionsClassGenerator().generateSource(in,
				"org.jboss.cdi.tck").getValue();
		// System.out.print(source);

		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		DiagnosticCollector<JavaFileObject> diagnosticsCollector = new DiagnosticCollector<JavaFileObject>();
		JavaFileObject file = new JavaSourceFromString("Sections.java", source);

		Iterable<? extends JavaFileObject> compilationUnits = Arrays
				.asList(file);

		CompilationTask task = compiler.getTask(null, null,
				diagnosticsCollector, null, null, compilationUnits);

		assertTrue(task.call());
	}

	private final class JavaSourceFromString extends SimpleJavaFileObject {

		private final String source;

		JavaSourceFromString(String className, String code)
				throws URISyntaxException {
			super(new URI(className), Kind.SOURCE);
			this.source = code;
		}

		@Override
		public CharSequence getCharContent(boolean ignoreEncodingErrors) {
			return source;
		}
	}

}

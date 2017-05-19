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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.annotation.Generated;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Generates the source code (naive way) of a section constants container class.
 *
 * The original intention was to generate the source code during annotation
 * processing. However due to various problems this class should be called
 * separately (probably using exec-maven-plugin).
 *
 * @author Martin Kouba
 */
public class SectionsClassGenerator {

	protected static String sourceClassName = "Sections";

	private static final String NEW_LINE = "\n";

	private static final String GAP = " ";

	private static final String PACKAGE_SEPARATOR = ".";

	private static final String DEFAULT_FILE_SEPARATOR = System
			.getProperty("file.separator");

	private String fileSeparator = DEFAULT_FILE_SEPARATOR;

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		if (args.length < 3) {
			throw new IllegalArgumentException();
		}

		String auditFilePath = args[0];
		String packageBase = args[1];
		String outputDirPath = args[2];

		if (args.length > 3) {
			sourceClassName = args[3];
		}

		SectionsClassGenerator generator = new SectionsClassGenerator();

		try {
			generator.generateToFile(new File(outputDirPath),
					new FileInputStream(new File(auditFilePath)), packageBase);
		} catch (Exception e) {
			throw new RuntimeException(
					"Unable to generate class with section constants.", e);
		}
	}

	/**
	 *
	 * @param auditXmlFile
	 * @param packageBase
	 * @return
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
	public GeneratedSource generateSource(InputStream auditXmlFile,
			String packageBase) throws SAXException, IOException,
			ParserConfigurationException {

		SectionIdGenerator sectionIdGenerator = new SectionIdGenerator();

		DocumentBuilder builder = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder();
		Document doc = builder.parse(auditXmlFile);
		String specId = doc.getDocumentElement().getAttribute("id");

		StringBuilder source = new StringBuilder();
		source.append("package ");
		source.append(packageBase);
		source.append(PACKAGE_SEPARATOR);
		source.append(specId);
		source.append(";");
		source.append(NEW_LINE);

		// Generator info
		source.append("@");
		source.append(Generated.class.getName());
		source.append("(value=\"");
		source.append(this.getClass().getName());
		source.append("\", date=\"");
		source.append(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZ")
				.format(new Date()));
		source.append("\")");
		source.append(NEW_LINE);

		source.append("public final class ");
		source.append(sourceClassName);
		source.append(" {");
		source.append(NEW_LINE);
		source.append("private " + sourceClassName+ "() {}");
		source.append(NEW_LINE);

		NodeList sectionNodes = doc.getDocumentElement().getChildNodes();

		for (int i = 0; i < sectionNodes.getLength(); i++) {

			if (sectionNodes.item(i) instanceof Element
					&& "section".equals(sectionNodes.item(i).getNodeName())) {

				Element sectionElement = (Element) sectionNodes.item(i);

				String id = sectionElement.getAttribute("id");
				String level = sectionElement.getAttribute("level");
				String title = sectionElement.getAttribute("title");

				String generatedId = level.isEmpty() ? null
						: sectionIdGenerator.nextId(Integer.valueOf(level));

				source.append("/**");
				source.append(NEW_LINE);
				source.append(" * ");
				if (generatedId != null) {
					source.append(generatedId);
					source.append(GAP);
				}
				source.append(title);
				source.append(NEW_LINE);
				source.append(" */");
				source.append(NEW_LINE);
				source.append("public static final String ");
				source.append(normalize(id));
				source.append(" = ");
				source.append("\"");
				source.append(id);
				source.append("\";");
				source.append(NEW_LINE);
			}
		}
		source.append("}");

		GeneratedSource generatedSource = new GeneratedSource(
				source.toString(), packageBase + PACKAGE_SEPARATOR + specId,
				sourceClassName);
		System.out.println("Section class source generated: "
				+ generatedSource.getName());
		return generatedSource;
	}

	/**
	 *
	 * @param auditFile
	 * @param packageBase
	 * @throws URISyntaxException
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
	public void generateToFile(File outputDir, InputStream auditFile,
			String packageBase) throws URISyntaxException, SAXException,
			IOException, ParserConfigurationException {

		GeneratedSource generatedSource = generateSource(auditFile, packageBase);

		File generatedSourceDir = new File(outputDir,
				packageNameToPath(generatedSource.getPackageName()));
		generatedSourceDir.mkdirs();

		FileWriter writer = new FileWriter(new File(generatedSourceDir,
				generatedSource.getSimpleName() + ".java"));
		writer.write(generatedSource.getValue());
		writer.close();
	}

	public SectionsClassGenerator setFileSeparator(String fileSeparator) {
		this.fileSeparator = fileSeparator;
		return this;
	}

	protected String packageNameToPath(String packageName) {
		return packageName.replace(".", fileSeparator);
	}

	private String normalize(String id) {

		StringBuilder result = new StringBuilder();
		for (int i = 0; i < id.length(); i++) {
			char tmpChar = id.charAt(i);
			if (Character.isLetterOrDigit(tmpChar) || tmpChar == '_') {
				result.append(tmpChar);
			}
			else if (tmpChar == '-') {
				result.append('_');
			}
		}
		return result.toString().toUpperCase();
	}

	public final class GeneratedSource {

		private String value;

		private String packageName;

		private String className;

		public GeneratedSource(String source, String packageName,
				String className) {
			super();
			this.value = source;
			this.packageName = packageName;
			this.className = className;
		}

		public String getValue() {
			return value;
		}

		public String getPackageName() {
			return packageName;
		}

		public String getName() {
			return packageName + "." + className;
		}

		public String getSimpleName() {
			return className;
		}

	}

}

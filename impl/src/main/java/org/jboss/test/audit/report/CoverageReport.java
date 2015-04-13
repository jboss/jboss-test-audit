package org.jboss.test.audit.report;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import org.jboss.test.audit.config.PropertyKeys;
import org.jboss.test.audit.config.RuntimeProperties;
import org.jboss.test.audit.model.Assertion;
import org.jboss.test.audit.model.Group;
import org.jboss.test.audit.model.Link;
import org.jboss.test.audit.model.Section;
import org.jboss.test.audit.model.SectionElement;
import org.jboss.test.audit.model.TableData;
import org.jboss.test.audit.model.Test;

/**
 * Generates the TCK spec coverage report
 *
 * @author Shane Bryzak
 * @author Tomas Remes
 */
public class CoverageReport {

    public enum TestStatus {
        COVERED, UNCOVERED, UNIMPLEMENTED, UNTESTABLE;
    }

    private static final String REPORT_FILE_NAME = "coverage-%s.html";
    private static final String REPORT_TEMPLATE_NAME = "report-template.ftl";
    private static final String REPORT_TEMPLATE_ENCODING = "UTF-8";

     // References to the spec assertions made by the test tests
    private final Map<String, List<SpecReference>> references;
    private Map<String, Object> data = new HashMap<String, Object>();

    private AuditParser auditParser;
    private File imageTargetDir;
    private File targetDir;
    private RuntimeProperties properties;

    private String fisheyeBaseUrl = null;
    private String svnBaseUrl = null;
    private String githubBaseUrl = null;

    private List<SpecReference> unmatched;
    private List<SpecReference> unversioned;

    private int failThreshold;
    private int passThreshold;
    private Set<String> unimplementedTestGroups;
    private Map<String, Set<Method>> summaryTestGroups;

    public CoverageReport(List<SpecReference> references,
            AuditParser auditParser, RuntimeProperties properties) {
        this.references = new HashMap<>();
        unversioned = new ArrayList<>();

        if (references != null) {
            for (SpecReference ref : references) {
                if (ref.getSpecVersion() == null || !ref.getSpecVersion().equalsIgnoreCase(
                        auditParser.getVersion())) {
                    unversioned.add(ref);
                } else {
                    if (!this.references.containsKey(ref.getSection())) {
                        this.references.put(ref.getSection(),
                                new ArrayList<SpecReference>());
                    }

                    this.references.get(ref.getSection()).add(ref);
                }
            }
        }

        this.auditParser = auditParser;
        this.properties = properties;

        try {
            // FishEye
            fisheyeBaseUrl = this.properties.getStringValue(
                    PropertyKeys.FISHEYE_BASE_URL_PROPERTY, null, false);
            if (fisheyeBaseUrl != null && !fisheyeBaseUrl.endsWith("/")) {
                fisheyeBaseUrl += "/";
            }
            // SVN
            svnBaseUrl = this.properties.getStringValue(PropertyKeys.SVN_BASE_URL_PROPERTY,
                    null, false);
            if (svnBaseUrl != null && !svnBaseUrl.endsWith("/")) {
                svnBaseUrl += "/";
            }
            // GitHub
            githubBaseUrl = this.properties.getStringValue(PropertyKeys.GITHUB_BASE_URL_PROPERTY,
                    null, false);
            if (githubBaseUrl != null && !githubBaseUrl.endsWith("/")) {
                githubBaseUrl += "/";
            }

            passThreshold = this.properties.getIntValue(PropertyKeys.PASS_THRESHOLD, 75,
                    false);
            failThreshold = this.properties.getIntValue(PropertyKeys.FAIL_THRESHOLD, 50,
                    false);

            String unimplemented = this.properties.getStringValue(
                    PropertyKeys.UNIMPLEMENTED_TEST_GROUPS, null, false);
            if (unimplemented != null) {
                String[] parts = unimplemented.split(",");
                unimplementedTestGroups = new HashSet<String>();
                for (String part : parts) {
                    if (!"".equals(part.trim())) {
                        unimplementedTestGroups.add(part.trim());
                    }
                }
            }

            String summary = this.properties.getStringValue(PropertyKeys.SUMMARY_TEST_GROUPS, null, false);
            if (summary != null) {
                String[] parts = summary.split(",");
                summaryTestGroups = new HashMap<String, Set<Method>>();
                for (String part : parts) {
                    if (!"".equals(part.trim())) {
                        summaryTestGroups.put(part.trim(), new TreeSet<>(Method.COMPARATOR));
                    }
                }

                for (SpecReference ref : references) {
                    Method method = new Method(ref.getPackageName(), ref.getClassName(), ref.getMethodName(), ref.getGroups());
                    for (String group : summaryTestGroups.keySet()) {
                        if (ref.getGroups().contains(group)) {
                            summaryTestGroups.get(group).add(method);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            // swallow
        }
    }

    /**
     * @param outputDir
     * @throws java.io.IOException
     */
    public void generateToOutputDir(File outputDir) throws IOException {

        System.out.println("Generate coverage report: " + auditParser.getName());

        File coverageFile = new File(outputDir, String.format(REPORT_FILE_NAME, auditParser.getSpecId()));
        FileOutputStream out = new FileOutputStream(coverageFile);

        imageTargetDir = new File(outputDir, "/images");
        targetDir = new File(outputDir.getAbsolutePath());
        
        if (!imageTargetDir.exists()) {
            imageTargetDir.mkdirs();
        }

        copyResource("stickynote.png", "META-INF", imageTargetDir);
        copyResource("blank.png", "META-INF", imageTargetDir);
        copyResource("Chart.js", targetDir);
        generate(out);
    }

    /**
     * @param out
     * @throws java.io.IOException
     */
    public void generate(OutputStream out) throws IOException {
        calculateUnmatched();
        writeHeader();
        writeChapterSummary();
        writeSectionSummary();
        writeCoverage();
        writeTestGroupSummary();
        writeUnmatched();
        writeUnversioned();

        Configuration cfg = new Configuration(Configuration.VERSION_2_3_21);
        cfg.setClassForTemplateLoading(this.getClass(), "/");
        cfg.setDefaultEncoding(REPORT_TEMPLATE_ENCODING);
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        Template temp = cfg.getTemplate(REPORT_TEMPLATE_NAME);

        try {
            temp.process(data, new OutputStreamWriter(out));
        } catch (TemplateException e) {
            e.printStackTrace();
        }
    }
    private void copyResource(String filename, File targetDir) throws IOException {
        copyResource(filename, null, targetDir);
        
    }
    private void copyResource(String filename, String path, File targetDir) throws IOException {
        String completePath = path !=null ? path + File.separator + filename : filename;
        InputStream imageData = this.getClass().getClassLoader()
                .getResourceAsStream(completePath);
        FileOutputStream out = new FileOutputStream(new File(targetDir,
                filename));
        try {
            byte[] buffer = new byte[4096];
            int read = imageData.read(buffer);
            while (read != -1) {
                out.write(buffer, 0, read);
                read = imageData.read(buffer);
            }
            out.flush();
        } finally {
            out.close();
        }
    }

    private void calculateUnmatched() {
        unmatched = new ArrayList<SpecReference>();

        for (String sectionId : references.keySet()) {
            for (SpecReference ref : references.get(sectionId)) {
                if (!unversioned.contains(ref)
                        && !auditParser.hasAssertion(ref.getSection(), ref
                        .getAssertion())) {
                    unmatched.add(ref);
                }
            }
        }
    }

    private void writeHeader() throws IOException {
        data.put("coverageTitle", auditParser.getName());
        data.put("version", auditParser.getVersion());
    }

    private void writeChapterSummary() throws IOException {
        int totalAssertions = 0;
        int totalTestable = 0;
        int totalTested = 0;
        int totalTests = 0;
        int totalUnimplemented = 0;
        int totalImplemented = 0;
        List<TableData> chapterItems = new ArrayList<TableData>();

        for (String sectionId : auditParser.getSectionIds()) {
            // Chapters have no .'s in their id
            if (sectionId.split("[.]").length == 1) {
                String prefix = sectionId + ".";

                int assertions = auditParser.getAssertionsForSection(sectionId).size();
                int testable = 0;
                int testCount = 0;
                int implemented = 0;
                int unimplemented = 0;

                for (Assertion assertion : auditParser
                        .getAssertionsForSection(sectionId)) {
                    if (assertion.isTestable())
                        testable++;

                    TestStatus status = getStatus(getCoverageForAssertion(sectionId,
                            assertion.getId()));
                    if (status.equals(TestStatus.COVERED)) {
                        implemented++;
                    } else if (status.equals(TestStatus.UNIMPLEMENTED)) {
                        unimplemented++;
                    }
                }

                // Gather stats here
                for (String subSectionId : auditParser.getSectionIds()) {
                    if (subSectionId.startsWith(prefix)) {
                        assertions += auditParser.getAssertionsForSection(
                                subSectionId).size();

                        for (Assertion assertion : auditParser
                                .getAssertionsForSection(subSectionId)) {
                            if (assertion.isTestable())
                                testable++;
                            List<SpecReference> coverage = getCoverageForAssertion(
                                    subSectionId, assertion.getId());
                            testCount += coverage.size();
                            TestStatus status = getStatus(coverage);
                            if (status.equals(TestStatus.COVERED)) {
                                implemented++;
                            } else if (status.equals(TestStatus.UNIMPLEMENTED)) {
                                unimplemented++;
                            }
                        }
                    }
                }

                int tested = implemented + unimplemented;

                totalAssertions += assertions;
                totalTestable += testable;
                totalImplemented += implemented;
                totalTested += tested;
                totalTests += testCount;
                totalUnimplemented += unimplemented;

                chapterItems.add(new TableData(auditParser.getSectionById(sectionId), assertions, testable, tested, testCount, unimplemented,
                        implemented,
                        countAndFormatCoverage(testable, implemented)));
            }
        }

        data.put("chapterItems", chapterItems);
        data.put("totalAssertions", String.valueOf(totalAssertions));
        data.put("totalTestable", String.valueOf(totalTestable));
        data.put("totalTested", String.valueOf(totalTested));
        data.put("totalTests", String.valueOf(totalTests));
        data.put("totalUnimplemented", String.valueOf(totalUnimplemented));
        data.put("totalImplemented", String.valueOf(totalImplemented));
        data.put("totalCoveragePercent", countAndFormatCoverage(totalTestable, totalImplemented));

        data.put("failThreshold", failThreshold);
        data.put("passThreshold", passThreshold);
    }

    private void writeSectionSummary() throws IOException {

        List<TableData> sectionItems = new ArrayList<TableData>();

        for (String sectionId : auditParser.getSectionIds()) {

            int assertions = auditParser.getAssertionsForSection(sectionId).size();
            int testable = 0;
            int implemented = 0;
            int unimplemented = 0;

            for (Assertion assertion : auditParser
                    .getAssertionsForSection(sectionId)) {
                if (assertion.isTestable())
                    testable++;

                TestStatus status = getStatus(getCoverageForAssertion(sectionId,
                        assertion.getId()));
                if (status.equals(TestStatus.COVERED)) {
                    implemented++;
                } else if (status.equals(TestStatus.UNIMPLEMENTED)) {
                    unimplemented++;
                }
            }

            int tested = implemented + unimplemented;

            TableData row = new TableData(auditParser.getSectionById(sectionId),
                    assertions,
                    testable, tested, unimplemented, implemented,
                    countAndFormatCoverage(testable, implemented));
            sectionItems.add(row);
        }
        data.put("sectionItems", sectionItems);
    }

    private void writeCoverage() throws IOException {

        final Map<Section, List<SectionElement>> myItems = new LinkedHashMap<>();

        for (String sectionId : auditParser.getSectionIds()) {

            List<SectionElement> items = auditParser.getItemsForSection(sectionId);

            if (items != null && !items.isEmpty()) {
                for (SectionElement item : items) {
                    if (item instanceof Group) {
                        appendAssertionGroup((Group) item);
                    } else if (item instanceof Assertion) {
                        appendAssertion((Assertion) item);
                    }
                }

                myItems.put(auditParser.getSectionById(sectionId), items);
            }
        }
        data.put("items", myItems);
    }

    private void appendAssertionGroup(Group group) throws IOException {
        if (group.getText() == null) {
            throw new IllegalStateException("Group text should not be null " + group);
        }
        for (Assertion assertion : group.getAssertions()) {
            appendAssertion(assertion);
        }
    }

    private void appendAssertion(Assertion assertion) throws IOException {
        List<SpecReference> coverage = getCoverageForAssertion(
                assertion.getSection().getId(), assertion.getId());
        TestStatus status = getStatus(coverage);

        assertion.setStatus(assertion.isTestable() ? status.name() : TestStatus.UNTESTABLE.name());

        if (assertion.getText() == null) {
            throw new IllegalStateException("Error parsing assertion (missing text) " + assertion);
        }
        if (assertion.isTestable()) {

            String currentPackageName = null;

            for (SpecReference ref : coverage) {
                if (!ref.getPackageName().equals(currentPackageName)) {
                    currentPackageName = ref.getPackageName();
                }

                String testArchiveName = getSha1OfTestClass(ref.getPackageName() + "." + ref.getClassName());
                List<Link> urls = new ArrayList<>();
                if (githubBaseUrl != null) {
                    urls.add(new Link(Link.Provider.GITHUB, githubBaseUrl));
                }
                if (fisheyeBaseUrl != null) {
                    urls.add(new Link(Link.Provider.FISHEYE, fisheyeBaseUrl));
                }
                if (svnBaseUrl != null) {
                    urls.add(new Link(Link.Provider.SVN, svnBaseUrl));
                }
                assertion.addTest(new Test(ref.getMethodName(), ref.getClassName(), currentPackageName, testArchiveName, urls));

            }
        }

    }

    private void writeUnmatched() throws IOException {
        if (unmatched.isEmpty())
            return;

        Collections.sort(unmatched, SpecReference.COMPARATOR);
        data.put("unmatched", unmatched);
    }

    private void writeUnversioned() throws IOException {
        if (unversioned.isEmpty())
            return;

        // Classname:version
        Map<String, String> classes = new HashMap<String, String>();
        for (SpecReference ref : unversioned) {
            String key = ref.getPackageName() + "." + ref.getClassName();
            if (!classes.containsKey(key)) {
                classes.put(key, ref.getSpecVersion());
            }
        }
        data.put("unversioned", classes);
    }

    private void writeTestGroupSummary() throws IOException {
        if (summaryTestGroups == null || summaryTestGroups.isEmpty())
            return;
        data.put("sumTestGroups", summaryTestGroups);
    }

    private List<SpecReference> getCoverageForAssertion(String sectionId,
            String assertionId) {
        List<SpecReference> refs = new ArrayList<SpecReference>();

        if (auditParser.hasSectionIdsGenerated()) {
            sectionId = auditParser.getSectionById(sectionId).getOriginalId();
        }

        if (references.containsKey(sectionId)) {
            for (SpecReference ref : references.get(sectionId)) {
                if (ref.getAssertion().equals(assertionId)) {
                    refs.add(ref);
                }
            }
        }
        return refs;
    }

    private TestStatus getStatus(List<SpecReference> references) {
        if (references.isEmpty()) {
            return TestStatus.UNCOVERED;
        }
        for (SpecReference reference : references) {
            if (isImplemented(reference.getGroups())) {
                return TestStatus.COVERED;
            }
        }
        return TestStatus.UNIMPLEMENTED;
    }

    private boolean isImplemented(List<String> groups) {
        for (String group : groups) {
            if (unimplementedTestGroups != null
                    && unimplementedTestGroups.contains(group))
                return false;
        }

        return true;
    }

    private String getSha1OfTestClass(String fqn) {
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
        messageDigest.update(fqn.getBytes());
        byte[] digest = messageDigest.digest();

        StringBuilder hexString = new StringBuilder();
        for (int i = 0; i < digest.length; i++) {
            hexString.append(Integer.toHexString(0xFF & digest[i]));
        }
        return hexString.toString();

    }

    private double countAndFormatCoverage(int totalTestable, int totalImplemented) {
        double totalCoveragePercent = totalTestable > 0 ? ((totalImplemented * 1.0) / totalTestable) * 100
                : -1;
        return totalCoveragePercent > 0 ? totalCoveragePercent : 0;
    }
}

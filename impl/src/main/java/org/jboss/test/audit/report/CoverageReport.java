package org.jboss.test.audit.report;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jboss.test.audit.config.RuntimeProperties;
import org.jboss.test.audit.config.Strings;

/**
 * Generates the TCK spec coverage report
 * 
 * @author Shane Bryzak
 */
public class CoverageReport
{

   public enum TestStatus {
      COVERED, UNCOVERED, UNIMPLEMENTED;
   }

   public static final String FISHEYE_BASE_URL_PROPERTY = "fisheye_base_url";

   public static final String SVN_BASE_URL_PROPERTY = "svn_base_url";

   private static final Pattern PATTERN_BOLD = Pattern.compile("([_][^_]*[_])");
   private static final Pattern PATTERN_STRIKETHROUGH = Pattern
         .compile("([~][^~]*[~])");
   private static final Pattern PATTERN_LITERAL = Pattern
         .compile("([|][^|]*[|])");
   private static final String REPORT_FILE_NAME = "coverage-%s.html";

   private static final String COLOUR_SHADE_GREEN = "#ddffdd";
   private static final String COLOUR_SHADE_RED = "#ffdddd";
   private static final String COLOUR_SHADE_BLUE = "#80d1ff";
   private static final String COLOUR_SHADE_ORANGE = "#ffcc33";
   private static final String COLOUR_SHADE_LIGHT_GREY = "#eeeeee";
   
   private static final String COLOUR_GRAPH_GRADIENT_FROM = "#ff3333";
   private static final String COLOUR_GRAPH_GRADIENT_TO = "#22aa22";

   /*
    * References to the spec assertions made by the test tests
    */
   private final Map<String, List<SpecReference>> references;

   private AuditParser auditParser;

   private File imageSrcDir;
   private File imageTargetDir;

   private RuntimeProperties properties;

   private String fisheyeBaseUrl = null;

   private String svnBaseUrl = null;

   private List<SpecReference> unmatched;
   private List<SpecReference> unversioned;

   private int failThreshold;
   private int passThreshold;
   private Set<String> unimplementedTestGroups;
   private Map<String, Set<Method>> summaryTestGroups;

   public CoverageReport(List<SpecReference> references,
         AuditParser auditParser, File imageSrcDir)
   {
      this.references = new HashMap<String, List<SpecReference>>();

      unversioned = new ArrayList<SpecReference>();
      
      if (references != null)
      {
         for (SpecReference ref : references)
         {
            if (ref.getSpecVersion() == null || !ref.getSpecVersion().equalsIgnoreCase(
                        auditParser.getVersion()))
            {
               unversioned.add(ref);
            }
            else
            {                  
               if (!this.references.containsKey(ref.getSection()))
               {
                  this.references.put(ref.getSection(),
                        new ArrayList<SpecReference>());
               }
      
               this.references.get(ref.getSection()).add(ref);
            }
         }
      }

      this.auditParser = auditParser;
      this.imageSrcDir = imageSrcDir;
      this.properties = new RuntimeProperties();

      try
      {
         fisheyeBaseUrl = this.properties.getStringValue(
               FISHEYE_BASE_URL_PROPERTY, null, false);

         if (!fisheyeBaseUrl.endsWith("/"))
         {
            fisheyeBaseUrl = fisheyeBaseUrl + "/";
         }

         svnBaseUrl = this.properties.getStringValue(SVN_BASE_URL_PROPERTY,
               null, false);

         if (!svnBaseUrl.endsWith("/"))
         {
            svnBaseUrl = svnBaseUrl + "/";
         }

         passThreshold = this.properties.getIntValue("pass_threshold", 75,
               false);
         failThreshold = this.properties.getIntValue("fail_threshold", 50,
               false);

         String unimplemented = this.properties.getStringValue(
               "unimplemented_test_groups", null, false);
         if (unimplemented != null)
         {
            String[] parts = unimplemented.split(",");
            unimplementedTestGroups = new HashSet<String>();
            for (String part : parts)
            {
               if (!"".equals(part.trim()))
               {
                  unimplementedTestGroups.add(part.trim());
               }
            }
         }
         
         String summary = this.properties.getStringValue("summary_test_groups", null, false);
         if (summary != null)
         {
            String[] parts = summary.split(",");
            summaryTestGroups = new HashMap<String, Set<Method>>();
            for (String part : parts)
            {
               if (!"".equals(part.trim()))
               {
                  summaryTestGroups.put(part.trim(), new TreeSet<Method>(Method.COMPARATOR));
               }
            }
            
            for (SpecReference ref : references)
            {
               Method method = new Method(ref.getPackageName(), ref.getClassName(), ref.getMethodName(), ref.getGroups());
               for (String group : summaryTestGroups.keySet())
               {
                  if (ref.getGroups().contains(group))
                  {
                     summaryTestGroups.get(group).add(method);
                  }
               }
            }
         }
      } catch (Exception ex)
      {
         // swallow
      }
   }

   public void generate(File outputDir) throws IOException
   {
      File coverageFile = new File(outputDir, String.format(REPORT_FILE_NAME, auditParser.getSpecId()));
      FileOutputStream out = new FileOutputStream(coverageFile);

      imageTargetDir = new File(outputDir, "/images");
      if (!imageTargetDir.exists())
      {
         imageTargetDir.mkdirs();
      }

      copyResourceImage("stickynote.png");
      copyResourceImage("blank.png");

      calculateUnmatched();
      writeHeader(out);
      writeContents(out);
      writeTestCoverageDistribution(out);
      writeChapterSummary(out);
      writeSectionSummary(out);
      writeCoverage(out);
      writeUnmatched(out);
      writeUnversioned(out);
      writeTestGroupSummary(out);
      writeFooter(out);
   }

   private void copyResourceImage(String filename) throws IOException
   {
      InputStream imageData = this.getClass().getClassLoader()
            .getResourceAsStream("META-INF/" + filename);
      FileOutputStream out = new FileOutputStream(new File(imageTargetDir,
            filename));
      try
      {
         byte[] buffer = new byte[4096];
         int read = imageData.read(buffer);
         while (read != -1)
         {
            out.write(buffer, 0, read);
            read = imageData.read(buffer);
         }

         out.flush();
      } finally
      {
         out.close();
      }
   }

   private void calculateUnmatched()
   {
      unmatched = new ArrayList<SpecReference>();

      for (String sectionId : references.keySet())
      {
         for (SpecReference ref : references.get(sectionId))
         {
            if (!unversioned.contains(ref)
                  && !auditParser.hasAssertion(ref.getSection(), ref
                        .getAssertion()))
            {
               unmatched.add(ref);
            }
         }
      }
   }

   private void writeHeader(OutputStream out) throws IOException
   {
      StringBuilder sb = new StringBuilder();

      sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
      sb.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\"\n");
      sb.append("\"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">\n");
      sb.append("<html>\n");
      sb.append("<head><title>" + auditParser.getName() + " TCK Coverage Report</title>\n");

      sb.append("<style type=\"text/css\">\n");
      sb.append("  body {\n");
      sb.append("   font-family: verdana, arial, sans-serif;\n");
      sb.append("   font-size: 11px; }\n");
      sb.append("  .code {\n");
      sb.append("    float: left;\n");
      sb.append("    font-weight: bold;\n");
      sb.append("    width: 50px;\n");
      sb.append("    margin-top: 0px;\n");
      sb.append("    height: 100%; }\n");
      sb.append("   a.external, a.external:visited, a.external:hover {\n");
      sb.append("    color: #0000ff;\n");
      sb.append("    font-size: 9px;\n");
      sb.append("    font-style: normal;\n");
      sb.append("    padding-left: 2px;\n");
      sb.append("    margin-left: 6px;\n");
      sb.append("    margin-right: 6px;\n");
      sb.append("    padding-right: 2px; }\n");
      sb.append("  .results {\n");
      sb.append("    margin-left: 50px; }\n");
      sb.append("  .description {\n");
      sb.append("    margin-top: 2px;\n");
      sb.append("    margin-bottom: 2px; }\n");
      sb.append("  .sectionHeader {\n");
      sb.append("    border-bottom: 1px solid #cccccc;\n");
      sb.append("    margin-top: 8px;\n");
      sb.append("    font-weight: bold; }\n");
      sb.append("  .packageName {\n");
      sb.append("    color: #999999;\n");
      sb.append("    font-size: 9px;\n");
      sb.append("    font-weight: bold; }\n");
      sb.append("  .groupName {\n");
      sb.append("    color: #0000FF;\n");
      sb.append("    font-size: 12px;\n");
      sb.append("    font-weight: bold; }\n");            
      sb.append("  .embeddedImage {\n");
      sb.append("    margin: 6px;\n");
      sb.append("    border: 1px solid black;\n");
      sb.append("    float: right; }\n");
      sb.append("  .coverage {\n");
      sb.append("    clear: both; }\n");
      sb.append("  .noCoverage {\n");
      sb.append("    margin-top: 2px;\n");
      sb.append("    margin-bottom: 2px;\n");
      sb.append("    font-weight: bold;\n");
      sb.append("    font-style: italic;\n");
      sb.append("    color: #ff0000; }\n");
      sb.append("  .coverageHeader {\n");
      sb.append("    font-weight: bold;\n");
      sb.append("    text-decoration: underline;\n");
      sb.append("    margin-top: 2px;\n");
      sb.append("    margin-bottom: 2px; }\n");
      sb.append("  .coverageMethod {\n");
      sb.append("    font-style: italic; }\n");
      sb.append("  .highlight {\n");
      sb.append("    background-color: #ffff00; }\n");
      sb.append("  .literal {\n");
      sb.append("   font-family: courier new; }\n");
      sb.append("  .implied {\n");
      sb.append("    color: #fff;\n");
      sb.append("    font-weight: bold;\n");
      sb.append("    background-color: #000; }\n");
      sb.append("  .group {\n");
      sb.append("    border-top: 1px solid #000000;\n");
      sb.append("    border-bottom: 1px solid #000000;\n");
      sb.append("    padding-bottom: 1px;\n");
      sb.append("    margin-bottom: 2px;\n");      
      sb.append("    min-height: 36px;\n");
      sb.append("    background-color: " + COLOUR_SHADE_LIGHT_GREY + "; }\n");      
      sb.append("  .groupAssertions {\n");
      sb.append("    padding-bottom: 1px;\n");
      sb.append("    margin-top: 8px;\n");
      sb.append("    margin-left: 50px;\n");
      sb.append("    margin-bottom: 2px;\n");      
      sb.append("    min-height: 36px; \n");
      sb.append("    background-color: ffffff; }\n");                  
      sb.append("  .pass {\n");
      sb.append("    border-top: 1px solid #488c41;\n");
      sb.append("    border-bottom: 1px solid #488c41;\n");
      sb.append("    padding-bottom: 1px;\n");
      sb.append("    margin-bottom: 2px;\n");
      sb.append("    min-height: 36px;\n");
      sb.append("    background-color: " + COLOUR_SHADE_GREEN + "; }\n");
      sb.append("  .fail {\n");
      sb.append("    border-top: 1px solid #ab2020;\n");
      sb.append("    border-bottom: 1px solid #ab2020;\n");
      sb.append("    padding-bottom: 1px;\n");
      sb.append("    margin-bottom: 2px;\n");
      sb.append("    min-height: 36px;\n");
      sb.append("    background-color: " + COLOUR_SHADE_RED + "; }\n");
      sb.append("  .skip {\n");
      sb.append("    border-top: 1px solid #ff9900;\n");
      sb.append("    border-bottom: 1px solid #ff9900;\n");
      sb.append("    padding-bottom: 1px;\n");
      sb.append("    margin-bottom: 2px;\n");
      sb.append("    min-height: 36px;\n");
      sb.append("    background-color: " + COLOUR_SHADE_ORANGE + "; }\n");
      sb.append("  .untestable {\n");
      sb.append("    padding-bottom: 16px;\n");
      sb.append("    margin-bottom: 2px;\n");
      sb.append("    border-top: 1px solid #317ba6;\n");
      sb.append("    border-bottom: 1px solid #317ba6;\n");
      sb.append("    min-height: 36px;\n");
      sb.append("    background-color: " + COLOUR_SHADE_BLUE + "; }\n");
      sb.append("  .stickynote {\n");
      sb
            .append("    background: url(images/stickynote.png) left top no-repeat;\n");
      sb.append("    position: absolute;\n");
      sb.append("    left: 16px;\n");
      sb.append("    width: 30px;\n");
      sb.append("    height: 30px;\n");
      sb.append("    margin-top:16px; }\n");
      sb.append("</style>\n");

      sb.append("</head><body>");
      sb.append("<h1>" + auditParser.getName() + " TCK Coverage</h1>");
      sb.append("<h2>");
      sb.append(auditParser.getVersion());
      sb.append("</h2>\n");

      out.write(sb.toString().getBytes());
   }

   private void writeContents(OutputStream out) throws IOException
   {
      StringBuilder sb = new StringBuilder();

      sb.append("<h3>Contents</h3>\n");
      sb.append("<div><a href=\"#chapterSummary\">Chapter Summary</a></div>\n");
      sb.append("<div><a href=\"#sectionSummary\">Section Summary</a></div>\n");
      sb.append("<div><a href=\"#coverageDetail\">Coverage Detail</a></div>\n");
      sb.append("<div><a href=\"#unmatched\">Unmatched Tests</a></div>\n");
      sb.append("<div><a href=\"#unversioned\">Unversioned Tests</a></div>\n");
      sb.append("<div><a href=\"#groupsummary\">Test Group Summary</a></div>\n");

      out.write(sb.toString().getBytes());
   }
   
   private void writeTestCoverageDistribution(OutputStream out) throws IOException
   {
      StringBuilder sb = new StringBuilder();
      sb.append("<h3 id=\"coverageDistribution\">Coverage Distribution</h3>\n");      
      
      SeriesGenerator gen = new SeriesGenerator();
      
      for (String sectionId : auditParser.getSectionIds())
      {            
         int testable = 0;
         int implemented = 0;
         int unimplemented = 0;
   
         for (AuditAssertion assertion : auditParser
               .getAssertionsForSection(sectionId))
         {
            if (assertion.isTestable())
               testable++;
   
            TestStatus status = getStatus(getCoverageForAssertion(sectionId,
                  assertion.getId()));
            if (status.equals(TestStatus.COVERED))
            {
               implemented++;
            } else if (status.equals(TestStatus.UNIMPLEMENTED))
            {
               unimplemented++;
            }
         }
   
         gen.addValue(testable > 0 ? ((implemented * 1.0) / testable) * 100
               : -1);
      }
      
      sb.append(new BarChartGenerator(COLOUR_GRAPH_GRADIENT_FROM, 
            COLOUR_GRAPH_GRADIENT_TO, gen.getSeries(10, 10)).generate());
      
      out.write(sb.toString().getBytes());
   }

   private void writeChapterSummary(OutputStream out) throws IOException
   {
      StringBuilder sb = new StringBuilder();

      sb.append("<h3 id=\"chapterSummary\">Chapter Summary</h3>\n");

      sb.append("<table width=\"100%\">\n");

      sb.append("<tr style=\"background-color:#dddddd\">\n");
      sb.append("  <th align=\"left\">Chapter</th>\n");
      sb.append("  <th>Assertions</th>\n");
      sb.append("  <th>Testable</th>\n");
      sb.append("  <th>Total Tested</th>\n");
      sb.append("  <th>Tested<br /> (problematic)</th>\n");
      sb.append("  <th>Tested<br /> (working)</th>\n");
      sb.append("  <th>Coverage %</th>\n");
      sb.append("</tr>\n");

      boolean odd = true;

      int totalAssertions = 0;
      int totalTestable = 0;
      int totalTested = 0;
      int totalUnimplemented = 0;
      int totalImplemented = 0;

      for (String sectionId : auditParser.getSectionIds())
      {

         // Chapters have no .'s in their id
         if (sectionId.split("[.]").length == 1)
         {
            String prefix = sectionId + ".";

            int assertions = auditParser.getAssertionsForSection(sectionId).size();
            int testable = 0;
            int implemented = 0;
            int unimplemented = 0;

            for (AuditAssertion assertion : auditParser
                  .getAssertionsForSection(sectionId))
            {
               if (assertion.isTestable())
                  testable++;

               TestStatus status = getStatus(getCoverageForAssertion(sectionId,
                     assertion.getId()));
               if (status.equals(TestStatus.COVERED))
               {
                  implemented++;
               } else if (status.equals(TestStatus.UNIMPLEMENTED))
               {
                  unimplemented++;
               }
            }

            // Gather stats here
            for (String subSectionId : auditParser.getSectionIds())
            {
               if (subSectionId.startsWith(prefix))
               {
                  assertions += auditParser.getAssertionsForSection(
                        subSectionId).size();

                  for (AuditAssertion assertion : auditParser
                        .getAssertionsForSection(subSectionId))
                  {
                     if (assertion.isTestable())
                        testable++;

                     TestStatus status = getStatus(getCoverageForAssertion(
                           subSectionId, assertion.getId()));
                     if (status.equals(TestStatus.COVERED))
                     {
                        implemented++;
                     } else if (status.equals(TestStatus.UNIMPLEMENTED))
                     {
                        unimplemented++;
                     }
                  }
               }
            }

            int tested = implemented + unimplemented;

            double coveragePercent = testable > 0 ? ((implemented * 1.0) / testable) * 100
                  : -1;

            totalAssertions += assertions;
            totalTestable += testable;
            totalImplemented += implemented;
            totalTested += tested;
            totalUnimplemented += unimplemented;

            if (odd)
            {
               sb.append("<tr style=\"background-color:#f7f7f7\">");
            } else
            {
               sb.append("<tr>");
            }

            odd = !odd;

            int margin = (sectionId.split("[.]").length - 1) * 16;

            sb.append("<td style=\"padding-left:" + margin + "px\">");
            sb.append("<a href=\"#" + sectionId + "\">");
            sb.append(sectionId);
            sb.append(" ");
            sb.append(auditParser.getSectionTitle(sectionId));
            sb.append("</a>");
            sb.append("</td>");

            sb.append("<td align=\"center\">");
            sb.append(assertions);
            sb.append("</td>");

            sb.append("<td align=\"center\">");
            sb.append(testable);
            sb.append("</td>");

            sb.append("<td align=\"center\">");
            sb.append(tested);
            sb.append("</td>");

            sb.append("<td align=\"center\">");
            sb.append(unimplemented);
            sb.append("</td>");

            sb.append("<td align=\"center\">");
            sb.append(implemented);
            sb.append("</td>");

            if (coveragePercent >= 0)
            {
               String bgColor = coveragePercent < failThreshold ? "#ffaaaa"
                     : coveragePercent < passThreshold ? "#ffffaa"
                           : coveragePercent > 100 ? "#FF00CC" : "#aaffaa";

               sb.append("<td align=\"center\" style=\"background-color:"
                     + bgColor + "\">");
               sb.append(String.format("%.2f%%", coveragePercent));
               sb.append("</td>");
            } else
            {
               sb.append("<td />");
            }

            sb.append("</tr>");

         }

      }

      sb.append("<tr style=\"font-weight: bold;background-color:#dddddd\">");

      sb.append("<td>");
      sb.append("Total");
      sb.append("</td>");

      sb.append("<td align=\"center\">");
      sb.append(totalAssertions);
      sb.append("</td>");

      sb.append("<td align=\"center\">");
      sb.append(totalTestable);
      sb.append("</td>");

      sb.append("<td align=\"center\">");
      sb.append(totalTested);
      sb.append("</td>");

      sb.append("<td align=\"center\">");
      sb.append(totalUnimplemented);
      sb.append("</td>");

      sb.append("<td align=\"center\">");
      sb.append(totalImplemented);
      sb.append("</td>");

      double totalCoveragePercent = totalTestable > 0 ? ((totalImplemented * 1.0) / totalTestable) * 100
            : -1;

      if (totalCoveragePercent >= 0)
      {
         String bgColor = totalCoveragePercent < failThreshold ? "#ffaaaa"
               : totalCoveragePercent < passThreshold ? "#ffffaa" : "#aaffaa";

         sb.append("<td align=\"center\" style=\"background-color:" + bgColor
               + "\">");
         sb.append(String.format("%.2f%%", totalCoveragePercent));
         sb.append("</td>");
      } else
      {
         sb.append("<td />");
      }

      sb.append("</tr>");

      sb.append("</table>");
      out.write(sb.toString().getBytes());
   }

   private void writeSectionSummary(OutputStream out) throws IOException
   {
      StringBuilder sb = new StringBuilder();

      sb.append("<h3 id=\"sectionSummary\">Section Summary</h3>\n");

      sb.append("<table width=\"100%\">");

      sb.append("<tr style=\"background-color:#dddddd\">");
      sb.append("<th align=\"left\">Section</th>");
      sb.append("<th>Assertions</th>");
      sb.append("<th>Testable</th>");
      sb.append("<th>Total Tested</th>");
      sb.append("<th>Tested<br /> (problematic)</th>");
      sb.append("<th>Tested<br /> (working)</th>");
      sb.append("<th>Coverage %</th>");
      sb.append("</tr>");

      boolean odd = true;

      for (String sectionId : auditParser.getSectionIds())
      {

         if (odd)
         {
            sb.append("<tr style=\"background-color:#f7f7f7\">");
         } else
         {
            sb.append("<tr>");
         }

         odd = !odd;

         int margin = (sectionId.split("[.]").length - 1) * 16;

         sb.append("<td style=\"padding-left:" + margin + "px\">");
         sb.append("<a href=\"#" + sectionId + "\">");
         sb.append(sectionId);
         sb.append(" ");
         sb.append(auditParser.getSectionTitle(sectionId));
         sb.append("</a>");
         sb.append("</td>");

         int assertions = auditParser.getAssertionsForSection(sectionId).size();
         int testable = 0;
         int implemented = 0;
         int unimplemented = 0;

         for (AuditAssertion assertion : auditParser
               .getAssertionsForSection(sectionId))
         {
            if (assertion.isTestable())
               testable++;

            TestStatus status = getStatus(getCoverageForAssertion(sectionId,
                  assertion.getId()));
            if (status.equals(TestStatus.COVERED))
            {
               implemented++;
            } else if (status.equals(TestStatus.UNIMPLEMENTED))
            {
               unimplemented++;
            }
         }

         int tested = implemented + unimplemented;

         double coveragePercent = testable > 0 ? ((implemented * 1.0) / testable) * 100
               : -1;

         sb.append("<td align=\"center\">");
         sb.append(assertions);
         sb.append("</td>");

         sb.append("<td align=\"center\">");
         sb.append(testable);
         sb.append("</td>");

         sb.append("<td align=\"center\">");
         sb.append(tested);
         sb.append("</td>");

         sb.append("<td align=\"center\">");
         sb.append(unimplemented);
         sb.append("</td>");

         sb.append("<td align=\"center\">");
         sb.append(implemented);
         sb.append("</td>");

         if (coveragePercent >= 0)
         {
            String bgColor = coveragePercent < failThreshold ? "#ffaaaa"
                  : coveragePercent < passThreshold ? "#ffffaa"
                        : coveragePercent > 100 ? "#FF00CC" : "#aaffaa";

            sb.append("<td align=\"center\" style=\"background-color:"
                  + bgColor + "\">");
            sb.append(String.format("%.2f%%", coveragePercent));
            sb.append("</td>");
         } else
         {
            sb.append("<td />");
         }

         sb.append("</tr>");
      }

      sb.append("</table>");
      out.write(sb.toString().getBytes());
   }

   private void writeCoverage(OutputStream out) throws IOException
   {

      out.write("<h3 id=\"coverageDetail\">Coverage Detail</h3>\n".getBytes());

      StringBuilder key = new StringBuilder();
      key.append("<table>\n");
      key
            .append("  <tr><th style=\"background-color:#dddddd\">Colour Key</th></tr>\n");
      key.append("  <tr><td style=\"background-color:" + COLOUR_SHADE_GREEN
            + ";text-align:center\">Assertion is covered</td></tr>\n");
      key.append("  <tr><td style=\"background-color:" + COLOUR_SHADE_RED
            + ";text-align:center\">Assertion is not covered</td></tr>\n");
      key.append("  <tr><td style=\"background-color:" + COLOUR_SHADE_ORANGE
            + ";text-align:center\">Assertion test is unimplemented</td></tr>\n");
      key.append("  <tr><td style=\"background-color:" + COLOUR_SHADE_BLUE
            + ";text-align:center\">Assertion is untestable</td></tr>\n");
      key.append("</table>\n");
      out.write(key.toString().getBytes());

      for (String sectionId : auditParser.getSectionIds())
      {
         List<SectionItem> items = auditParser.getItemsForSection(sectionId);
         
         
         //List<AuditAssertion> sectionAssertions = auditParser
               ///getAssertionsForSection(sectionId);

         if (items != null && !items.isEmpty())
         {
            StringBuilder sb = new StringBuilder();

            out.write(("<h4 class=\"sectionHeader\" id=\"" + sectionId
                  + "\">Section " + sectionId + " - "
                  + escape(auditParser.getSectionTitle(sectionId)) + "</h4>\n")
                  .getBytes());

            for (SectionItem item : items)
            {
               if (item instanceof AssertionGroup)
               {
                  appendAssertionGroup(sb, (AssertionGroup) item);
               }
               else if (item instanceof AuditAssertion)
               {
                  appendAssertion(sb, (AuditAssertion) item);
               }
            }

            out.write(sb.toString().getBytes());
         } else
         {
            // We still want to be able to jump to this section by clicking on
            // the links
            // in the chapter and section summaries
            out.write(("<div style=\"visibility:hidden\" id=\"" + sectionId + "\"></div>\n")
                        .getBytes());
         }
      }
   }
   
   private void appendAssertionGroup(StringBuilder sb, AssertionGroup group) throws IOException
   {
      sb.append("  <div class=\"group\">\n");
      sb.append("    <p class=\"description\">");
      String text = parseStrikethrough(parseBold(parseLiteral(escape(group.getText()))));
      sb.append(text);
      sb.append("</p>\n");      
      
      sb.append("    <div class=\"groupAssertions\">\n");
      for (AuditAssertion assertion : group.getAssertions())
      {
         appendAssertion(sb, assertion);
      }
      sb.append("    </div>\n");
      
      sb.append("  </div>\n");
   }
   
   private void appendAssertion(StringBuilder sb, AuditAssertion assertion) throws IOException
   {
      List<SpecReference> coverage = getCoverageForAssertion(
            assertion.getSection(), assertion.getId());
      TestStatus status = getStatus(coverage);

      String divClass = null;

      if (assertion.isTestable())
      {
         if (status.equals(TestStatus.UNCOVERED))
         {
            divClass = "fail";            
         } 
         else if (status.equals(TestStatus.UNIMPLEMENTED))
         {
            divClass = "skip";
         } 
         else
         {
            divClass = "pass";
         }
      } 
      else
      {
         divClass = "untestable";
      }

      sb.append("  <div class=\"" + divClass + "\">\n");

      if (assertion.isImplied())
      {
         sb.append("<span class=\"implied\">The following assertion is not made explicitly by the spec, however it is implied</span>");
      }

      sb.append("    <span class=\"code\">");
      sb.append(assertion.getId());
      sb.append(")");

      if (!Strings.isEmpty(assertion.getNote()))
      {
         sb.append("<img title=\"" + assertion.getNote()
               + "\" src=\"images/blank.png\" class=\"stickynote\"/>");
      }

      sb.append("</span>\n");
      sb.append("    <div class=\"results\">");

      sb.append("<p class=\"description\">");
      String imageFilename = assertion.getSection() + "." + assertion.getId()
            + ".png";
      File imageFile = new File(imageSrcDir, imageFilename);

      if (imageFile.exists())
      {
         sb.append("<img src=\"images/" + imageFile.getName()
               + "\" class=\"embeddedImage\"/>");
         copyFile(imageFile, new File(imageTargetDir, imageFilename));
      }

      String assertionText = parseStrikethrough(parseBold(parseLiteral(escape(assertion
            .getText()))));
      sb.append(assertionText);
      sb.append("</p>\n");

      if (assertion.isTestable())
      {
         sb.append("    <div class=\"coverage\">\n");
         sb.append("      <p class=\"coverageHeader\">Coverage</p>\n");

         String currentPackageName = null;

         if (status.equals(TestStatus.UNCOVERED))
         {
            sb.append("        <p class=\"noCoverage\">No tests exist for this assertion</p>\n");
         } else
         {
            for (SpecReference ref : coverage)
            {
               if (!ref.getPackageName().equals(currentPackageName))
               {
                  currentPackageName = ref.getPackageName();
                  sb.append("        <div class=\"packageName\">");
                  sb.append(currentPackageName);
                  sb.append("        </div>\n");
               }

               sb.append("        <div class=\"coverageMethod\">");
               sb.append(ref.getClassName());
               sb.append(".");
               sb.append(ref.getMethodName());
               sb.append("()");

               if (fisheyeBaseUrl != null)
               {
                  sb.append("<a class=\"external\" target=\"_blank\" href=\"");
                  sb.append(fisheyeBaseUrl);
                  sb.append(currentPackageName.replace('.', '/'));
                  sb.append("/");
                  sb.append(ref.getClassName());
                  sb.append(".java");
                  sb.append("\">fisheye</a>");
               }

               if (svnBaseUrl != null)
               {
                  if (fisheyeBaseUrl != null)
                  {
                     sb.append("|");
                  }

                  sb.append("<a class=\"external\" target=\"_blank\" href=\"");
                  sb.append(svnBaseUrl);
                  sb.append(currentPackageName.replace('.', '/'));
                  sb.append("/");
                  sb.append(ref.getClassName());
                  sb.append(".java");
                  sb.append("\">svn</a>");
               }

               sb.append("</div>\n");
            }
         }

         sb.append("    </div>\n");
      } 
      else if (!coverage.isEmpty())
      {
         sb.append("<b>A test exists for this untestable assertion!</b>");
      }

      sb.append("</div></div>\n");      
   }

   private String parseBold(String text)
   {
      Matcher m = PATTERN_BOLD.matcher(text);

      String result = text;
      while (m.find())
      {
         String replacement = "<span class=\"highlight\">"
               + m.group().substring(1, m.group().length() - 1) + "</span>";
         result = m.replaceFirst(replacement);
         m.reset(result);
      }
      return result;
   }

   private String escape(String value)
   {
      return value.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
   }

   private String parseStrikethrough(String text)
   {
      Matcher m = PATTERN_STRIKETHROUGH.matcher(text);

      String result = text;
      while (m.find())
      {
         String replacement = "<del>"
               + m.group().substring(1, m.group().length() - 1) + "</del>";
         result = m.replaceFirst(replacement);
         m.reset(result);
      }
      return result;
   }

   private String parseLiteral(String text)
   {
      Matcher m = PATTERN_LITERAL.matcher(text);

      String result = text;
      while (m.find())
      {
         String replacement = "<span class=\"literal\">"
               + m.group().substring(1, m.group().length() - 1) + "</span>";
         result = m.replaceFirst(replacement);
         m.reset(result);
      }
      return result;
   }

   private void writeUnmatched(OutputStream out) throws IOException
   {
      if (unmatched.isEmpty())
         return;

      StringBuilder sb = new StringBuilder();

      sb.append("<h3 id=\"unmatched\">Unmatched tests</h3>\n");
      sb.append(String.format(
            "<p>The following %d tests do not match any known assertions:</p>",
            unmatched.size()));

      sb.append("<table border=\"1\" cellspacing=\"0\" cellpadding=\"0\">\n");
      sb
            .append("  <tr><th>Section</th><th>Assertion</th><th>Test Class</th><th>Test Method</th></tr>\n");

      Collections.sort(unmatched, SpecReference.COMPARATOR);
      for (SpecReference ref : unmatched)
      {
         sb.append("<tr>");

         sb.append("<td>");
         sb.append(ref.getSection());
         sb.append("</td>");

         sb.append("<td>");
         sb.append(ref.getAssertion());
         sb.append("</td>");

         sb.append("<td>");
         sb.append("<div class=\"packageName\">");
         sb.append(ref.getPackageName());
         sb.append("</div>");
         sb.append(ref.getClassName());
         sb.append("</td>");

         sb.append("<td>");
         sb.append(ref.getMethodName());
         sb.append("()");
         sb.append("</td>");

         sb.append("</tr>");
      }

      sb.append("</table>");

      out.write(sb.toString().getBytes());
   }
   
   private void writeUnversioned(OutputStream out) throws IOException
   {
      if (unversioned.isEmpty())
         return;
      
      // Classname:version
      Map<String,String> classes = new HashMap<String,String>();
      for (SpecReference ref : unversioned)
      {
         String key = ref.getPackageName() + "." + ref.getClassName();
         if (!classes.containsKey(key))
         {
            classes.put(key, ref.getSpecVersion());
         }
      }

      StringBuilder sb = new StringBuilder();

      sb.append("<h3 id=\"unversioned\">Unversioned tests</h3>\n");
      sb.append(String.format(
            "<p>The following %d test classes either do not have a version specified, or the version is unrecognized:</p>",
            classes.size()));

      sb.append("<table border=\"1\" cellspacing=\"0\" cellpadding=\"0\">\n");
      sb.append("  <tr><th>Test Class</th><th>Version</th></tr>\n");

      for (String cls : classes.keySet())
      {
         sb.append("<tr>");

         sb.append("<td>");
         sb.append(cls);
         sb.append("</td>");

         sb.append("<td>");
         sb.append(classes.get(cls));
         sb.append("</td>");

         sb.append("</tr>");
      }

      sb.append("</table>");

      out.write(sb.toString().getBytes());            
   }
   
   private void writeTestGroupSummary(OutputStream out) throws IOException
   {
      if (summaryTestGroups == null || summaryTestGroups.isEmpty()) return;
      
      StringBuilder sb = new StringBuilder();
      
      sb.append("<h3 id=\"groupsummary\">Highlighted test groups</h3>\n");
      sb.append("<table border=\"1\" cellspacing=\"0\" cellpadding=\"0\">\n");
      sb.append("  <tr><th>Test Class</th><th>Test method</th></tr>\n");
      
      for (String group : summaryTestGroups.keySet())
      {
         sb.append("<tr><td colspan=\"2\">");
         sb.append("<div class=\"groupName\">");
         sb.append(group);
         sb.append(" (").append(summaryTestGroups.get(group).size()).append(")");
         sb.append("</div>");
         sb.append("</td></tr>");
         
         summaryTestGroups.get(group);
         
         for (Method ref : summaryTestGroups.get(group))
         {
            sb.append("<tr><td>");
            sb.append("<div class=\"packageName\">");
            sb.append(ref.getPackageName());
            sb.append("</div>");            
            sb.append(ref.getClassName());
            sb.append("</td><td>");
            sb.append(ref.getMethodName());            
            sb.append("()</td></tr>");
         }
      }
      
      sb.append("</table>");
      out.write(sb.toString().getBytes());
   }

   private List<SpecReference> getCoverageForAssertion(String sectionId,
         String assertionId)
   {
      List<SpecReference> refs = new ArrayList<SpecReference>();

      if (references.containsKey(sectionId))
      {
         for (SpecReference ref : references.get(sectionId))
         {
            if (ref.getAssertion().equals(assertionId))
            {
               refs.add(ref);
            }
         }
      }

      return refs;
   }

   private TestStatus getStatus(List<SpecReference> references)
   {
      if (references.isEmpty())
      {
         return TestStatus.UNCOVERED;
      }
      for (SpecReference reference : references)
      {
         if (isImplemented(reference.getGroups()))
         {
            return TestStatus.COVERED;
         }
      }
      return TestStatus.UNIMPLEMENTED;
   }

   private boolean isImplemented(List<String> groups)
   {
      for (String group : groups)
      {
         if (unimplementedTestGroups != null
               && unimplementedTestGroups.contains(group))
            return false;
      }

      return true;
   }

   private void writeFooter(OutputStream out) throws IOException
   {
      out.write("</table>".getBytes());
      out.write("</body></html>".getBytes());
   }

   private void copyFile(File sourceFile, File targetFile) throws IOException
   {
      FileChannel inChannel = new FileInputStream(sourceFile).getChannel();
      FileChannel outChannel = new FileOutputStream(targetFile).getChannel();
      try
      {
         inChannel.transferTo(0, inChannel.size(), outChannel);
      } catch (IOException e)
      {
         throw e;
      } finally
      {
         if (inChannel != null)
            inChannel.close();
         if (outChannel != null)
            outChannel.close();
      }
   }
}

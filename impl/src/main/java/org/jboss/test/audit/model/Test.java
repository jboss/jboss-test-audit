package org.jboss.test.audit.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tomas Remes
 */
public class Test {

    private String testName;
    private String packageName;
    private String archiveName;
    private String className;
    private List<Link> links;

    public Test(String methodName, String className, String packageName, String archiveName, List<Link> links) {
        this.testName = className + "." + methodName + "()";
        this.className = className;
        this.packageName = packageName;
        this.archiveName = archiveName;
        this.links = adjustLinks(links);
    }

    public String getTestName() {
        return testName;
    }

    public List<Link> getLinks() {
        return links;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getArchiveName() {
        return archiveName;
    }

    public void setArchiveName(String archiveName) {
        this.archiveName = archiveName;
    }

    private List<Link> adjustLinks(List<Link> originalURLs) {

        List<Link> newLinks = new ArrayList<>();
        if (originalURLs != null) {
            for (Link link : originalURLs) {

                String newUrl = link.getUrl()+ "/" + (packageName.replace(".", "/")) + "/" + className + ".java";
                newLinks.add(new Link(link.getProvider(), newUrl));
            }
        }
        return newLinks;

    }

}

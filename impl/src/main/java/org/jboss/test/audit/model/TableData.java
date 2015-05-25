package org.jboss.test.audit.model;

/**
 * @author Tomas Remes
 */
public class TableData {

    private final Section section;
    private final int assertions;
    private final int testable;
    private final int tested;
    private final int testCount;
    private final int unimplemented;
    private final int implemented;
    private final double coverage;

    public TableData(Section section, int assertions, int testable, int tested, int unimplemented, int implemented,
            double coverage) {
        this(section, assertions, testable, tested, 0, unimplemented, implemented, coverage);
    }

    public TableData(Section section, int assertions, int testable, int tested, int testCount, int unimplemented,
            int implemented, double coverage) {
        this.section = section;
        this.assertions = assertions;
        this.testable = testable;
        this.tested = tested;
        this.testCount = testCount;
        this.unimplemented = unimplemented;
        this.implemented = implemented;
        this.coverage = coverage;

    }

    public double getCoverage() {
        return coverage;
    }

    public Section getSection() {
        return section;
    }

    public int getAssertions() {
        return assertions;
    }

    public int getTestable() {
        return testable;
    }

    public int getTested() {
        return tested;
    }

    public int getUnimplemented() {
        return unimplemented;
    }

    public int getImplemented() {
        return implemented;
    }

    public int getTestCount() {
        return testCount;
    }

    public String displayCoverage() {

        return this.assertions > 0 ? String.valueOf(((double) Math.round(this.coverage * 100) / 100)) : "";
    }

    public String coverageForGraph() {

        return String.valueOf(((double) Math.round(this.coverage * 100) / 100)).replace(",", ".");
    }

}

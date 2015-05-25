package org.jboss.test.audit.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tomas Remes
 */
public class Assertion extends SectionElement {

    private String id = "";
    private String note;
    private boolean testable;
    private boolean implied;
    private String status;
    private Group group;
    private List<Test> tests;

    public Assertion(String id, String text, String note, boolean testable, boolean implied, Group group, Section section) {
        super(text, section);
        this.id = id;
        this.note = note;
        this.testable = testable;
        this.implied = implied;
        this.group = group;
        this.tests = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Test> getTests() {
        return tests;
    }

    public boolean isTestable() {
        return testable;
    }

    public void setTestable(boolean testable) {
        this.testable = testable;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public boolean isImplied() {
        return implied;
    }

    public void setImplied(boolean implied) {
        this.implied = implied;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    
    public void addTest(Test test){
        tests.add(test);
    }

    @Override
    public String toString() {
        return this.getId() + " " + this.getText() + " " + this.getNote() + " " + this.getStatus() + " " + this.isImplied() + " " + this.isTestable();
    }

}

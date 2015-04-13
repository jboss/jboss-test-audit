package org.jboss.test.audit.model;

/**
 * @author Tomas Remes
 */
public class SectionElement {

    private String text;

    private Section section;
    
    public SectionElement(String text, Section section){
        this.text = text;
        this.section = section;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Section getSection() {
        return section;
    }

    public void setSection(Section section) {
        this.section = section;
    }
}

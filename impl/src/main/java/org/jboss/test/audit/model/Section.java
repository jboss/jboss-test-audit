package org.jboss.test.audit.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tomas Remes
 */
public class Section {

    private String id;
    private String title;
    private int level;
    private String originalId;
    private List<SectionElement> sectionElements;

    public Section(String id, String title, int level, String originalId) {
        this.id = id;
        this.title = title;
        this.level = level;
        this.originalId = originalId;
        this.sectionElements = new ArrayList<>();
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<SectionElement> getSectionElements() {
        return sectionElements;
    }

    public String getOriginalId() {
        return originalId;
    }

    public void setOriginalId(String originalId) {
        this.originalId = originalId;
    }

    @Override
    public String toString() {
        return this.getId() + " " + this.getTitle() + " " + this.getLevel();
    }

}

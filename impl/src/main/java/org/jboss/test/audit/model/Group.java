package org.jboss.test.audit.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tomas Remes
 */
public class Group extends SectionElement {


    private List<Assertion> assertions;

    public Group(String text, Section section){
        super(text, section);
        this.assertions = new ArrayList<>();
    }

    public List<Assertion> getAssertions() {
        return assertions;
    }
    
    public void addAssertion(Assertion assertion){
          assertions.add(assertion);
    }

    @Override
    public String toString(){
        return super.getText();
    }
    


}

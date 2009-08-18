package org.jboss.test.audit.report;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a group of assertions, with each assertion testing an aspect of 
 * a statement made by the spec.
 *  
 * @author Shane Bryzak
 */
public class AssertionGroup extends SectionItem
{
   private List<AuditAssertion> assertions;
   
   public AssertionGroup(String section)
   {
      setSection(section);
      assertions = new ArrayList<AuditAssertion>();
   }
      
   public void addAssertion(AuditAssertion assertion)
   {
      assertions.add(assertion);
   }
   
   public List<AuditAssertion> getAssertions()
   {
      return assertions;
   }

   @Override
   public int compareTo(SectionItem other)
   {
      return 0;
   }   
}

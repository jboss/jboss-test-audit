package org.jboss.test.audit.report;

/**
 * Represents a single assertion as defined in the audit xml 
 * 
 * @author Shane Bryzak
 *
 */
public class AuditAssertion extends SectionItem 
{
   private String id;
   private String note;
   private boolean testable;
   private boolean implied;
   private AssertionGroup group;
   
   public AuditAssertion(String section, String id, String text, String note, 
         boolean testable, boolean implied, AssertionGroup group)
   {
      setSection(section);
      setText(text);
      this.id = id;
      this.note = note;
      this.testable = testable;
      this.implied = implied;
      this.group = group;
   }
   
   public String getId()
   {
      return id;
   }

   public String getNote()
   {
      return note;
   }
   
   public boolean isTestable()
   {
      return testable;
   }
   
   public boolean isImplied()
   {
      return implied;
   }
   
   public AssertionGroup getGroup()
   {
      return group;
   }

   @Override
   public int compareTo(SectionItem other)
   {
      int i = getSection().compareTo(other.getSection());
      if (i != 0)
      {
         return i;
      }
      else
      {
         if (other instanceof AuditAssertion)
         {
            return id.compareTo(((AuditAssertion) other).id);
         }
         else
         {
            return 0;
         }
      }
   }
   
}

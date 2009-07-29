package org.jboss.test.audit.report;

/**
 * Base abstract class for items found within a section
 *  
 * @author Shane Bryzak
 */
public abstract class SectionItem implements Comparable<SectionItem>
{
   private String section;
   private String text;
   
   public String getSection()
   {
      return section;
   }
   
   public void setSection(String section)
   {
      this.section = section;
   }
   
   public String getText()
   {
      return text;   
   }
   
   public void setText(String text)
   {
      this.text = text;
   }

   public abstract int compareTo(SectionItem other);
}

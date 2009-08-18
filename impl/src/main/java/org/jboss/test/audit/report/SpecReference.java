package org.jboss.test.audit.report;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Represents the metadata for a single instance of @SpecAssertion
 * 
 * @author Shane Bryzak
 */
public class SpecReference
{
   
   private static class SpecReferenceComparator implements Comparator<SpecReference>
   {

      @Override
      public int compare(SpecReference arg0, SpecReference arg1)
      { 
         return arg0.getFqn().compareToIgnoreCase(arg1.getFqn());
      }

   }
   
   public static final Comparator<SpecReference> COMPARATOR = new SpecReferenceComparator();
   
   private String specId;
   private String specVersion;
   private String section;
   private String assertion;
   private String packageName;
   private String className;
   private String methodName;
   private List<String> groups;

   public SpecReference()
   {
      this.groups = new ArrayList<String>();
   }
   
   public void setSpecId(String specId)
   {
      this.specId = specId;
   }
   
   public void setSpecVersion(String specVersion)
   {
      this.specVersion = specVersion;
   }

   public void setSection(String section)
   {
      this.section = section;
   }

   public void setAssertion(String assertion)
   {
      this.assertion = assertion;
   }

   public void setPackageName(String packageName)
   {
      this.packageName = packageName;
   }

   public void setClassName(String className)
   {
      this.className = className;
   }

   public void setMethodName(String methodName)
   {
      this.methodName = methodName;
   }
   
   public String getSpecId()
   {
      return specId;
   }
   
   public String getSpecVersion()
   {
      return specVersion;
   }

   public String getSection()
   {
      return section;
   }

   public String getAssertion()
   {
      return assertion;
   }

   public String getPackageName()
   {
      return packageName;
   }

   public String getClassName()
   {
      return className;
   }

   public String getMethodName()
   {
      return methodName;
   }

   public List<String> getGroups()
   {
      return groups;
   }

   @Override
   public String toString()
   {
      return "SpecReference[version=" + specVersion + ";section=" + section + ";assertion=" + assertion
            + ";class=" + packageName + "." + className + ";method="
            + methodName + "]";
   }
   
   public String getFqn()
   {
      return packageName + "." + className + "." + methodName;
   }
}

package org.jboss.test.audit.report;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.jboss.test.audit.config.RuntimeProperties;
import org.jboss.test.audit.generate.SectionIdGenerator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Parsing utilities for tck-audit.xml
 *
 * @author Shane Bryzak
 *
 */
public class AuditParser
{

   private SectionIdGenerator sectionIdGenerator;

   private String name;
   private String specId;
   private String version;

   private Map<String,List<SectionItem>> sectionItems = new HashMap<String,List<SectionItem>>();

   private Map<String,String> titles = new HashMap<String,String>();
   private Map<String,String> generatedToOriginalIdMap = new HashMap<String,String>();
   private Map<String,String> originalToGeneratedIdMap = new HashMap<String,String>();

   private InputStream source;

   public AuditParser(InputStream source, RuntimeProperties properties)
   {
      this.source = source;
   }

   public String getName()
   {
      return name;
   }

   public String getSpecId()
   {
      return specId;
   }

   public String getVersion()
   {
      return version;
   }

   public String getSectionTitle(String sectionId)
   {
      return titles.get(sectionId);
   }

   public Map<String,List<SectionItem>> getSectionItems()
   {
      return sectionItems;
   }

   public List<String> getSectionIds()
   {
      List<String> sectionIds = new ArrayList<String>(sectionItems.keySet());

      Collections.sort(sectionIds, new SectionIdComparator());
      return sectionIds;
   }

   /**
    *
    * @param sectionId
    * @return
    */
   public List<AuditAssertion> getAssertionsForSection(String sectionId)
   {
      List<AuditAssertion> assertions = new ArrayList<AuditAssertion>();

      for (SectionItem item : sectionItems.get(sectionId))
      {
         if (item instanceof AuditAssertion)
         {
            assertions.add((AuditAssertion) item);
         }
         else if (item instanceof AssertionGroup)
         {
            for (AuditAssertion assertion : ((AssertionGroup) item).getAssertions())
            {
               assertions.add(assertion);
            }
         }
      }

      return assertions;
   }

   /**
    * Returns a list of items for the specified section ID
    *
    * @param sectionId
    * @return
    */
   public List<SectionItem> getItemsForSection(String sectionId)
   {
      List<SectionItem> items = new ArrayList<SectionItem>(sectionItems.get(sectionId));
      return items;
   }

   /**
    *
    * @param sectionId
    * @param assertionId
    * @return
    */
   public boolean hasAssertion(String sectionId, String assertionId)
   {
      if(sectionIdGenerator != null) {
    	  // Note that sectionId has the original value
    	  sectionId = originalToGeneratedIdMap.get(sectionId);
      }

	  if (!sectionItems.containsKey(sectionId))
      {
         return false;
      }

      for (SectionItem item : sectionItems.get(sectionId))
      {
         if (item instanceof AuditAssertion && ((AuditAssertion) item).getId().equals(assertionId))
         {
            return true;
         }
         else if (item instanceof AssertionGroup)
         {
            for (AuditAssertion assertion : ((AssertionGroup) item).getAssertions())
            {
               if (assertion.getId().equals(assertionId)) return true;
            }
         }
      }

      return false;
   }

   /**
    * Load the spec assertions defined in tck-audit.xml
    */
   public AuditParser parse() throws Exception
   {
      DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

      Document doc = builder.parse(source);
      NodeList sectionNodes = doc.getDocumentElement().getChildNodes();

      name = doc.getDocumentElement().getAttribute("name");
      specId = doc.getDocumentElement().getAttribute("id");
      version = doc.getDocumentElement().getAttribute("version");

      if(Boolean.parseBoolean(doc.getDocumentElement().getAttribute("generateSectionIds"))) {
    	  sectionIdGenerator = new SectionIdGenerator();
      }

      for (int i = 0; i < sectionNodes.getLength(); i++)
      {
         if (sectionNodes.item(i) instanceof Element &&
             "section".equals(sectionNodes.item(i).getNodeName()))
         {
            processSectionNode((Element) sectionNodes.item(i));
         }
      }
      return this;
   }

   public String getSectionOriginalId(String sectionId) {
	   return generatedToOriginalIdMap.get(sectionId);
   }

   public boolean hasSectionIdsGenerated() {
	   return sectionIdGenerator != null;
   }

   private void processSectionNode(Element node)
   {
	  String id = node.getAttribute("id");
	  String level = node.getAttribute("level");
	  if(hasSectionIdsGenerated() && level.isEmpty()) {
		  throw new IllegalStateException("Section id generation is enabled and node is missing a level info");
	  }

      String sectionId = (sectionIdGenerator != null ? sectionIdGenerator.nextId(Integer.valueOf(level)) : id);

      titles.put(sectionId, node.getAttribute("title"));
      sectionItems.put(sectionId, new ArrayList<SectionItem>());
      if(sectionIdGenerator != null) {
    	  generatedToOriginalIdMap.put(sectionId, id);
    	  originalToGeneratedIdMap.put(id, sectionId);
      }

      NodeList assertionNodes = node.getChildNodes();

      for (int i = 0; i < assertionNodes.getLength(); i++)
      {
         if (assertionNodes.item(i) instanceof Element &&
             "assertion".equals(assertionNodes.item(i).getNodeName()))
         {
            processAssertionNode(sectionId, (Element) assertionNodes.item(i), null);
         }
         else if (assertionNodes.item(i) instanceof Element &&
               "group".equals(assertionNodes.item(i).getNodeName()))
         {
              processGroupNode(sectionId, (Element) assertionNodes.item(i));
         }
      }
   }

   private void processGroupNode(String sectionId, Element node)
   {
      AssertionGroup group = new AssertionGroup(sectionId);

      NodeList children = node.getChildNodes();

      for (int i = 0; i < children.getLength(); i++)
      {
         Node child = children.item(i);

         if (child instanceof Element && "assertion".equals(child.getNodeName()))
         {
            processAssertionNode(sectionId, (Element) child, group);
         }
         else if (child instanceof Element && "text".equals(child.getNodeName()))
         {
            group.setText(child.getTextContent());
         }
      }

      List<SectionItem> items = sectionItems.get(sectionId);
      items.add(group);
   }

   private void processAssertionNode(String sectionId, Element node, AssertionGroup group)
   {
      String text = null;
      String note = null;

      for (int i = 0; i < node.getChildNodes().getLength(); i++)
      {
         Node child = node.getChildNodes().item(i);

         if (child instanceof Element)
         {
            if ("text".equals(child.getNodeName()))
            {
               text = child.getTextContent();
            }
            else if ("note".equals(child.getNodeName()))
            {
               note = child.getTextContent();
            }
         }
      }

      boolean testable = node.hasAttribute("testable") ?
            Boolean.parseBoolean(node.getAttribute("testable")) : true;

      boolean implied = node.hasAttribute("implied") ?
            Boolean.parseBoolean(node.getAttribute("implied")) : false;

      AuditAssertion assertion = new AuditAssertion(sectionId,
            node.getAttribute("id"), text, note, testable, implied, group);

      if (assertion.getGroup() != null)
      {
         group.addAssertion(assertion);
      }
      else
      {
         List<SectionItem> items = sectionItems.get(sectionId);
         items.add(assertion);
      }
   }
}

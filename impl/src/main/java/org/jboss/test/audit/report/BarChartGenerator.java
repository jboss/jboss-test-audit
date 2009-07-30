package org.jboss.test.audit.report;

import java.util.List;

/**
 * Generates a vertical bar chart in HTML
 *  
 * @author Shane Bryzak
 */
public class BarChartGenerator
{
   private String fromColour;
   private String toColour;
   
   private List<Integer> values;
   
   private int chartHeight = 60; // pixels
   private int barWidth = 16; // pixels
   
   /**
    * 
    * @param fromColour
    * @param toColour
    * @param series A list of label:value values
    */
   public BarChartGenerator(String fromColour, String toColour, List<Integer> values)
   {
      this.fromColour = fromColour;
      this.toColour = toColour;
      this.values = values;      
   }
   
   public String generate()
   {
      int chartWidth = barWidth * values.size();
      StringBuilder sb = new StringBuilder();      
      sb.append("<div style=\"width:" + chartWidth + "px;height:" + chartHeight + "px;border:1px solid #000000\">\n");
      
      // Calculate the highest value for scaling purposes
      int highest = 0;      
      int highestPosition = 0; 
      for (int i = 0; i < values.size(); i++)
      {
         if (values.get(i) > highest)
         {
            highest = values.get(i);
            highestPosition = i;
         }
      }
      
      List<String> gradient = new ColourGradient(fromColour, toColour)
         .calculateGradient(highestPosition + 1);
      
      List<String> downGradient = new ColourGradient(toColour, fromColour)
         .calculateGradient(values.size() - highestPosition + 1); 
      gradient.addAll(downGradient.subList(1, downGradient.size()));
      
      for (int i = 0; i < values.size(); i++)
      {
         int val = values.get(i);
         long pixels = Math.round((chartHeight * 1.0) - (((val * 1.0) / (highest * 1.0)) * (0.9 * chartHeight)));
            
         sb.append("  <div style=\"float:left;width:" + barWidth + "px;height:" + chartHeight + "px;background-color:" + gradient.get(i) + "\">\n");
         sb.append("    <div style=\"background-color:#ffffff;height:" + pixels + "px\">&nbsp;</div>\n");
         sb.append("  </div>\n");
      }
      
      sb.append("</div>\n");
      
      return sb.toString();
   }
}

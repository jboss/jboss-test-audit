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
   
   private List<SeriesGenerator.SeriesElement> values;
   
   private int chartHeight = 60; // pixels
   private int barWidth = 16; // pixels
   
   /**
    * 
    * @param fromColour
    * @param toColour
    * @param series A list of label:value values
    */
   public BarChartGenerator(String fromColour, String toColour, List<SeriesGenerator.SeriesElement> values)
   {
      this.fromColour = fromColour;
      this.toColour = toColour;
      this.values = values;      
   }
   
   public String generate()
   {
      int chartWidth = barWidth * values.size() + values.size(); // add values.size() for border widths
      StringBuilder sb = new StringBuilder();      
      sb.append("<div style=\"width:" + chartWidth + "px;height:" + chartHeight + "px;border:1px solid #000000\">\n");
      
      // Calculate the highest value for scaling purposes
      double highest = 0;      
      int highestPosition = 0; 
      for (int i = 0; i < values.size(); i++)
      {
         if (values.get(i).getValue() > highest)
         {
            highest = values.get(i).getValue();
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
         double val = values.get(i).getValue();
         long pixels = val == 0 ? chartHeight : Math.round((chartHeight * 1.0) - (((val * 1.0) / (highest * 1.0)) * (0.9 * chartHeight)));
         
         String title = String.format("%.1f%% to %.1f%% coverage - %d sections", 
               values.get(i).getRangeFrom(),
               values.get(i).getRangeTo(),
               values.get(i).getValue());
            
         sb.append("  <div title=\"" + title + "\" style=\"float:left;border-right:1px solid #eeeeee;width:" + barWidth + "px;height:" + chartHeight + "px;background-color:" + gradient.get(i) + "\">\n");
         sb.append("    <div style=\"background-color:#ffffff;height:" + pixels + "px\">&nbsp;</div>\n");
         sb.append("  </div>\n");
      }
      
      sb.append("</div>\n");
      
      return sb.toString();
   }
}

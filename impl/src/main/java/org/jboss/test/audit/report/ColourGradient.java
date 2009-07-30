package org.jboss.test.audit.report;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Generates a gradient of gradual colour changes between two html colour codes.
 *  
 * @author Shane Bryzak
 */
public class ColourGradient
{   
   class Colour
   {
      public int red;
      public int green;
      public int blue;
      
      public Colour(int red, int green, int blue)
      {
         this.red = red;
         this.green = green;
         this.blue = blue;
      }
   }
   
   private Colour fromColour;
   private Colour toColour;
   
   private final Pattern htmlColourPattern = Pattern.compile("(?i)^#?([a-f0-9][a-f0-9]?)([a-f0-9][a-f0-9]?)([a-f0-9][a-f0-9]?)$");
   
   public ColourGradient(String gradientFrom, String gradientTo)
   {
      fromColour = parseHtmlColour(gradientFrom);
      toColour = parseHtmlColour(gradientTo);
   }
   
   private Colour parseHtmlColour(String htmlColourCode)
   {
      Matcher m = htmlColourPattern.matcher(htmlColourCode);
      if (!m.matches()) throw new IllegalArgumentException("Invalid colour code: " + htmlColourCode);      
      
      int red = getColourValue(m.group(1));      
      int green = getColourValue(m.group(2));
      int blue = getColourValue(m.group(3));      
      
      return new Colour(red, green, blue);
   }
   
   private int getColourValue(String code)
   {
      return Integer.parseInt(code + (code.length() == 1 ? code : ""), 16);   
   }
      
   public String getHtmlColour(Colour colour)
   {
      return String.format("#%02x%02x%02x", colour.red, colour.green, colour.blue);
   }
   
   public List<String> calculateGradient(int steps)
   {
      List<String> values = new ArrayList<String>();      
      values.add(getHtmlColour(fromColour));
      
      Colour stepColour = new Colour(fromColour.red, fromColour.green, fromColour.blue);
      
      for (int i = 1; i < (steps - 1); i++)
      {
         stepColour.red = nudgeColour(fromColour.red, toColour.red, steps, i);
         stepColour.green = nudgeColour(fromColour.green, toColour.green, steps, i);
         stepColour.blue = nudgeColour(fromColour.blue, toColour.blue, steps, i);
         values.add(getHtmlColour(stepColour));
      }
      
      values.add(getHtmlColour(toColour));
      return values;
   }
   
   private int nudgeColour(int start, int end, int steps, int step)
   {
      return Math.round(start + ((end - start) / ((steps - 1) * 1.0F)) * step);
   }
}

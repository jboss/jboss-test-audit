package org.jboss.test.audit.report;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates a series of values for use in a chart
 *  
 * @author Shane Bryzak
 */
public class SeriesGenerator
{
   private List<Double> values;
   
   public SeriesGenerator()
   {
      values = new ArrayList<Double>();
   }
   
   public void addValue(Double value)
   {
      values.add(value);
   }
   
   public List<Integer> getSeries(int bandSize, int total)
   {      
      List<Integer> series = new ArrayList<Integer>();
    
      for (int i = 0; i < total; i++)
      {
         float rangeFrom = i * bandSize;
         float rangeTo = rangeFrom + bandSize;
         int count = 0;
         
         for (Double value : values)
         {
            if (value >= rangeFrom && (value < rangeTo || (i == total - 1 && value <= rangeTo))) count++;
         }
         
         series.add(count);
      }      
      
      return series;
   }
}

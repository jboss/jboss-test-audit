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
   public class SeriesElement
   {
      private int value;
      private double rangeFrom;
      private double rangeTo;
      
      public SeriesElement(double rangeFrom, double rangeTo, int value)
      {
         this.rangeFrom = rangeFrom;
         this.rangeTo = rangeTo;
         this.value = value;
      }
      
      public int getValue()
      {
         return value;
      }
      
      public double getRangeFrom()
      {
         return rangeFrom;
      }
      
      public double getRangeTo()
      {
         return rangeTo;
      }
   }
   
   private List<Double> values;
   
   public SeriesGenerator()
   {
      values = new ArrayList<Double>();
   }
   
   public void addValue(Double value)
   {
      values.add(value);
   }
   
   public List<SeriesElement> getSeries(int bandSize, int total)
   {      
      List<SeriesElement> series = new ArrayList<SeriesElement>();
    
      for (int i = 0; i < total; i++)
      {
         float rangeFrom = i * bandSize;
         float rangeTo = rangeFrom + bandSize;
         int count = 0;
         
         for (Double value : values)
         {
            if (value >= rangeFrom && (value < rangeTo || (i == total - 1 && value <= rangeTo))) count++;
         }
         
         series.add(new SeriesElement(rangeFrom, rangeTo, count));
      }      
      
      return series;
   }
}

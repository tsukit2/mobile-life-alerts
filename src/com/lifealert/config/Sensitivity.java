package com.lifealert.config;

/**
 * Sensitivity type.
 * 
 * @author Chate Luu, Sukit Tretriluxana
 */
public enum Sensitivity {
   // the enumerations
   VERY_SENSITIVE(0.2F, "Very Sensitive"),
   SENSITIVE(0.4F, "Sensitive"),
   NORMAL(0.6F, "Normal"),
   SOMEWHAT(0.8F, "Somewhat"),
   NOT_SENSITIVE(1.0F, "Not Sensitive");
   
   /**
    * Constructor.
    * 
    * @param val        Value for this sensitivity.
    */
   Sensitivity(float val, String label) {
      this.val = val;
      this.label = label;
   }
   
   /**
    * @return the sensitivity value.
    */
   public float getVal() {
      return val;
   }
   
   /**
    * @return the sensitivity label.
    */
   public String getLabel() {
      return label;
   }
   
   private final float val;
   private final String label;
   
   
   /**
    * Scale used by the sensitivity.
    */
   public static final float SCALE = 0.2F;
}

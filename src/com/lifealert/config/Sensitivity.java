package com.lifealert.config;

/**
 * Sensitivity type.
 * 
 * @author eddy
 */
public enum Sensitivity {
   // the enumerations
   VERY_SENSITIVE(0.2F),
   SENSITIVE(0.4F),
   NORMAL(0.6F),
   SOMEWHAT(0.8F),
   NOT_SENSITIVE(1.0F);
   
   /**
    * Constructor.
    * 
    * @param val        Value for this sensitivity.
    */
   Sensitivity(float val) {
      this.val = val;
   }
   
   /**
    * @return the sensitivity value.
    */
   public float getVal() {
      return val;
   }
   
   private final float val;
   
   
   /**
    * Scale used by the sensitivity.
    */
   public static final float SCALE = 0.2F;
}

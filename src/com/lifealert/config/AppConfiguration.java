package com.lifealert.config;

import java.util.Map;

import android.app.ApplicationContext;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Provide easy access to the application configuration that's strong type. It deals with the complexity of
 * retrieving and storying the preferences so the rest of the application can do things more easily. Note that
 * the class needs to be initialized first with an application context.
 * 
 * @author eddy
 */
public class AppConfiguration {
   private static final String PREF_FILENAME = "LifeAlertAppCfg";
   private static final String PREF_SENSITIVITY = "Sensitivity";
   private static final String PREF_EMERGENCY_CONTACT_ROWID = "EmergencyContactRowId";
   private static final String PREF_CALL_911 = "Call911";
   private static final String PREF_USER_NAME = "UserName";
   private static final String PREF_USER_ADDRESS = "UserAddress";
   private static final String PREF_USER_PHONE = "UserPhone";
   private static final String PREF_VOICE_MAIL_PATH = "VoiceMailPath";
   private static final String PREF_TEXT_MSG = "TextMsg";
   
   private static SharedPreferences pref;
   private static Map<String, ?> localPrefs;
   
   /**
    * Initialize this class so that it can be used later on. Always call this method before any other
    * methods to prevent exceptions.
    * 
    * @param context          Application context to be used to retrieve shared preferences.
    */
   public static void init(ApplicationContext context) {
      // load up the preference for later use
      pref = context.getSharedPreferences(PREF_FILENAME, Context.MODE_PRIVATE);
      localPrefs = pref.getAll();
   }
   
   public static Float getSensitivity() {
      return (Float) getPref(PREF_SENSITIVITY);
   }
   
   public static void setSensitivity(float val) {
      updatePref(PREF_SENSITIVITY, val);
   }
   
   public static Long getEmergencyContactRowId() {
      return (Long) getPref(PREF_EMERGENCY_CONTACT_ROWID);
   }

   public static void setEmergencyContactRowId(long val) {
      updatePref(PREF_EMERGENCY_CONTACT_ROWID, val);
   }
   
   public static Boolean getCall911() {
      return (Boolean) getPref(PREF_CALL_911);
   }

   public static void setCall911(boolean val) {
      updatePref(PREF_CALL_911, val);
   }

   public static String getUserName() {
      return (String) getPref(PREF_USER_NAME);
   }

   public static void setUserName(String val) {
      updatePref(PREF_USER_NAME, val);
   }

   public static String getUserAddress() {
      return (String) getPref(PREF_USER_ADDRESS);
   }

   public static void setUserAddress(String val) {
      updatePref(PREF_USER_ADDRESS, val);
   }
   
   public static String getUserPhone() {
      return (String) getPref(PREF_USER_PHONE);
   }

   public static void setUserPhone(String val) {
      updatePref(PREF_USER_PHONE, val);
   }
   
   public static String getVoiceMailPath() {
      return (String) getPref(PREF_VOICE_MAIL_PATH);
   }

   public static void setVoiceMailPath(String val) {
      updatePref(PREF_VOICE_MAIL_PATH, val);
   }
   
   public static String getTextMsg() {
      return (String) getPref(PREF_TEXT_MSG);
   }

   public static void setTextMsg(String val) {
      updatePref(PREF_TEXT_MSG, val);
   }
   
   /**
    * Helper method to get the preference. This centralize the access so we can check for the initialization.
    * 
    * @param key        The key of the preference.
    * @return preference value or null if not there yet.
    */
   private static Object getPref(String key) {
      checkInitialization();
      return localPrefs.get(key);
   }
   
   /**
    * Helper method to update the preference. Each call results to the permanent update to the shared
    * preference.
    * 
    * @param key           Key of the preference.
    * @param val           Value of the preference. Should not be null.           
    */
   private static void updatePref(String key, Object val) {
      // do some screening
      checkInitialization();
      if (val == null) {
         throw new IllegalArgumentException("value cannot be null");
      }
      
      // update the actual preference and commit it right away
      SharedPreferences.Editor edit = pref.edit();
      if (val instanceof String) {
         edit.putString(key, (String) val);
      } else if (val instanceof Float) {
         edit.putFloat(key, ((Float) val).floatValue());
      } else if (val instanceof Boolean) {
         edit.putBoolean(key, ((Boolean) val).booleanValue());
      }
      edit.commit();
      
      // then refresh the local prefs
      localPrefs = pref.getAll();
   }
   
   /**
    * Helper method to centrally check for the initialization.
    */
   private static void checkInitialization() {
      // throw exception if it's not initialized
      if (pref == null) {
         throw new IllegalThreadStateException("AppConfiguration hasn't been initialized");
      }
   }
}

// $Id: debug.java 4 2008-08-22 17:09:15Z moyekj@yahoo.com $

package com.unwiredappeal.tivo.utils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.grlea.log.DebugLevel;
import org.grlea.log.SimpleLog;
import org.grlea.log.SimpleLogger;

import com.tivo.hme.interfaces.ILogger;
import com.unwiredappeal.tivo.config.StreamBabyConfig;

public class Log {
   public static Log inst = new Log();
   //SimpleLog simpleLog = SimpleLog.defaultInstance();
   SimpleLog simpleLog; // = SimpleLog.defaultInstance();
   
   
   @SuppressWarnings("unchecked")
   public Map<Class, SimpleLogger> logForClassMap = Collections.synchronizedMap(new HashMap<Class, SimpleLogger>());
   
   public Log() {
	   File f = new File(StreamBabyConfig.streamBabyDir, "simplelog.properties");
	   if (!f.exists()) {
		   f = new File(StreamBabyConfig.streamBabyDir, "simplelog.properties.default");		   
	   }
	   if (!f.exists())
		   simpleLog = SimpleLog.defaultInstance(); 
	   else {
		   Properties props = new Properties();
		   try {
			props.load(f.toURI().toURL().openStream());
			String log = props.getProperty("simplelog.logFile");
			if (log != null) {
				props.setProperty("simplelog.logFile", StreamBabyConfig.convertRelativePath(log));
			}
			
		} catch (MalformedURLException e) {
		} catch (IOException e) {

		}
		   simpleLog = new SimpleLog(props);
	   }
   }
   public static void debug(String msg) {
	   inst.db(DebugLevel.L5_DEBUG, msg);
   }
   public static void warn(String msg) {
	   inst.db(DebugLevel.L3_WARN, msg);
   }
   public static void info(String msg) {
	   inst.db(DebugLevel.L4_INFO, msg);
   }

   public static void error(String msg) {
	   inst.db(DebugLevel.L2_ERROR, msg);
	   
   }
   
   public static void fatal(String msg) {
	   inst.db(DebugLevel.L1_FATAL, msg);	   
   }
   
   public static void verbose(String msg) {
	   inst.db(DebugLevel.L6_VERBOSE, msg);	   
   }


    @SuppressWarnings("unchecked")
	private Class getCallingClass() {
        Class cc = null;
        boolean sawMyClass = false;
        StackTraceElement[] trace = new Throwable().getStackTrace();
        for (int i=0;cc == null && i<trace.length;i++) {
            StackTraceElement elt = trace[i];
            if (!elt.getClassName().startsWith(this.getClass().getCanonicalName())) {
            	if (sawMyClass)
					try {
						cc = Class.forName(elt.getClassName());
					} catch (ClassNotFoundException e) {

					}
            }
            else
            	sawMyClass = true;
        }
        return cc == null ? Log.class : cc;
    	
    }
    
    @SuppressWarnings("unchecked")
	public void db(DebugLevel level, String s) {
		Class cc = getCallingClass();
		SimpleLogger logger = logForClassMap.get(cc);
		if (logger == null) {
			logger = new SimpleLogger(simpleLog, cc);
			logForClassMap.put(cc, logger);
		}
		logger.db(level, s);
    }
    
    public class ILoggerImpl implements ILogger {
		@SuppressWarnings("unchecked")
		public void log(int priority, String s) {
			// Translatethe priority
		    //int LOG_INFO    = 0;
		    //int LOG_DEBUG   = 1;
		    //int LOG_NOTICE  = 2;
		    //int LOG_WARNING = 3
			DebugLevel spri = DebugLevel.L1_FATAL;
			switch(priority) {
				case ILogger.LOG_DEBUG:
					spri = DebugLevel.L5_DEBUG;
					break;
				case ILogger.LOG_INFO:
					spri = DebugLevel.L4_INFO;
					break;
				case ILogger.LOG_NOTICE:
					spri = DebugLevel.L3_WARN;
					break;
				case ILogger.LOG_WARNING:
					spri = DebugLevel.L3_WARN;
					break;				
			}
			db(spri, s);
		}
    }

	public ILogger getILogger() {
		return new ILoggerImpl();
	}
}

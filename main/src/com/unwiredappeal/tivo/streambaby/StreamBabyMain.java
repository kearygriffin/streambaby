package com.unwiredappeal.tivo.streambaby;

import java.io.File;
import java.io.IOException;

import bsh.EvalError;
import bsh.Interpreter;

import com.tivo.hme.host.util.ArgumentList;
import com.tivo.hme.host.util.Config;
import com.tivo.hme.sim.StreamBabySimulator;
import com.unwiredappeal.tivo.config.ConfigurationManager;
import com.unwiredappeal.tivo.config.StreamBabyConfig;
import com.unwiredappeal.tivo.streambaby.host.Main;
import com.unwiredappeal.tivo.streambaby.tests.Tests;
import com.unwiredappeal.tivo.utils.Log;

public class StreamBabyMain  {

   public static String DEFAULT_CLASSNAME = StreamBabyStream.class.getCanonicalName();
   public static String FIRSTIME_SCRIPT = "scripts/first-run.bsh";
   public static void main(String argv[]) throws Exception
    {

	  System.err.println("Initializing...");
	  
	  Version version = new Version();
	  ArgumentList al = new ArgumentList(argv);
	  boolean isVer = al.getBoolean("--version");
	  if (isVer) {
		  System.err.println(version.getVersionString());
		  return;
	  }
	  
	  String firstRunScript = StreamBabyConfig.convertRelativePath(FIRSTIME_SCRIPT);
	  if (firstRunScript != null && firstRunScript.length() > 0) {
		  File f = new File(firstRunScript);
		  if (f.exists() && f.canRead()) {
			  int retVal = beanShell(firstRunScript, null);
			  if (retVal < 0) { 
				  Log.error("error running first run script: " + firstRunScript);
				  if (retVal < -1)
					  System.exit(retVal);
			  }
			 
			  else if (retVal == 0) {
				  boolean renamed = f.renameTo(new File(firstRunScript + ".ran"));
				  if (!renamed) {
					  Log.error("Unable to rename first-run script.");
				  }
			  }
		  }
	  }
	  
	  String bc = al.getValue("--bs", null);
	  if (bc != null) {
		  beanShell(bc, al);
		  System.exit(0);
	  }

	  String configFile = al.getValue("--config", null);
	  boolean readConfig = StreamBabyConfig.inst.readConfiguration(configFile);
	  if (configFile != null && !readConfig) {
		  System.err.println("Error loading config: " + configFile);
		  return;
	  }

	  bc = al.getValue("--bsc", null);
	  if (bc != null) {
		  beanShell(bc, al);
		  System.exit(0);
	  }
	  
	  if (al.getBoolean("--test")) {
		  Tests tests = new Tests();
		  tests.performTest(al);
		  return;
	  }
	  
	  if (al.getBoolean("--genpreview")) {
		  PreviewCacheUtils.generate(al);
		  return;
	  }
	  if (al.getBoolean("--cleancache")) {
		  PreviewCacheUtils.cleanup(true);
		  return;
	  }

	  // Register our shutdown Hook
	  Runtime.getRuntime().addShutdownHook(ShutdownHook.getShutdownHook());

	  Config config = new Config();
	  if (StreamBabyConfig.inst._DEBUG) {
	        config.put("listener.debug", "true");
	  }
	  
	  if (StreamBabyConfig.cfgDisableMdns.getBool() == true) {
		  config.put("mdns.disable", "true");
	  }
	  if (al.getBoolean("--help")) {
		  System.out.println("StreamBaby Help:");
		  System.out.println("Configuration help (default config file: " + StreamBabyConfig.CONFIG_FILE + ")");
		  System.out.print(StreamBabyConfig.inst.getConfigHelp());
		  return;
	  }

	  if (StreamBabyConfig.cfgIp.value != null)
		  Main.defaultIp = StreamBabyConfig.cfgIp.value;
	  int port = ConfigurationManager.parseInt(StreamBabyConfig.cfgPort.value);
	  if (port != -1)
		  Main.defaultPort = port;
      String intf = al.getValue("--intf", null);
      String ip = al.getValue("--ip",  null);
      if (ip != null)
    	  Main.defaultIp = ip;
      else if (intf != null)
    	  Main.defaultIp = intf;
      port = al.getInt("--port", -1);
      if (port != -1)
    	  Main.defaultPort = port;

      if (StreamBabyConfig.cfgDeletePreviews.getBool() == true)
    	  PreviewCacheUtils.cleanup(true);
      
      Main.defaultClassname = DEFAULT_CLASSNAME;
	   Log.info("STARTING " + version.getVersionString() + "...");
        // print startup message and go
	   Main.logger = Log.inst.getILogger();
	   if (al.getBoolean("--simulator")) {
		   String[] rem = al.getRemainingArgs();
		   String[] newArgs = new String[rem.length+1];
		   for (int i=0;i<rem.length;i++)
			   newArgs[i] = rem[i];
		   newArgs[newArgs.length-1] = DEFAULT_CLASSNAME;
		   StreamBabySimulator.main(newArgs);
	   }
	   else
		   new Main(al, config, true);
    }
   
   public static class BshLogger {
	   public void  print(String str) {
		   Log.warn(str);
	   }
   }
   public static int beanShell(String origScript, ArgumentList al) {
	   Interpreter bsh = new Interpreter();
	   
	   String script = StreamBabyConfig.convertRelativePath("scripts/" + origScript);
	   if (!new File(script).exists())
		   script = StreamBabyConfig.convertRelativePath(origScript, StreamBabyConfig.workingDir);
	   try {
		   if (al != null)
				bsh.set("argv", al.getRemainingArgs());
		   bsh.set("baseDir", StreamBabyConfig.streamBabyDir);
		   bsh.set("currentDir", StreamBabyConfig.workingDir);
		   bsh.set("isWindows", StreamBabyConfig.isWindows);
		   bsh.set("log", new BshLogger());
		   bsh.set("config", StreamBabyConfig.inst);
		   bsh.setClassLoader(Thread.currentThread().getContextClassLoader());
		 
		   Object o = bsh.source(script);
		   if (o instanceof Integer)
			   return ((Integer)o).intValue();
		   else
			   return -1;
		} catch (EvalError e) {
			e.printStackTrace();
			return -2;
		} catch (IOException e) {
			Log.error("Could not locate script: " + script);
			return -1;
		}
   }
}

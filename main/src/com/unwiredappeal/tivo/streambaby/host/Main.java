// $Id

package com.unwiredappeal.tivo.streambaby.host;

//////////////////////////////////////////////////////////////////////
//
// File: Main.java
//
// Copyright (c) 2003-2005 TiVo Inc.
//
//////////////////////////////////////////////////////////////////////

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.BindException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

import com.tivo.hme.host.util.ArgumentList;
import com.tivo.hme.host.util.Config;
import com.tivo.hme.host.util.Misc;
import com.tivo.hme.interfaces.IArgumentList;
import com.tivo.hme.interfaces.IFactory;
import com.tivo.hme.interfaces.IHmeConstants;
import com.tivo.hme.interfaces.ILogger;
import com.unwiredappeal.tivo.push.InternalPush;
import com.unwiredappeal.tivo.utils.Log;

/**
 * Main SDK class to launch one or more factories.
 *
 * @author      Jonathan Payne
 */
@SuppressWarnings(value = "unchecked")
public class Main implements ILogger
{
	public static final int DEFAULT_PORT = 7288;
    public static int defaultPort = -1;
    public static String  defaultIp = null;
    public static String defaultClassname = null;
    public final static String DNSSD_KEY = "dnssd";
    
    protected Config config;
    protected Listener listener;
    protected List factories = new ArrayList();
    protected JmDNS rv[];
    public static ILogger logger;
    
    public Main(ArgumentList args) throws IOException
    {
        this(args, null, true);
    }
    public Main(ArgumentList args, boolean start) throws IOException
    {
    	this(args, null, start);
    }
    public Main(ArgumentList args, Config cfg, boolean start) throws IOException
    {
        Log.debug("args=" + args + " start=" + start);
        //
        // build config
        //

        this.config = cfg;
        if (config == null)
        	config = new Config();
        config.put("listener.debug", "" + args.getBoolean("-d"));

        int port = args.getInt("--port", defaultPort == -1 ? DEFAULT_PORT : defaultPort);
        config.put("http.ports", "" + port);
        String intf = args.getValue("--intf", null);
        String ip = args.getValue("--ip", defaultIp);

        //
        // determine list of interfaces
        //

        String interfaceList = "";
        if (intf != null) {
           interfaceList += "," + intf;
        } else if (ip != null) {
           interfaceList += "," + ip;
        } else {
           // add at most one regular interface
           boolean regularIntf = false;
 
           InetAddress addrs[] = Misc.getInterfaces();
           for (int i = 0 ; i < addrs.length ; i++) {
              InetAddress addr = addrs[i];
              String str = addr.getHostAddress();
              if (str.equals("127.0.0.1") || str.startsWith("169.254.")) {
                 continue;
              } else if (!regularIntf) {
                 regularIntf = true;
                 interfaceList += "," + str;
              }
           }
        }
        if ("true".equals(System.getProperty("hme.loopback"))) {
            interfaceList += "," + "127.0.0.1";
         }
        config.put("http.interfaces", interfaceList);

        //
        // determine list of factories
        //

        try {
            // load the factories
            if (start) {
                createFactory(args, ClassLoader.getSystemClassLoader());
            }

            // bail if we didn't get any
            if (start && factories.size() == 0) {
                System.out.println("Failed to instantiate any HME apps");
                return;
            }

            //
            // start the listener
            //
            
            if (logger == null)
            	logger = this;
            try {
                listener = new Listener(config, logger);
                Listener.DEBUG_FLUSHES = true;
            } catch (BindException e) {
                if (port == DEFAULT_PORT && defaultPort == -1) {
                    // hm - default port failed, try another
                    config.put("http.ports", "1234");
                    listener = new Listener(config, logger);                
                } else {
                    throw e;
                }
            }
        
            //
            // get ready for JmDNS
            //
            
            String interfaces[] = listener.getInterfaces();
            
            boolean disableMdns= config.getBool("mdns.disable", false);

            if (!disableMdns) {
	            rv = new JmDNS[interfaces.length];
	            for (int i = 0; i < interfaces.length; ++i) {
	                //rv[i] = new JmDNS(InetAddress.getByName(interfaces[i]));
	            	rv[i] = JmDNS.create(InetAddress.getByName(interfaces[i]));
	            	new TiVoListener(rv[i]);
	            	// and hack away to remove the shutdown hook
	            	removeBadJmdnsShutdownHook(rv[i]);
	            }
            }
            
            //
            // now start the factories
            //
            
            listener.setFactories(factories);
            for (Iterator i = factories.iterator(); i.hasNext(); ) {
                IFactory factory = (IFactory)i.next();
                register(factory);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("error: " + e.getMessage());
            usage();
        }
    }

	private void removeBadJmdnsShutdownHook(JmDNS mdns) {
		try {
			Field[] fields = mdns.getClass().getDeclaredFields();
			for (Field f : fields) {
				if (f.getName().equals("shutdown")) {
					f.setAccessible(true);
					Thread s = (Thread)f.get(mdns);
					Runtime.getRuntime().removeShutdownHook(s);
					break;
				}
			}
		}
		catch(Exception e) { }
	}

    private void usage()
    {
        System.out.println("usage: Main [--port <port>] [--ip <ip>] class");
        System.out.println();
        System.exit(1);
    }
    
    /**
     * Create a factory with the specified arguments and class loader.
     */
    
    private void createFactory(ArgumentList args, ClassLoader loader)
    {
        Log.debug("args=" + args + " loader=" + loader);
        try {
            String classname = args.getValue("--class", null);
            if (classname == null && args.getRemainingCount() == 0)
            	classname = defaultClassname;
            if (classname == null) {
                classname = args.shift();
            }
            Class appClass = Class.forName(classname, true, loader);
            Class[] paramTypes = {String.class, ClassLoader.class, IArgumentList.class};
            Method getFactoryMethod = appClass.getMethod("getAppFactory", paramTypes);
                        
            Object[] params = {classname, loader, args };
    
            IFactory factory = (IFactory)getFactoryMethod.invoke(null, params);
            
            args.checkForIllegalFlags();
            factories.add(factory);
        } catch (ClassNotFoundException e) {
            System.out.println("error: class not found: " + e.getMessage());
            System.out.println("error: check the classpath and access permissions");
        } catch (IllegalAccessException e) {
            System.out.println("error: illegal access: " + e.getMessage());
            System.out.println("error: make sure the class is public and has a public default constructor");
        } catch (NoSuchMethodException e) {
            System.out.println("error: no constructor: " + e.getMessage());
            System.out.println("error: make sure the class is public and has a public default constructor");
        } catch (IllegalArgumentException e) {
            System.out.println("error: illegal argument: " + e.getMessage());
            System.out.println("error: make sure the class is public and has a public static getAppFactory method with correct parameters");
        } catch (InvocationTargetException e) {
            System.out.println("error: unable to invoke method: " + e.getMessage());
            System.out.println("error: make sure the class is public and has a public static getAppFactory method");
        }
    }

    protected static class UnregisterThread extends Thread {
    	JmDNS mdns;
    	ServiceInfo info;
    	public UnregisterThread(JmDNS mdns, ServiceInfo info) {
    		this.mdns = mdns;
    		this.info = info;
    	}
    	@Override
    	public void run() {
    		mdns.unregisterService(info);
    	}
    }
    /**
     * Register a factory if MDNS is turned on.
     */
    protected void register(IFactory factory) throws IOException
    {
        Log.debug("factory=" + factory);
        String interfaces[] = listener.getInterfaces();
        int ports[] = listener.getPorts();
        for (int i = 0; i < interfaces.length; ++i) {
            for (int j = 0; j < ports.length; ++j) {
                String url = ("http://" + interfaces[i] + ":" +
                              ports[j] + factory.getAppName());

                if (rv == null) {
                    System.out.println(url + " [no mdns]");
                    continue;
                }

                logger.log(ILogger.LOG_INFO, "MDNS: " + url);
                logger.log(ILogger.LOG_NOTICE,"streambaby ready & listening.");

                //
                // register using jmdns
                //
                ServiceInfo info = getServiceInfo(IHmeConstants.MDNS_TYPE, factory, ports[j]);
                rv[j].registerService(info);
                // Don't do this, or we'll have same problem jmdns shutdown has
                //UnregisterThread t = new UnregisterThread(rv[j], info);

                //Runtime.getRuntime().addShutdownHook(t);
            }
        }
    }

    protected ServiceInfo getServiceInfo(String mdns_type, IFactory factory, int port)
    {
        Log.debug("mdns_type=" + mdns_type + " factory=" + factory + " port=" + port);
        Hashtable atts = new Hashtable();
        atts.put("path", factory.getAppName());
        atts.put("version", (String)factory.getFactoryData().get(IFactory.HME_VERSION_TAG));
        //return new ServiceInfo(mdns_type, factory.getAppTitle() + "." + mdns_type, port, 0, 0, atts);
        return ServiceInfo.create(mdns_type, factory.getAppTitle() + "." + mdns_type, port, 0, 0, atts);
    }

    //
    // from IMain
    //

    public void log(int priority, String s)
    {
    	String l = "LOG: " + s;
    	System.err.println(l);
    }

    public static void main(String argv[]) throws IOException
    {
        // print startup message and go
        System.err.println("STARTING...");
        new Main(new ArgumentList(argv));
    }
	public Listener getListener() {
		return listener;
	}
	public List getFactories() {
		return factories;
	}

}

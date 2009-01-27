// $Id

package com.unwiredappeal.tivo.streambaby.host;

//////////////////////////////////////////////////////////////////////
//
// File: HostContext.java
//
// Copyright (c) 2005 TiVo Inc.
//
//////////////////////////////////////////////////////////////////////

/*
 * Created on May 20, 2005
 *
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import com.tivo.hme.interfaces.IContext;
import com.tivo.hme.interfaces.IFactory;
import com.tivo.hme.interfaces.ILogger;
import com.tivo.hme.host.http.server.HttpRequest;
import com.tivo.hme.host.util.Misc;
import com.unwiredappeal.tivo.utils.Log;

/**
 * 
 * @author kgidley
 */
@SuppressWarnings(value = "unchecked")
public class HostContext implements IContext {

    /**
     * The uri used to connect to this application.
     */
    URL uri;

    /**
     * The base uri which can be used to direct HTTP requests at this
     * application.
     */
    URL base;

    String cookie;
    Map params;
    URL assetURI;
    
//    Properties state;
    Map persistentDataProps;
    
    HttpRequest request;
    Socket s;
    InputStream in;
    OutputStream out;

    ILogger logger = null;
    
    IFactory factory = null;
    String appClassName = null;
    HashMap connAttrMap;
    
   /* (non-Javadoc)
    * @see com.tivo.hme.hosting.IContext#getOutputStream()
    */
   public OutputStream getOutputStream() {
      return out;
   }

    /* (non-Javadoc)
     * @see com.tivo.hme.hosting.IContext#getInputStream()
     */
    public InputStream getInputStream() {
        return in;
    }
    

   /* (non-Javadoc)
    * @see com.tivo.hme.hosting.IContext#getLogger()
    */
   public ILogger getLogger() {
      return logger;
   }

   /* (non-Javadoc)
    * @see com.tivo.hme.hosting.IContext#getReceiverGUID()
    */
   public String getReceiverGUID() {
            return getConnectionAttribute("tsn");
   }

   /* (non-Javadoc)
    * @see com.tivo.hme.hosting.IContext#getConnectionAttribute(java.lang.String)
    */
   public String getConnectionAttribute(String key) 
    {
        String retVal = null;
        
        // look for a param value first
        retVal = (String)params.get(key);
        if (retVal == null || retVal.length() < 1)
        {
            // now look for a header value if didn't find a param
            retVal = request.get(key);
        }
        
      return retVal;
   }

    /* (non-Javadoc)
     * @see com.tivo.hme.interfaces.IContext#getConnectionAttributes()
     */
    public Map getConnectionAttributes() {
        if (connAttrMap == null) {
            connAttrMap = new HashMap();
            connAttrMap.putAll(params);
            for (Iterator iter = request.getHeaders().getKeys(); iter.hasNext();) {
                String key = (String) iter.next();
                connAttrMap.put(key, request.getHeaders().get(key));
            }
        }
        return connAttrMap;
    }

      
    /* (non-Javadoc)
     * @see com.tivo.hme.hosting.IContext#getSocket()
     */
    public Socket getSocket() {
        return s;
    }

    /**
     * @return the base uri which can be used to direct HTTP requests at this
     * application.
     */
    public URL getBaseURI()
    {
        return base;
    }

    /**
     * The URI for loading external assets.
     */
    public URL getAssetURI()
    {
        if (assetURI == null) {
            synchronized (this) {
                if (assetURI == null) {
                    assetURI = factory.getAssetURI();
                    if (assetURI == null) {
                        try {
                            assetURI = new URL(base, "assets/");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return assetURI;
    }
    
    /* (non-Javadoc)
     * @see com.tivo.hme.hosting.IContext#getPersistentData(java.lang.String)
     */
    public String getPersistentData(String key) {
        return getPersistentData(key, appClassName, false);
    }

    /* (non-Javadoc)
     * @see com.tivo.hme.hosting.IContext#getPersistentData(java.lang.String, java.lang.String, boolean)
     */
    public String getPersistentData(String key, String applicationId,
            boolean applicationGlobal) 
    {
        Log.debug("key=" + key + " applicationId=" + applicationId + " applicationGlobal=" + applicationGlobal);
        String data = null;
        String compKey = constructCompoundKey(applicationId, applicationGlobal);
        if (compKey != null)
        {
            data = getSessionState(compKey).getProperty(key);
        }
        else
        {
            // invalid applicationId must have been used, 
            // log error message
        }
        return data;
    }

    /* (non-Javadoc)
     * @see com.tivo.hme.hosting.IContext#setPersistentData(java.lang.String, java.lang.String)
     */
    public void setPersistentData(String key, String value) {
        Log.debug("key=" + key + " value=" + value);
        setPersistentData(key, value, appClassName, false);
    }

    /* (non-Javadoc)
     * @see com.tivo.hme.hosting.IContext#setPersistentData(java.lang.String, java.lang.String, java.lang.String, boolean)
     */
    public void setPersistentData(String key, String value,
            String applicationId, boolean applicationGlobal) 
    {
        Log.debug("key=" + key + " value=" + value + "applicationId=" + applicationId + " applicationGlobal=" + applicationGlobal);
        String compKey = constructCompoundKey(applicationId, applicationGlobal);
        if (compKey != null)
        {
            getSessionState(compKey).setProperty(key, value);
            saveSessionState(compKey);
        }
    }

    /**
     * @return
     */
    private String constructCompoundKey(String applicationIdStr, boolean applicationGlobal) 
    {
        Log.debug("applicationIdStr=" + applicationIdStr + " applicationGlobal=" + applicationGlobal);
        String rcvrOrGlobalStr = null;

        if (applicationGlobal) {
            rcvrOrGlobalStr = "app_global_data";
        } else {
            rcvrOrGlobalStr = getReceiverGUID();
        }

        if (applicationIdStr == null || applicationIdStr.length() < 1) {
            applicationIdStr = appClassName;
        }

        if (validApplicationId(applicationIdStr)) {
            return rcvrOrGlobalStr + "-and-" + applicationIdStr;
        } else {
            return null;
        }
    }

    /**
     * @param applicationIdStr
     * @return
     */
    private boolean validApplicationId(String applicationIdStr) 
    {
        Log.debug("applicationIdStr=" + applicationIdStr);
        // TODO: better logic to check applicationIdStr against
        // classname - should compare each whole dotted part of 
        // the name.  I.e. check 'com.foo.' and 'com.foo.bar.' 
        // rather than the current (quick) implementation that 
        // allows something that might be a partial match.
        // Right now, 'com.foo' would match both 'com.fool.motley' 
        // and 'com.food.beer' and that might allow one app to read
        // anothers data - a bad thing.
        if ((applicationIdStr.length() > 4) 
                && appClassName.startsWith(applicationIdStr))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    
    /**
     * Get all session properties.
     */
    synchronized Properties getSessionState(String compKey)
    {
        Log.debug("compKey=" + compKey);
        if (persistentDataProps == null) {
            persistentDataProps = new HashMap();
        }
        Properties state = (Properties)persistentDataProps.get(compKey);
        if ( state == null) {
            state = new Properties();
            persistentDataProps.put(compKey, state);
            File file = getSessionFile(compKey);
//            System.out.println("*** file for persistent state: " + file.getName());
            if (file.exists()) {
                try {
                    FileInputStream in = new FileInputStream(file);
                    try {
                        state.load(in);
                    } finally {
                        in.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return state;
    }

    synchronized void saveSessionState(String compKey)
    {
        Log.debug("compKey=" + compKey);
        if (persistentDataProps == null) {
            persistentDataProps = new HashMap();
        }
        Properties state = (Properties)persistentDataProps.get(compKey);
        if (state != null) {
            File file = getSessionFile(compKey);
//            System.out.println("*** file for persistent state: " + file.getName());
            File dir = file.getParentFile();
            if (!dir.exists()) {
                dir.mkdirs();
            }
            try {
                FileOutputStream out = new FileOutputStream(file);
                try {
                    state.store(out, base.toString());
                } finally {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    File getSessionFile(String compKey)
    {
        Log.debug("compKey=" + compKey);
        String tivo = Misc.isWindows() ? "Application Data/TiVo" : ".tivo";
        return new File(System.getProperty("user.home") + "/" + tivo + "/"+ compKey + ".txt");
    }

    /* (non-Javadoc)
     * @see com.tivo.hme.interfaces.IContext#close()
     */
    public void close() throws IOException {
        getSocket().close();
    }

}

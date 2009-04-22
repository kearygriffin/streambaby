package com.unwiredappeal.tivo.streambaby.host;

/*
 * Copyright (C) 2005 Leon Nicholls
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free
 * Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 * See the file "COPYING" for more details.
 */

import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.net.*;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;
import javax.jmdns.ServiceTypeListener;

import com.unwiredappeal.tivo.push.InternalPush;
import com.unwiredappeal.tivo.utils.Log;


public class TiVoListener implements ServiceListener, ServiceTypeListener {

    // TiVo with 7.1 software supports rendevouz and has a web server

    private final static String TIVO_PLATFORM = "platform";

    private final static String TIVO_PLATFORM_PREFIX = "tcd"; // platform=tcd/Series2

    private final static String TIVO_TSN = "TSN";

    private final static String TIVO_SW_VERSION = "swversion";

    private final static String TIVO_PATH = "path";
    
    public static final String MDNS_HTTP_SERVICE = "_http._tcp.local.";


    public TiVoListener(JmDNS mJmDNS) {
            mJmDNS.addServiceListener(MDNS_HTTP_SERVICE, this);
    }
    public void close() {
    }

	public void serviceAdded(ServiceEvent arg0) {
		String type = arg0.getType();
		String name = arg0.getName();
		//ServiceInfo info = arg0.getInfo();
		JmDNS jmdns = arg0.getDNS();
		
		if (name.endsWith("." + type)) {
            name = name.substring(0, name.length() - (type.length() + 1));
        }
        Log.debug("addService: " + name);

        //ServiceInfo service = jmdns.getServiceInfo(type, name);
        //if (service != null) {
            //if (!name.endsWith(".")) {
              //  name = name + "." + type;
            //}
            Log.debug("Updating service: " + type + " (" + name + ")");
            jmdns.requestServiceInfo(type, name, 0);
        //}
    }
	public void serviceRemoved(ServiceEvent arg0) {
		String type = arg0.getType();
		String name = arg0.getName();
		//ServiceInfo info = arg0.getInfo();
		//JmDNS jmdns = arg0.getDNS();

		if (name.endsWith("." + type)) {
            name = name.substring(0, name.length() - (type.length() + 1));
        }
        Log.debug("removeService: " + name);
        // TODO: Should remove TiVo from list, if we knew about it...
    }
	public void serviceTypeAdded(ServiceEvent arg0) {
		String type = arg0.getType();
		//String name = arg0.getName();
		//ServiceInfo info = arg0.getInfo();
		//JmDNS jmdns = arg0.getDNS();
		Log.debug("addServiceType: " + type);
    }
	public void serviceResolved(ServiceEvent arg0) {
		String type = arg0.getType();
		String name = arg0.getName();
		ServiceInfo info = arg0.getInfo();
		JmDNS jmdns = arg0.getDNS();
		
        Log.debug("resolveService: " + type + " (" + name + ")");

        /*
         * DVR AAB0._http._tcp.local. // name DVR-AAB0.local.:80 // server:port 192.168.0.5:80 // address:port
         * platform=tcd/Series2 TSN=24020348251AAB0 swversion=7.1.R1-01-2-240 path=/index.html
         */

        if (type.equals(MDNS_HTTP_SERVICE)) {
            if (info == null) {
                Log.error("Service not found: " + type + "(" + name + ")");
            } else {
                boolean found = false;
                //TiVo tivo = new TiVo();
                //tivo.setName(name);
                //tivo.setServer(info.getServer());
                //tivo.setPort(info.getPort());
                //tivo.setAddress(info.getHostAddress());

                String tsn = null;
                String platform = null;
                String sw = null;
                String path = null;
                found = info.getPropertyString(TIVO_PLATFORM)!=null && info.getPropertyString(TIVO_TSN)!=null;
                for (Enumeration names = info.getPropertyNames(); names.hasMoreElements();) {
                    String prop = (String) names.nextElement();
                    if (prop.equals(TIVO_PLATFORM)) {
                        //tivo.setPlatform(info.getPropertyString(prop));
                    	platform = info.getPropertyString(prop);
                        /*
                        if (tivo.getPlatform().startsWith(TIVO_PLATFORM_PREFIX))
                            found = true;
                        */
                    } else if (prop.equals(TIVO_TSN)) {
                        tsn = info.getPropertyString(prop);
                    } else if (prop.equals(TIVO_SW_VERSION)) {
                        sw = info.getPropertyString(prop);
                    } else if (prop.equals(TIVO_PATH)) {
                        path = info.getPropertyString(prop);
                    }
                }

                if (found && tsn != null) {
                	Log.info("jmDns found TiVo: " + name + ", tsn: " + tsn);
                	InternalPush.getInstance().addTivoTsn(tsn, name);
                }
            }
        }
    }
    //private JmDNS mJmDNS;
}

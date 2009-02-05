package com.tivo.hme.sim;

import javax.swing.JFrame;

import com.tivo.hme.host.util.ArgumentList;
import com.tivo.hme.sdk.Factory;
import com.tivo.hme.sdk.util.HmeVersion;
import com.unwiredappeal.tivo.streambaby.host.Main;

public class StreamBabySimulator extends Simulator {
    public static void main(String argv[]) throws Exception
    {
        ArgumentList args = new ArgumentList(argv);
        Simulator.DEBUG = args.getBoolean("-d");

        
        if (args.getBoolean("-h") || args.getBoolean("--help")) {
            usage();
            return;
        } else if (Simulator.DEBUG) {
            new HmeVersion().printVersion(System.out);
        }

        if (args.getBoolean("-mute")) {
            SOUND = false;
        }

        if (args.getBoolean("-record")) {
            RECORD = true;
        }

        if (args.getBoolean("-s")) {
            USEMDNS = false;
        }
        
        String url = null;
        String classname = args.getValue("-class", null);
        if (args.getRemainingCount() > 0) {
            String arg = args.shift();
            if (arg.startsWith("http://")) {
                // it's a URL
                url = arg;
                if (args.getRemainingCount() > 0) {
                    usage();
                    return;
                }
            } else {
                // assume remaining arg is the class to run
                classname = arg;
            }
        }

        // we have an applicaion class, start a hosting environment
        // using that class
        if (classname != null) {
        	if (args.getRemainingCount() > 0) {
        		classname = classname + " " + args.toString();
        	}
            ArgumentList mainArgs = new ArgumentList(classname);
            Main main = new Main(mainArgs);
            if (main.getFactories() != null && main.getListener() != null) {
                Factory appFactory = (Factory)(main.getFactories().get(0));
                String[] intfs = main.getListener().getInterfaces();
                url = "http://" + intfs[0] + appFactory.getAppName();
            } else {
            	System.out.println("Unable to start specified application - running Simulator stand alone.");
            }
            //System.out.println("*** url = ->" + url +"<-");
        }

        JFrame.setDefaultLookAndFeelDecorated(true);
        new Simulator().run(url);
    }
}

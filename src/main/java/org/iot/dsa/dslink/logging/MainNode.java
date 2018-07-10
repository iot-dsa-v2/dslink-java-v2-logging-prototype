package org.iot.dsa.dslink.logging;

import org.iot.dsa.DSRuntime;
import org.iot.dsa.dslink.DSMainNode;

public class MainNode extends DSMainNode implements Runnable {
    
    public MainNode() {
    }

    
    /**
     * Defines the permanent children of this node type, their existence is guaranteed in all
     * instances.  This is only ever called once per, type per process.
     */
    @Override
    protected void declareDefaults() {
        super.declareDefaults();
        declareDefault("Logging", new LogService());
    }
    
    @Override
    protected void onStable() {
        DSRuntime.run(this);
    }


    @Override
    public void run() {
        while(true) {
            info("Hello Info!");
            warn("Warning! Hello!");
            fine("Looking Fine!");
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}

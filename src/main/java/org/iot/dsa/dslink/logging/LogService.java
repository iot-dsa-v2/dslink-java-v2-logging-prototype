package org.iot.dsa.dslink.logging;

import java.util.Enumeration;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import org.iot.dsa.node.DSIObject;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSNode;
import org.iot.dsa.node.action.ActionInvocation;
import org.iot.dsa.node.action.ActionResult;
import org.iot.dsa.node.action.DSAbstractAction;

public class LogService extends DSNode {
    
    public LogService() {
    }
    
    private LogManager logManager = LogManager.getLogManager();
    
    @Override
    protected void declareDefaults() {
        super.declareDefaults();
        declareDefault("Refresh", getRefreshAction());
    }
    
    @Override
    protected void onStable() {
        super.onStable();
        init();
    }
    
    private void init() {
        Enumeration<String> logNames = logManager.getLoggerNames();
        while (logNames.hasMoreElements()) {
            String name = logNames.nextElement();
            Logger logger = logManager.getLogger(name);
            if (name.isEmpty()) {
                name = "root";
            }
            DSIObject obj = get(name);
            if (logger != null) {
                if (!(obj instanceof LoggerNode)) {
                    obj = new LoggerNode();
                    put(name, obj);
                }
                ((LoggerNode) obj).setLoggerObj(logger);
            } else {
                remove(name);
            }
        }
        for (DSInfo info: this) {
            DSIObject obj = info.getObject();
            if (obj instanceof LoggerNode && ((LoggerNode) obj).getLoggerObj() == null) {
                remove(info);
            }
        }
    }
    
    private DSAbstractAction getRefreshAction() {
        DSAbstractAction act = new DSAbstractAction() {
            
            @Override
            public void prepareParameter(DSInfo info, DSMap parameter) {
            }
            
            @Override
            public ActionResult invoke(DSInfo info, ActionInvocation invocation) {
                ((LogService) info.getParent()).init();
                return null;
            }
        };
        return act;
    }

}

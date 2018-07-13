package org.iot.dsa.dslink.logging;

import java.util.logging.LogManager;
import java.util.logging.Logger;
import org.iot.dsa.node.DSIObject;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSString;
import org.iot.dsa.node.action.ActionInvocation;
import org.iot.dsa.node.action.ActionResult;
import org.iot.dsa.node.action.DSAbstractAction;

public class LoggerNode extends StreamableLogNode {
    
    private DSInfo levelInfo = getInfo("Log Level");    
    
    public LoggerNode() {
    }

    public Logger getLoggerObj() {
        return LogManager.getLogManager().getLogger(getName());
    }
    
    public DSInfo getLevelInfo() {
        return levelInfo;
    }
    
    @Override
    protected void declareDefaults() {
        super.declareDefaults();
//        declareDefault("Edit", getEditAction());
        declareDefault("Refresh", getRefreshAction());
        declareDefault("Get Streaming Log", getStreamLogAction());
        declareDefault("Remove", getRemoveAction());
        declareDefault("Log Level", DSString.valueOf("Unknown"));
    }

    @Override
    protected void onStable() {
        super.onStable();
    }
    
    private DSIObject getRemoveAction() {
        DSAbstractAction act = new DSAbstractAction() {
            
            @Override
            public void prepareParameter(DSInfo info, DSMap parameter) {
            }
            
            @Override
            public ActionResult invoke(DSInfo info, ActionInvocation invocation) {
                ((LoggerNode) info.getParent()).remove();
                return null;
            }
        };
        return act;
    }
    
    private void remove() {
        getParent().remove(getInfo());
    }

    private DSAbstractAction getRefreshAction() {
        DSAbstractAction act = new DSAbstractAction() {        
            @Override
            public void prepareParameter(DSInfo info, DSMap parameter) {
            }
            
            @Override
            public ActionResult invoke(DSInfo info, ActionInvocation invocation) {
                ((LoggerNode) info.getParent()).updateLevel(-1);
                return null;
            }
        };
        return act;
    }
}

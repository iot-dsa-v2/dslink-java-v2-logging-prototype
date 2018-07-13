package org.iot.dsa.dslink.logging;

import java.util.Enumeration;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import org.iot.dsa.node.DSFlexEnum;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSList;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSMetadata;
import org.iot.dsa.node.DSString;
import org.iot.dsa.node.action.ActionInvocation;
import org.iot.dsa.node.action.ActionResult;
import org.iot.dsa.node.action.DSAbstractAction;

public class LogService extends StreamableLogNode {
    
    private DSInfo levelInfo = getInfo("Default Log Level");   
    
    public LogService() {
    }
    
    @Override
    public Logger getLoggerObj() {
        return LogManager.getLogManager().getLogger("");
    }
    
    public DSInfo getLevelInfo() {
        return levelInfo;
    }
        
    @Override
    protected void declareDefaults() {
        super.declareDefaults();
        declareDefault("Add Log", getAddLogAction());
        declareDefault("Stream All", getStreamLogAction());
        declareDefault("Default Log Level", DSString.valueOf("Unknown")).setTransient(true);
    }

    @Override
    protected void onStable() {
        super.onStable();
    }
    
    private DSList getLogNames() {
        DSList l = new DSList();
        Enumeration<String> logNames = LogManager.getLogManager().getLoggerNames();
        while (logNames.hasMoreElements()) {
            String name = logNames.nextElement();
            if (!name.isEmpty()) {
                l.add(name);
            }
        }
        return l;
    }
    
    
    private DSAbstractAction getAddLogAction() {
        DSAbstractAction act = new DSAbstractAction() {
            
            @Override
            public void prepareParameter(DSInfo info, DSMap parameter) {
                DSMetadata meta = new DSMetadata(parameter);
                if ("Log".equals(meta.getName())) {
                    DSList range = getLogNames();
                    if (range.size() > 0) {
                        meta.setType(DSFlexEnum.valueOf(range.getString(0), range));
                    }
                }
            }
            
            @Override
            public ActionResult invoke(DSInfo info, ActionInvocation invocation) {
                ((LogService) info.getParent()).addLog(invocation.getParameters());
                return null;
            }
        };
        DSList range = getLogNames();
        act.addParameter("Log", DSFlexEnum.valueOf(range.getString(0), range), "");
        return act;
    }
    
    private void addLog(DSMap parameters) {
        String logName = parameters.getString("Log");
        put(logName, new LoggerNode());
    }
    
}

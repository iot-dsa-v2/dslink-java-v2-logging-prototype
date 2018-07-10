package org.iot.dsa.dslink.logging;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import org.iot.dsa.node.DSFlexEnum;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSList;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSMetadata;
import org.iot.dsa.node.DSNode;
import org.iot.dsa.node.DSValueType;
import org.iot.dsa.node.action.ActionInvocation;
import org.iot.dsa.node.action.ActionResult;
import org.iot.dsa.node.action.ActionSpec;
import org.iot.dsa.node.action.ActionTable;
import org.iot.dsa.node.action.ActionSpec.ResultType;
import org.iot.dsa.node.action.DSAbstractAction;

public class LoggerNode extends DSNode {
    
    private static DSList levelRange = new DSList().add(Level.ALL.toString())
            .add(Level.CONFIG.toString()).add(Level.FINE.toString()).add(Level.FINER.toString())
            .add(Level.FINEST.toString()).add(Level.INFO.toString()).add(Level.OFF.toString())
            .add(Level.SEVERE.toString()).add(Level.WARNING.toString());
    
    private Logger loggerObj;
    
    public LoggerNode() {
    }

    public Logger getLoggerObj() {
        return loggerObj;
    }

    public void setLoggerObj(Logger loggerObj) {
        this.loggerObj = loggerObj;
    }
    
    @Override
    protected void declareDefaults() {
        super.declareDefaults();
        declareDefault("edit", getEditAction());
        declareDefault("get streaming log", getGetLogAction());
    }

    private DSAbstractAction getGetLogAction() {
        DSAbstractAction act = new DSAbstractAction() {
            
            @Override
            public void prepareParameter(DSInfo info, DSMap parameter) {
            }
            
            @Override
            public ActionResult invoke(DSInfo info, ActionInvocation invocation) {
                return ((LoggerNode) info.getParent()).startLogStream(info, invocation);
            }
        };
        act.setResultType(ResultType.STREAM_TABLE);
        act.addValueResult("Log", DSValueType.STRING).setEditor("textarea");
        return act;
    }
    
    private ActionTable startLogStream(final DSInfo actionInfo, final ActionInvocation invocation) {
        final Handler handler = new Handler() {
//            private List<String> buffer = new LinkedList<String>();
            
            @Override
            public void publish(LogRecord record) {
                String line = getFormatter().format(record);
//                buffer.add(line);
                invocation.send(new DSList().add(line));
            }
            
            @Override
            public void flush() {
//                List<String> oldBuffer = buffer;
//                buffer = new LinkedList<String>();
//                for (String line: oldBuffer) {
//                    invocation.send(new DSList().add(line));
//                }
            }
            
            @Override
            public void close() throws SecurityException {
                invocation.close();
            }
        };
        handler.setFormatter(new SimpleFormatter());
        loggerObj.addHandler(handler);
        DSMap col = new DSMetadata().setName("Record").setType(DSValueType.STRING).getMap();
        final List<DSMap> columns = Collections.singletonList(col);
        
        return new ActionTable() {
            
            @Override
            public void onClose() {
                handler.close();
                loggerObj.removeHandler(handler);
            }
            
            @Override
            public ActionSpec getAction() {
                return actionInfo.getAction();
            }
            
            @Override
            public Iterator<DSList> getRows() {
                List<DSList> empty = Collections.emptyList();
                return empty.iterator();
            }
            
            @Override
            public Iterator<DSMap> getColumns() {
                return columns.iterator();
            }
        };
    }

    private DSAbstractAction getEditAction() {
        DSAbstractAction act = new DSAbstractAction() {
            
            @Override
            public void prepareParameter(DSInfo info, DSMap parameter) {
                ((LoggerNode) info.getParent()).prepareEditParameter(parameter);    
            }
            
            @Override
            public ActionResult invoke(DSInfo info, ActionInvocation invocation) {
                ((LoggerNode) info.getParent()).edit(invocation.getParameters());
                return null;
            }
        };
        act.addParameter("Level", DSFlexEnum.valueOf("INFO", levelRange), "");
//        act.addParameter("Filter", DSValueType.STRING, "");
        return act;
    }
    
    private void prepareEditParameter(DSMap parameter) {
        if (loggerObj == null) {
            return;
        }
        if ("Level".equals(parameter.getString("name"))) {
            parameter.put("default", getLoggerLevel().toString());
        } else if ("Filter".equals(parameter.getString("name"))) {
//            parameter.put("default", loggerObj.getFilter().toString());
        }
    }
    
    private void edit(DSMap parameters) {
        String level = parameters.getString("Level");
        if (loggerObj != null) {
            loggerObj.setLevel(Level.parse(level));
        }
    }
    
    private Level getLoggerLevel() {
        Logger obj = loggerObj;
        Level l = obj.getLevel();
        while (l == null) {
            obj = obj.getParent();
            l = obj.getLevel();
        }
        return l;
    }


}

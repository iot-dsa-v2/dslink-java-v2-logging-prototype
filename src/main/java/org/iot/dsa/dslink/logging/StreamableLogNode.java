package org.iot.dsa.dslink.logging;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.iot.dsa.DSRuntime;
import org.iot.dsa.logging.DSLogHandler;
import org.iot.dsa.node.DSFlexEnum;
import org.iot.dsa.node.DSIValue;
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
import org.iot.dsa.node.action.DSAbstractAction;
import org.iot.dsa.node.action.ActionSpec.ResultType;

public abstract class StreamableLogNode extends DSNode {
    
    private static DSList levelRange = new DSList().add(Level.ALL.toString())
            .add(Level.CONFIG.toString()).add(Level.FINE.toString()).add(Level.FINER.toString())
            .add(Level.FINEST.toString()).add(Level.INFO.toString()).add(Level.OFF.toString())
            .add(Level.SEVERE.toString()).add(Level.WARNING.toString());
    
    public abstract Logger getLoggerObj();
    
    public abstract DSInfo getLevelInfo();
        
    @Override
    protected void onStable() {
        super.onStable();
        updateLevel(500);
    }
    
    protected DSAbstractAction getStreamLogAction() {
        DSAbstractAction act = new DSAbstractAction() {
            
            @Override
            public void prepareParameter(DSInfo info, DSMap parameter) {
            }
            
            @Override
            public ActionResult invoke(DSInfo info, ActionInvocation invocation) {
                return ((StreamableLogNode) info.getParent()).startLogStream(info, invocation);
            }
        };
        act.addParameter("Filter", DSValueType.STRING, "Optional Regex filter");
        act.setResultType(ResultType.STREAM_TABLE);
        act.addValueResult("Log", DSValueType.STRING).setEditor("textarea");
        return act;
    }
    
    private ActionTable startLogStream(final DSInfo actionInfo, final ActionInvocation invocation) {
        Logger loggerObj = getLoggerObj();
        String filter = invocation.getParameters().getString("Filter");
        final Handler handler = new DSLogHandler() {
            
            @Override
            protected void write(LogRecord record) {
                String line = toString(this, record);
                if (filter == null || line.matches(filter)) {
                    invocation.send(new DSList().add(line));
                }
            }
            
//            @Override
//            public void close() {
//                super.close();
//                invocation.close();
//            }
            
        };
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
    
    @Override
    public void onSet(DSInfo info, DSIValue value) {
        super.onSet(info, value);
        if (info == getLevelInfo()) {
            updateLevel(-1);
        }
    }
    
    protected void updateLevel(long retryDelay) {
        if ("Unknown".equals(getLevelInfo().getValue().toString())) {
            Level level = getLoggerLevel();
            if (level != null) {
                put(getLevelInfo(), DSFlexEnum.valueOf(level.toString(), levelRange));
            } else {
                scheduleRetry(retryDelay);
            }
        } else {
            Logger loggerObj = getLoggerObj();
            String level = getLevelInfo().getValue().toString();
            if (loggerObj != null) {
                loggerObj.setLevel(Level.parse(level));
            } else {
                scheduleRetry(retryDelay);
            }
        }
    }
    
    private void scheduleRetry(long retryDelay) {
        if (retryDelay > 0) {
            DSRuntime.runDelayed(new Runnable() {
                @Override
                public void run() {
                    updateLevel(retryDelay * 2);
                }
            }, retryDelay);
        }
    }
    
    private Level getLoggerLevel() {
        Logger obj = getLoggerObj();
        if (obj == null) {
            return null;
        }
        Level l = obj.getLevel();
        while (l == null) {
            obj = obj.getParent();
            l = obj.getLevel();
        }
        return l;
    }

}

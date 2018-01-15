package org.optaplanner.openshift.employeerostering.gwtui.client.common;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.user.client.Timer;

public class TimeoutRestCaller {

    private static Map<Object, TimeoutRestCaller> callerMap = new HashMap<>();
    private Callee callee;
    private Timer timer;

    private TimeoutRestCaller(Callee callee, Object obj) {
        this.callee = callee;
        timer = new Timer() {

            @Override
            public void run() {
                doCall(obj);
            }
        };
    }

    public static void call(Object obj, Callee callee) {
        call(obj, callee, 1000);
    }

    public static void call(Object obj, Callee callee, int delayMilliSec) {
        TimeoutRestCaller caller = callerMap.get(obj);
        if (null == caller) {
            caller = new TimeoutRestCaller(callee, obj);
            callerMap.put(obj, caller);
        } else {
            caller.callee = callee;
        }
        caller.timer.schedule(delayMilliSec);
    }

    private static void doCall(Object obj) {
        TimeoutRestCaller caller = callerMap.get(obj);
        callerMap.remove(obj);
        caller.callee.call();
    }

    public interface Callee {

        void call();
    }
}

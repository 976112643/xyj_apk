package com.mikuwxc.autoreply.wcutil;

import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class Throttle<T> {
    private Action<T> action;
    private long firstTimestamp = 0;
    private long milliseconds;
    private final Set<T> params;
    private Timer timer;

    public interface Action<T> {
        void call(Set<T> set);
    }

    public Throttle(long j, Action<T> action) {
        this.milliseconds = j;
        this.action = action;
        this.timer = new Timer();
        this.params = new HashSet();
    }

    public void call(T t) {
        if (this.firstTimestamp == 0) {
            this.firstTimestamp = System.currentTimeMillis();
            this.timer.schedule(new TimerTask() {
                public void run() {
                    Set hashSet;
                    Throttle.this.firstTimestamp = 0;
                    synchronized (Throttle.this.params) {
                        hashSet = new HashSet(Throttle.this.params);
                        Throttle.this.params.clear();
                    }
                    Throttle.this.action.call(hashSet);
                }
            }, this.milliseconds);
        }
        if (t != null) {
            synchronized (this.params) {
                this.params.add(t);
            }
        }
    }


    final /* synthetic */ void lambda$call$0$Throttle() {
        Set<T> copyParams;
        this.firstTimestamp = 0;
        synchronized (this.params) {
            copyParams = new HashSet(this.params);
            this.params.clear();
        }
        this.action.call(copyParams);
    }
}
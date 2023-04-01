package com.redpxnda.respawnobelisks.scheduled;

public interface Task {
    void tick();
    void fire();
    boolean hasBeenExecuted();
}

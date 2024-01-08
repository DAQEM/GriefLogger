package com.daqem.grieflogger.event;

import dev.architectury.event.EventResult;

public abstract class AbstractEvent {

    public static EventResult interrupt() {
        return EventResult.interruptFalse();
    }

    public static EventResult pass() {
        return EventResult.pass();
    }
}

package com.daqem.grieflogger.thread;

import io.netty.util.concurrent.DefaultThreadFactory;

public class GriefLoggerThreadFactory extends DefaultThreadFactory {

    private static final String THREAD_NAME = "GriefLogger thread";

    public GriefLoggerThreadFactory() {
        super(THREAD_NAME);
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = super.newThread(r);
        thread.setName(THREAD_NAME);
        return thread;
    }
}

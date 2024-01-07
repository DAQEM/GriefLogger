package com.daqem.grieflogger.event;

import com.daqem.grieflogger.thread.ThreadManager;
import dev.architectury.event.events.common.TickEvent;

import java.util.concurrent.ExecutionException;

public class TickEvents {

    public static void registerEvents() {
        TickEvent.SERVER_POST.register(server -> {
            ThreadManager.getAndRemoveCompleted().forEach(
                    (future, onComplete) -> {
                        try {
                            onComplete.onComplete(future.get());
                        } catch (InterruptedException | ExecutionException e) {
                            throw new RuntimeException(e);
                        }
                    }
            );

        });
    }
}

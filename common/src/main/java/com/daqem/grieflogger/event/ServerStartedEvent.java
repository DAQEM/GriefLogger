package com.daqem.grieflogger.event;

import com.daqem.grieflogger.i18n.LanguageManager;

import dev.architectury.event.events.common.LifecycleEvent;

public class ServerStartedEvent {

    public static void registerEvent() {
        LifecycleEvent.SERVER_STARTED.register(LanguageManager::load);
    }
}

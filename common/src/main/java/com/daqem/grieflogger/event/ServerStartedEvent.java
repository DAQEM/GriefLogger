package com.daqem.grieflogger.event;

import com.daqem.grieflogger.i18n.LanguageManager;

import com.daqem.knot.events.EventsService;

public class ServerStartedEvent {

    public static void registerEvent() {
        EventsService.Server.LIFECYCLE_STARTED.register(LanguageManager::load);
    }
}

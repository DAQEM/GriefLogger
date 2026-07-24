package com.daqem.grieflogger.database.service;

import com.daqem.grieflogger.GriefLogger;

public interface Services {

    BlockService BLOCK = new BlockService(GriefLogger.getDatabase());
    ChatService CHAT = new ChatService(GriefLogger.getDatabase());
    CommandService COMMAND = new CommandService(GriefLogger.getDatabase());
    ContainerService CONTAINER = new ContainerService(GriefLogger.getDatabase());
    EntityService ENTITY = new EntityService(GriefLogger.getDatabase());
    ItemService ITEM = new ItemService(GriefLogger.getDatabase());
    LevelService LEVEL = new LevelService(GriefLogger.getDatabase());
    MaterialService MATERIAL = new MaterialService(GriefLogger.getDatabase());
    SessionService SESSION = new SessionService(GriefLogger.getDatabase());
    UsernameService USERNAME = new UsernameService(GriefLogger.getDatabase());
    UserService USER = new UserService(GriefLogger.getDatabase());
}

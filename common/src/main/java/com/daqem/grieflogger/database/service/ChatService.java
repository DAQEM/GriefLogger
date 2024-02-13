package com.daqem.grieflogger.database.service;

import com.daqem.grieflogger.database.Database;
import com.daqem.grieflogger.database.repository.ChatRepository;
import com.daqem.grieflogger.thread.ThreadManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class ChatService {

    private final ChatRepository chatRepository;

    public ChatService(Database database) {
        this.chatRepository = new ChatRepository(database);
    }

    public void createTable() {
        chatRepository.createTable();
    }

    public void createIndexes() {
        chatRepository.createIndexes();
    }

    public void insert(UUID userUuid, Level level, BlockPos pos, String message) {
        chatRepository.insert(System.currentTimeMillis(),
                userUuid.toString(),
                level.dimension().location().toString(),
                pos.getX(),
                pos.getY(),
                pos.getZ(),
                message);
    }

    public void insertAsync(UUID userUuid, Level level, BlockPos pos, String message) {
        ThreadManager.execute(() -> insert(userUuid, level, pos, message));
    }
}

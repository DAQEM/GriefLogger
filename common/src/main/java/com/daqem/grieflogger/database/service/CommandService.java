package com.daqem.grieflogger.database.service;

import com.daqem.grieflogger.database.Database;
import com.daqem.grieflogger.database.repository.ChatRepository;
import com.daqem.grieflogger.database.repository.CommandRepository;
import com.daqem.grieflogger.thread.ThreadManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class CommandService {

    private final CommandRepository commandRepository;

    public CommandService(Database database) {
        this.commandRepository = new CommandRepository(database);
    }

    public void createTableAsync() {
        ThreadManager.execute(commandRepository::createTable);
    }

    public void insert(UUID userUuid, Level level, BlockPos pos, String command) {
        commandRepository.insert(System.currentTimeMillis(),
                userUuid.toString(),
                level.dimension().location().toString(),
                pos.getX(),
                pos.getY(),
                pos.getZ(),
                command);
    }

    public void insertAsync(UUID userUuid, Level level, BlockPos pos, String command) {
        ThreadManager.execute(() -> insert(userUuid, level, pos, command));
    }
}

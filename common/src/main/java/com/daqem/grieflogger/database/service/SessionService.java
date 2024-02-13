package com.daqem.grieflogger.database.service;

import com.daqem.grieflogger.command.filter.FilterList;
import com.daqem.grieflogger.database.Database;
import com.daqem.grieflogger.database.repository.SessionRepository;
import com.daqem.grieflogger.model.action.SessionAction;
import com.daqem.grieflogger.model.history.SessionHistory;
import com.daqem.grieflogger.thread.ThreadManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.UUID;

public class SessionService {

    private final SessionRepository sessionRepository;

    public SessionService(Database database) {
        this.sessionRepository = new SessionRepository(database);
    }

    public void createTable() {
        sessionRepository.createTable();
    }

    public void createIndexes() {
        sessionRepository.createIndexes();
    }

    public void insert(UUID userUuid, Level level, BlockPos pos, SessionAction sessionAction) {
        sessionRepository.insert(
                System.currentTimeMillis(),
                userUuid.toString(),
                level.dimension().location().toString(),
                pos.getX(), pos.getY(), pos.getZ(),
                sessionAction.getId()
        );
    }

    public void insertAsync(UUID userUuid, Level level, BlockPos pos, SessionAction sessionAction) {
        ThreadManager.execute(() -> insert(userUuid, level, pos, sessionAction));
    }

    public List<SessionHistory> getFilteredSessionHistory(Level level, FilterList filterList) {
        return sessionRepository.getFilteredSessionHistory(
                level.dimension().location().toString(),
                filterList
        );
    }
}

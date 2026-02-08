package com.daqem.grieflogger.database.service;

import com.daqem.grieflogger.command.filter.ActionFilter;
import com.daqem.grieflogger.command.filter.FilterList;
import com.daqem.grieflogger.database.Database;
import com.daqem.grieflogger.database.repository.SessionRepository;
import com.daqem.grieflogger.model.action.SessionAction;
import com.daqem.grieflogger.model.history.SessionHistory;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;
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
                level.dimension().identifier().toString(),
                pos.getX(), pos.getY(), pos.getZ(),
                sessionAction.getId()
        );
    }

    public List<SessionHistory> getFilteredSessionHistory(Level level, FilterList filterList) {
        Optional<ActionFilter> actionFilter = filterList.getActionFilter();
        if ((actionFilter.isPresent() && actionFilter.get().getActions().stream().noneMatch(action -> action instanceof SessionAction))
                ||
                (filterList.getIncludeFilter().isPresent())
                ||
                (filterList.getExcludeFilter().isPresent())
        ) {
            return List.of();
        }
        return sessionRepository.getFilteredSessionHistory(
                level.dimension().identifier().toString(),
                filterList
        );
    }
}

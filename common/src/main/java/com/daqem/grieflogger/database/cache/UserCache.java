package com.daqem.grieflogger.database.cache;

import com.daqem.grieflogger.database.service.Services;
import com.daqem.grieflogger.database.service.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserCache implements ICache {

    private final static UserService userService = Services.USER;

    private long usernameTime = 0;
    private final Map<Integer, String> usernames = new HashMap<>();

    public Map<Integer, String> getAllUsernames() {
        if (usernameTime + 1000 < System.currentTimeMillis()) {
            usernames.clear();
            usernames.putAll(userService.getAllUsernames());
            usernameTime = System.currentTimeMillis();
        }
        return usernames;
    }

    public List<Integer> getAllUserIds() {
        return List.copyOf(getAllUsernames().keySet());
    }
}

package com.github.ivacoronel.server.services;

import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import com.github.ivacoronel.server.domain.model.User;

public interface ScheduleTaskService {

    public void publishChallenge(User user);
    
    public void handleActivity(User user, Map<Long, ScheduledExecutorService> timers);
}

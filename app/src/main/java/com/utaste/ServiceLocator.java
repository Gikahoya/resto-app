package com.utaste;

import android.content.Context;

import com.utaste.data.memory.InMemoryUserRepository;
import com.utaste.service.WaiterService;
import com.utaste.data.sqlite.UserDao;


public final class ServiceLocator {

    private static WaiterService waiters;  // livrable 1
    private static UserDao userDao;        // livrable 2

    private ServiceLocator() {}            // prevent instantiation

    // --- WaiterService
    public static synchronized WaiterService waiters() {
        if (waiters == null) {
            waiters = new WaiterService(InMemoryUserRepository.getInstance());
        }
        return waiters;
    }

    public static synchronized UserDao userDao(Context ctx) {
        if (userDao == null) {
            userDao = new UserDao(ctx);
        }
        return userDao;
    }
}

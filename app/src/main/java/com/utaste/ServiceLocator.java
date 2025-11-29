package com.utaste;

import android.content.Context;

import com.utaste.data.memory.InMemoryUserRepository;
import com.utaste.domain.user.UserRepository;
import com.utaste.service.WaiterService;
import com.utaste.data.sqlite.UserDao;


public final class ServiceLocator {

    private static WaiterService waiters;
    private static UserDao userDao;
    // ✅ AJOUT : une variable statique pour notre User Repository
    private static UserRepository userRepository;

    private ServiceLocator() {}

    // --- WaiterService
    public static synchronized WaiterService waiters() {
        if (waiters == null) {
            // ✅ MODIFICATION : Utilise la nouvelle méthode pour la cohérence
            waiters = new WaiterService(getUserRepository());
        }
        return waiters;
    }


    public static synchronized UserRepository getUserRepository() {
        if (userRepository == null) {
            userRepository = InMemoryUserRepository.getInstance();
        }
        return userRepository;
    }

    public static synchronized UserDao userDao(Context ctx) {
        if (userDao == null) {
            userDao = new UserDao(ctx);
        }
        return userDao;
    }
}

package com.utaste;

import com.utaste.data.memory.InMemoryUserRepository;
import com.utaste.service.WaiterService;

public final class ServiceLocator {

    private static WaiterService waiters;

    private ServiceLocator() {}

    public static WaiterService waiters() {
        if (waiters == null) {
            waiters = new WaiterService(InMemoryUserRepository.getInstance());
        }
        return waiters;
    }
}

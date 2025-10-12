package com.utaste;

import com.utaste.data.memory.InMemoryUserStore;
import com.utaste.service.WaiterService;

public final class ServiceLocator {
    private static InMemoryUserStore store;
    private static WaiterService waiters;

    private ServiceLocator() {}

    public static InMemoryUserStore store() {
        if (store == null) store = new InMemoryUserStore();
        return store;
    }

    public static WaiterService waiters() {
        if (waiters == null) waiters = new WaiterService(store());
        return waiters;
    }
}

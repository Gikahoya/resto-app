package com.utaste.data.memory;

import java.util.HashMap;
import java.util.Map;
import com.utaste.domain.user.Role;
import com.utaste.domain.user.User;

public class InMemoryUserStore {
    private final Map<String, User> users = new HashMap<>();
    public InMemoryUserStore(){

    }
    public Map<String, User> data(){ return users; } //  email
}

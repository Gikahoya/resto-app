package com.utaste.data.memory;

import com.utaste.domain.user.Admin;
import com.utaste.domain.user.Chef;
import com.utaste.domain.user.Waiter;
import com.utaste.domain.user.User;
import com.utaste.domain.user.UserRepository;
import com.utaste.domain.user.Credentials;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class InMemoryUserRepository implements UserRepository {

    private static InMemoryUserRepository instance;
    private final Map<String, User> users = new HashMap<>();

    private InMemoryUserRepository() {
        // Création des utilisateurs par défaut
        User admin = new Admin("admin", "admin-pwd");
        User chef = new Chef("chef", "chef-pwd");
        User waiter1 = new Waiter("waiter1", "waiter-pwd");
        User waiter2 = new Waiter("waiter2", "waiter-pwd");

        users.put(admin.id, admin);
        users.put(chef.id, chef);
        users.put(waiter1.id, waiter1);
        users.put(waiter2.id, waiter2);
    }

    // Retourne toujours la même instance partagée
    public static InMemoryUserRepository getInstance() {
        if (instance == null) {
            instance = new InMemoryUserRepository();
        }
        return instance;
    }
    @Override
    public User findByCredentials(Credentials credentials) {
        User user = users.get(credentials.id);
        if (user != null && user.password.equals(credentials.password)) {
            return user;
        }
        return null;
    }

    @Override
    public User findByEmail(String email) {
        // non utilisé ici, mais nécessaire pour respecter l'interface
        for (User user : users.values()) {
            if (user.email != null && user.email.equals(email)) {
                return user;
            }
        }
        return null;
    }

    @Override
    public User findById(String id) {
        return users.get(id);
    }

    @Override
    public void addUser(User user) {
        if (users.containsKey(user.id)) {
            throw new IllegalArgumentException("Username already exists");
        }
        users.put(user.id, user);
    }

    @Override
    public void updateUser(User user) {
        users.put(user.id, user);
    }

    @Override
    public void deleteUser(String id) {
        users.remove(id);
    }

    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }
}
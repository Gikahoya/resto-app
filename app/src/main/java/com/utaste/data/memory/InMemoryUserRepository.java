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
        // ---- Utilisateurs par défaut ----

        // Admin
        Admin admin = new Admin("admin", "admin-pwd");
        admin.email = "admin@utaste.com";
        admin.firstName = "Admin";
        admin.lastName = "User";

        // Chef
        Chef chef = new Chef("chef", "chef-pwd");
        chef.email = "chef@utaste.com";
        chef.firstName = "Head";
        chef.lastName = "Chef";

        // Waiter 1
        Waiter waiter1 = new Waiter("waiter1", "waiter-pwd");
        waiter1.email = "waiter1@utaste.com";
        waiter1.firstName = "John";
        waiter1.lastName = "Doe";

        // Waiter 2
        Waiter waiter2 = new Waiter("waiter2", "waiter-pwd");
        waiter2.email = "waiter2@utaste.com";
        waiter2.firstName = "Jane";
        waiter2.lastName = "Smith";

        users.put(admin.id, admin);
        users.put(chef.id, chef);
        users.put(waiter1.id, waiter1);
        users.put(waiter2.id, waiter2);
    }

    public static InMemoryUserRepository getInstance() {
        if (instance == null) {
            instance = new InMemoryUserRepository();
        }
        return instance;
    }

    @Override
    public User findByCredentials(Credentials credentials) {
        if (credentials == null || credentials.id == null) return null;

        // 1) Essai par ID (username = id)
        User user = users.get(credentials.id);
        if (user != null && user.password != null
                && user.password.equals(credentials.password)) {
            return user;
        }

        // 2) Essai par email (username utilisé comme email)
        user = findByEmail(credentials.id);
        if (user != null && user.password != null
                && user.password.equals(credentials.password)) {
            return user;
        }

        return null;
    }

    @Override
    public User findByEmail(String email) {
        if (email == null) return null;
        for (User user : users.values()) {
            if (email.equals(user.email)) {
                return user;
            }
        }
        return null;
    }

    @Override
    public User findById(String id) {
        if (id == null) return null;
        return users.get(id);
    }

    @Override
    public void addUser(User user) {
        if (user == null || user.id == null) {
            throw new IllegalArgumentException("User id is required");
        }
        if (users.containsKey(user.id)) {
            throw new IllegalArgumentException("User with this ID already exists");
        }
        users.put(user.id, user);
    }

    @Override
    public void updateUser(User user) {
        if (user == null || user.id == null) {
            throw new IllegalArgumentException("User id is required");
        }
        users.put(user.id, user);
    }

    @Override
    public void deleteUser(String id) {
        if (id == null) return;
        users.remove(id);
    }

    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }
}

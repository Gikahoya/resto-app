package com.utaste.data.memory;

import com.utaste.domain.user.Admin;
import com.utaste.domain.user.Chef;
import com.utaste.domain.user.User;
import com.utaste.domain.user.UserRepository;
import com.utaste.domain.user.Credentials;
import com.utaste.domain.user.Waiter;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class InMemoryUserRepository implements UserRepository {

    private static InMemoryUserRepository instance;
    private final Map<String, User> users = new HashMap<>();

    private InMemoryUserRepository() {
        // La logique de création des utilisateurs est maintenant dans reset()
        reset();
    }

    public static InMemoryUserRepository getInstance() {
        if (instance == null) {
            instance = new InMemoryUserRepository();
        }
        return instance;
    }

    /**
     * MÉTHODE AJOUTÉE :
     * Réinitialise les données en recréant les utilisateurs par défaut.
     */
    public void reset() {
        // Vide la liste actuelle des utilisateurs
        users.clear();

        // Recrée les utilisateurs par défaut
        User admin = new Admin("admin", "admin-pwd");
        User chef = new Chef("chef", "chef-pwd");

        User waiter1 = new Waiter("waiter1", "waiter-pwd");
        waiter1.email = "waiter1@utaste.com";
        waiter1.firstName = "John";
        waiter1.lastName = "Doe";

        User waiter2 = new Waiter("waiter2", "waiter-pwd");
        waiter2.email = "waiter2@utaste.com";
        waiter2.firstName = "Jane";
        waiter2.lastName = "Smith";

        users.put(admin.id, admin);
        users.put(chef.id, chef);
        users.put(waiter1.id, waiter1);
        users.put(waiter2.id, waiter2);
    }

    @Override
    public User findByCredentials(Credentials credentials) {
        User user = users.get(credentials.id);
        if (user != null && user.password.equals(credentials.password)) {
            return user;
        }

        user = findByEmail(credentials.id); // On utilise le champ "username" comme un email
        if (user != null && user.password.equals(credentials.password)) {
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
        return users.get(id);
    }

    @Override
    public void addUser(User user) {
        if (users.containsKey(user.id)) {
            throw new IllegalArgumentException("User with this ID already exists");
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

    /**
     * Changes the password for a given user.
     * The user can be identified by their ID or email.
     *
     * @param userIdOrEmail The ID or email of the user.
     * @param newPassword   The new password to set.
     * @return true if the password was changed successfully, false otherwise (e.g., user not found).
     */
    public boolean changePassword(String userIdOrEmail, String newPassword) {
        if (userIdOrEmail == null || newPassword == null || newPassword.trim().isEmpty()) {
            return false;
        }

        User user = findById(userIdOrEmail);
        if (user == null) {
            user = findByEmail(userIdOrEmail);
        }

        if (user != null) {
            user.password = newPassword.trim();
            user.updatedAt = System.currentTimeMillis();
            updateUser(user);
            return true;
        }

        return false; // User not found
    }
}

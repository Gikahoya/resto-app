package com.utaste.service;

import com.utaste.domain.user.Role;
import com.utaste.domain.user.User;
import com.utaste.domain.user.UserRepository;
import com.utaste.domain.user.Waiter;
import java.util.List;
import java.util.UUID;

public class WaiterService {
    private final UserRepository repo;

    public WaiterService(UserRepository repo) {
        this.repo = repo;
    }

    public List<User> list() {
        List<User> allUsers = repo.getAllUsers();
        List<User> waitersOnly = new java.util.ArrayList<>();
        for (User user : allUsers) {
            if (user.role == Role.WAITER) {
                waitersOnly.add(user);
            }
        }
        return waitersOnly;
    }

    public User findByEmail(String email) {
        return repo.findByEmail(email);
    }


    public User create(String first, String last, String email, String pwd) {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("A valid email is required");
        }
        if (repo.findByEmail(email) != null) {
            throw new IllegalArgumentException("Email already in use");
        }

        // ✅ VOTRE IDÉE : on extrait la partie avant le '@' pour en faire l'ID
        String newId = email.split("@")[0];

        // On vérifie que cet ID n'est pas déjà pris (sécurité)
        if (repo.findById(newId) != null) {
            throw new IllegalArgumentException("Username '" + newId + "' is already taken.");
        }

        // On crée le Waiter avec ce nouvel ID simple
        User u = new Waiter(newId, pwd);

        u.email = email;
        u.firstName = first;
        u.lastName  = last;

        repo.addUser(u);
        return u;
    }

    public User update(String oldEmail, String first, String last, String newEmail, String pwd) {
        User userToUpdate = repo.findByEmail(oldEmail);
        if (userToUpdate == null) throw new IllegalArgumentException("User not found");

        if (!oldEmail.equals(newEmail)) {
            if (repo.findByEmail(newEmail) != null) {
                throw new IllegalArgumentException("New email is already in use.");
            }
        }

        userToUpdate.email = newEmail;
        userToUpdate.firstName = first;
        userToUpdate.lastName = last;
        if (pwd != null && !pwd.isBlank()) {
            userToUpdate.password = pwd;
        }
        userToUpdate.updatedAt = System.currentTimeMillis();

        repo.updateUser(userToUpdate);
        return userToUpdate;
    }

    public void delete(String email) {
        User userToDelete = repo.findByEmail(email);
        if (userToDelete == null) throw new IllegalArgumentException("User not found");
        repo.deleteUser(userToDelete.id);
    }
}

package com.utaste.domain.user;

import java.util.List;

public interface UserRepository {

    public abstract User findByCredentials(Credentials creds);
    public abstract User findByEmail(String email);
    public abstract User findById(String id);
    public abstract void addUser(User user);
    public abstract void updateUser(User user);
    public abstract void deleteUser(String email);
    public abstract List<User> getAllUsers();
}

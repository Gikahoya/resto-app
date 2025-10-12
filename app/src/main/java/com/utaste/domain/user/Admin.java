package com.utaste.domain.user;

public class Admin extends User {

    public Admin(String id, String password) {
        super(id, null, password, Role.ADMIN);
    }
}

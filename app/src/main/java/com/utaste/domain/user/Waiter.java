package com.utaste.domain.user;

public class Waiter extends User {

    public Waiter(String id, String password) {
        super(id, null, password, Role.WAITER);
    }
}

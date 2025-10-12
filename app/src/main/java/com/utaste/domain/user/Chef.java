package com.utaste.domain.user;

public class Chef extends User {

    public Chef(String id, String password) {
        super(id, null, password, Role.CHEF);
    }
}

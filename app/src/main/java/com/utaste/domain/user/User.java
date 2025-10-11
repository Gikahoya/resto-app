package com.utaste.domain.user;

public class User {
    public final String id;                  // id technique (UUID)
    public String firstName, lastName;
    public String email, password;
    public Role role;
    public long createdAt, updatedAt;        // timestamps en ms

    public User(String id, String email, String pwd, Role role){
        this.id=id; this.email=email; this.password=pwd; this.role=role;
        this.createdAt=this.updatedAt=System.currentTimeMillis();
    }
}

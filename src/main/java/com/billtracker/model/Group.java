package com.billtracker.model;

import java.util.ArrayList;
import java.util.List;

public class Group {
    private String id;
    private String name;
    private List<User> users;

    public Group(String id, String name) {
        this.id = id;
        this.name = name;
        this.users = new ArrayList<>();
    }

    public void addUser(User user) {
        this.users.add(user);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<User> getUsers() {
        return users;
    }
}

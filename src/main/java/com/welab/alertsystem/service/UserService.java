package com.welab.alertsystem.service;

import com.welab.alertsystem.model.User;

import java.util.List;

public interface UserService {
    public User getUserById(Integer id);
    public User getUserByEmail(String email);
    public List<User> getApproverUserList();

}

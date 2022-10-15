package com.welab.alertsystem.DAO;

import com.welab.alertsystem.model.User;

import java.util.List;

public interface UserDao {

    List<User> getUserById(Integer id);

    List<User> getUserByIdAndRole(Integer id, String role);

    List<User> getUserByEmail(String email);
    List<User> getUserListByRole(String role);
}

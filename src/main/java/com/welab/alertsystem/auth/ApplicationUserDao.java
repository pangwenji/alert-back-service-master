package com.welab.alertsystem.auth;

import com.welab.alertsystem.model.User;

import java.util.List;
import java.util.Optional;

public interface ApplicationUserDao {
    List<User> selectApplicationUserByUsername(String username);

}

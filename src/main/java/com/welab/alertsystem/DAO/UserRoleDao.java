package com.welab.alertsystem.DAO;

import com.welab.alertsystem.model.UserRole;

import java.util.List;

public interface UserRoleDao {

    public UserRole getUserRoleByName(String roleName);

    public UserRole getUserRoleById(Integer Id);

    public List<UserRole> getUserRoleList();

}

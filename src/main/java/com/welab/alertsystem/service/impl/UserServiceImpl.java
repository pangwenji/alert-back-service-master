package com.welab.alertsystem.service.impl;

import com.welab.alertsystem.model.User;
import com.welab.alertsystem.DAO.UserDao;
import com.welab.alertsystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    UserDao userDao;

    @Autowired
    public UserServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public User getUserById(Integer id) {
        return null;
    }

    @Override
    public User getUserByEmail(String email) {
        return userDao.getUserByEmail(email).get(0);
    }

    @Override
    public List<User> getApproverUserList() {
        return userDao.getUserListByRole("APPROVER");
    }


}

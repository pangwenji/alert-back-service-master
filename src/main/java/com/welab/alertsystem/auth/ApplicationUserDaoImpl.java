package com.welab.alertsystem.auth;

import com.welab.alertsystem.model.User;
import com.welab.alertsystem.DAO.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ApplicationUserDaoImpl implements ApplicationUserDao {

    private PasswordEncoder passwordEncoder;

    UserDao userDao;

    @Autowired
    public ApplicationUserDaoImpl(PasswordEncoder passwordEncoder, UserDao userDao) {
        this.passwordEncoder = passwordEncoder;
        this.userDao = userDao;
    }

    @Override
    public List<User> selectApplicationUserByUsername(String username) {
        List<User> userList = userDao.getUserByEmail(username);
        return userList;
    }


}

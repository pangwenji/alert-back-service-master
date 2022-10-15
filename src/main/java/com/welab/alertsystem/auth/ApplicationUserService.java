package com.welab.alertsystem.auth;

import com.welab.alertsystem.model.User;
import com.welab.alertsystem.model.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ApplicationUserService implements UserDetailsService {

    ApplicationUserDao applicationUserDao;
    PasswordEncoder passwordEncoder;

    @Autowired
    public ApplicationUserService(ApplicationUserDao applicationUserDao, PasswordEncoder passwordEncoder) {
        this.applicationUserDao = applicationUserDao;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        List<User> userList = applicationUserDao.selectApplicationUserByUsername(username);

        if (userList.size() == 0) {
            throw new UsernameNotFoundException(String.format("Username %s not found", username));
        }
        User user = userList.get(0);
        Set<SimpleGrantedAuthority> authSet = new HashSet<>();
        List<UserRole> roleList = user.getRoleList();
        for (int i = 0; i < roleList.size(); i++) {
            authSet.add(new SimpleGrantedAuthority("ROLE_" + roleList.get(i).getName()));
        }
        UserDetails userDetails = new ApplicationUser(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                passwordEncoder.encode(user.getPassword()),
                authSet,
                true,
                true,
                true,
                true
        );
        return userDetails;
    }
}

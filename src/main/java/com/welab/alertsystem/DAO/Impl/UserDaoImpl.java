package com.welab.alertsystem.DAO.Impl;

import com.welab.alertsystem.model.User;
import com.welab.alertsystem.model.UserRole;
import com.welab.alertsystem.DAO.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class UserDaoImpl implements UserDao {
    @Autowired
    JdbcTemplate jdbcTemplate;

    @Override
    public List<User> getUserById(Integer id) {
        String sql = "" +
                "SELECT a.*, STRING_AGG (c.name, ';') as role_list FROM users as a " +
                "join users__user_roles as b " +
                "on a.id = b.user_id " +
                "join user_roles as c " +
                "on b.user_role_id = c.id " +
                "WHERE a.id = ?" +
                "GROUP by a.id " +
                "ORDER By a.id ASC" +
                "";
        return jdbcTemplate.query(sql, mapUserFromDb(), new Object[]{id});
    }

    @Override
    public List<User> getUserByIdAndRole(Integer id, String role) {
        String sql = "" +
                "With tmp as " +
                "(SELECT a.id FROM users as a " +
                "join users__user_roles as b " +
                "on a.id = b.user_id " +
                "join user_roles as c " +
                "on b.user_role_id = c.id " +
                "WHERE c.name= ? AND ) " +
                "SELECT a.*, STRING_AGG (c.name, ';') as role_list " +
                "FROM users as a " +
                "join users__user_roles as b " +
                "on a.id = b.user_id " +
                "join user_roles as c " +
                "on b.user_role_id = c.id " +
                "WHERE a.id IN (SELECT id FROM tmp) " +
                "GROUP by a.id " +
                "ORDER By a.id ASC" ;
        return jdbcTemplate.query(sql, mapUserFromDb(), new Object[]{role});
    }

    @Override
    public List<User> getUserByEmail(String email) {
        String sql = "" +
                "SELECT a.*, STRING_AGG (c.name, ';') as role_list FROM users as a " +
                "join users__user_roles as b " +
                "on a.id = b.user_id " +
                "join user_roles as c " +
                "on b.user_role_id = c.id " +
                "WHERE a.email = ? " +
                "GROUP by a.id " +
                "ORDER By a.id ASC" +
                "";
        return jdbcTemplate.query(sql, mapUserFromDb(), new Object[]{email});
    }

    @Override
    public List<User> getUserListByRole(String role) {
        String sql = "" +
                "With tmp as " +
                "(SELECT a.id FROM users as a " +
                "join users__user_roles as b " +
                "on a.id = b.user_id " +
                "join user_roles as c " +
                "on b.user_role_id = c.id " +
                "WHERE c.name=? ) " +
                "SELECT a.*, STRING_AGG (c.name, ';') as role_list " +
                "FROM users as a " +
                "join users__user_roles as b " +
                "on a.id = b.user_id " +
                "join user_roles as c " +
                "on b.user_role_id = c.id " +
                "WHERE a.id IN (SELECT id FROM tmp) " +
                "GROUP by a.id " +
                "ORDER By a.id ASC" ;
        return jdbcTemplate.query(sql, mapUserFromDb(), new Object[]{role});
    }

    private RowMapper<User> mapUserFromDb() {
        return (resultSet, i) -> {
            Integer id = Optional.ofNullable(resultSet.getString("id")).map(Integer::parseInt).orElse(null);
            String email = resultSet.getString("email");
            String password = resultSet.getString("password");
            String username = resultSet.getString("username");
            LocalDateTime createdAt = resultSet.getTimestamp("created_at").toLocalDateTime();
            LocalDateTime updatedAt = resultSet.getTimestamp("updated_at").toLocalDateTime();
            List<UserRole> roleList = Arrays.stream(resultSet.getString("role_list")
                                    .split(";"))
                                    .map(e-> new UserRole(null, e, null)).collect(Collectors.toList());
            User user = new User();
            user.setId(id);
            user.setEmail(email);
            user.setUsername(username);
            user.setPassword(password);
            user.setCreatedAt(createdAt);
            user.setUploadedAt(updatedAt);
            user.setRoleList(roleList);
            return user;
        };
    }

}

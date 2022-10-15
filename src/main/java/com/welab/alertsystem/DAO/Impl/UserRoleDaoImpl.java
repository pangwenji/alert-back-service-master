package com.welab.alertsystem.DAO.Impl;

import com.welab.alertsystem.model.UserRole;
import com.welab.alertsystem.DAO.UserRoleDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;

@Repository
public class UserRoleDaoImpl implements UserRoleDao {

    @Autowired
    JdbcTemplate jdbcTemplate;



    @Override
    public UserRole getUserRoleByName(String roleName) {
        String sql = "" +
                "SELECT " +
                "*" +
                "FROM user_roles " +
                "WHERE name = ?";

        return jdbcTemplate.query(sql, mapUserRoleFromDb(),new Object[]{roleName}).get(0);
    }

    @Override
    public UserRole getUserRoleById(Integer Id) {
        String sql = "" +
                "SELECT " +
                "*" +
                "FROM user_roles " +
                "WHERE id = ? ";

        return jdbcTemplate.query(sql, mapUserRoleFromDb(),new Object[]{Id}).get(0);
    }

    @Override
    public List<UserRole> getUserRoleList() {
        String sql = "" +
                "SELECT * " +
                "FROM user_roles ";
        return jdbcTemplate.query(sql, mapUserRoleFromDb());
    }

    private RowMapper<UserRole> mapUserRoleFromDb() {
        return (resultSet, i) -> {
            Integer id = resultSet.getInt("id");
            String name = resultSet.getString("name");
            String description = resultSet.getString("description");
            Date created_at = resultSet.getDate("created_at");
            Date updated_at = resultSet.getDate("updated_at");
            return new UserRole(
                    id,
                    name,
                    description
            );
        };
    }
}

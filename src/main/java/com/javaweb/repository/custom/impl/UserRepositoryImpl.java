package com.javaweb.repository.custom.impl;

import com.javaweb.builder.UserSearchBuilder;
import com.javaweb.entity.User;
import com.javaweb.repository.custom.UserRepositoryCustom;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class UserRepositoryImpl implements UserRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    public void getNormalWhere(UserSearchBuilder userSearchBuilder, StringBuilder where) {

        try {
            Field[] fields = UserSearchBuilder.class.getDeclaredFields();
            for (Field item : fields) {
                item.setAccessible(true);
                String fieldName = item.getName().toLowerCase();
                // NullPointerException
                if (!fieldName.equals("roles")) {
                    Object value = item.get(userSearchBuilder);
                    if (value != null && !value.equals("")) {
                        if (item.getType().getName().equals("java.lang.Long")) {
                            where.append(" AND u." + fieldName + " = " + value);
                        } else if (item.getType().getName().equals("java.lang.Integer")) {
                            where.append(" AND u." + fieldName + " = " + value);
                        } else if (item.getType().getName().equals("java.lang.String")) {
                            where.append(" AND u." + fieldName + " LIKE '%" + value + "%' ");
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void getSpecialWhere(UserSearchBuilder userSearchBuilder, StringBuilder where) {
        Set<String> roles = userSearchBuilder.getRoles();
        if (roles != null && !roles.isEmpty()) {


            // Add EXISTS clause to check for role matches
            where.append(" AND EXISTS (")
                    .append("SELECT 1 FROM user_role ur ")
                    .append("JOIN role r ON ur.role_id = r.id ")
                    .append("WHERE ur.user_id = u.id AND r.code IN (");

            // Add role names as parameters with OR conditions
            String roleNames = roles.stream()
                    .map(role -> "'" + role + "'")
                    .collect(Collectors.joining(", "));

            where.append(roleNames).append("))");
        }
    }

    @Override
    public List<User> getAllUsers(UserSearchBuilder userSearchBuilder) {
        StringBuilder sql = new StringBuilder("SELECT * FROM user u");
        StringBuilder where = new StringBuilder(" WHERE 1=1 ");

        getNormalWhere(userSearchBuilder, where);
        getSpecialWhere(userSearchBuilder, where);
        sql.append(where).append(" GROUP BY u.id ");
        System.out.println(sql);
        Query query = entityManager.createNativeQuery(sql.toString(), User.class);
        return query.getResultList();
    }
}

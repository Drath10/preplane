package com.preplane.dev.repositories.User;

import java.util.List;
import java.sql.Timestamp;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.preplane.dev.assets.SQLResult;
import com.preplane.dev.models.User;
import com.preplane.dev.rowMappers.CountMapper;
import com.preplane.dev.rowMappers.User.UserRowMapper;

import org.springframework.jdbc.core.RowMapper;

@Repository
public class JDBCUserRepository implements UserRepository {

    @Autowired
    private JdbcTemplate template;
    private RowMapper<User> mapper;
    private RowMapper<Integer> countMapper;

    public JDBCUserRepository() {
        this.mapper = new UserRowMapper();
        this.countMapper = new CountMapper();
    }

    @Override
    @Transactional
    public SQLResult<Integer> save(User user) {
        String sqlQuery = "INSERT INTO user (username, password, email_address, first_name, last_name) VALUES (?,?,?,?,?)";
        var result = new SQLResult<Integer>();

        try {
            int rowCount = template.update(sqlQuery, user.getUsername(), user.getPassword(), user.getEmailAddress(),
                    user.getFirstName(), user.getLastName());
            result.response = rowCount;

            if (rowCount == 1) {
                result.message = "The user was created succesfully.";
                result.statusCode = HttpStatus.CREATED;
            } else {
                result.message = "There was an error in creating the user.";
                result.statusCode = HttpStatus.BAD_REQUEST;
            }
        } catch (Exception e) {
            System.out.println(e);
            result.message = "There was an error in creating the user. Error Message: " + e.getMessage();
            result.statusCode = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return result;
    }

    @Override
    @Transactional
    public SQLResult<User> findById(int userId) {
        String sqlQuery = "SELECT * FROM user WHERE user_id = ?";
        var result = new SQLResult<User>();

        try {
            var response = template.query(sqlQuery, this.mapper, userId);

            if (!response.isEmpty()) {
                result.message = "User fetched successfully.";
                result.statusCode = HttpStatus.OK;
                result.response = response.get(0);
            } else {
                result.message = "There is no user with such the provided ID.";
                result.statusCode = HttpStatus.BAD_REQUEST;
            }
        } catch (Exception e) {
            System.out.println(e);
            result.message = "There was an error in fetching the user. Error Message: " + e.getMessage();
            result.statusCode = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return result;
    }

    @Override
    @Transactional
    public SQLResult<Integer> deleteById(int userId) {
        String sqlQuery = "DELETE FROM user WHERE user_id = ?";
        var result = new SQLResult<Integer>();

        try {
            var rowCount = template.update(sqlQuery, userId);
            result.response = rowCount;

            if (rowCount == 1) {
                result.message = "User deleted successfully.";
                result.statusCode = HttpStatus.OK;
            } else {
                result.message = "There is no user with such the provided ID.";
                result.statusCode = HttpStatus.BAD_REQUEST;
            }
        } catch (Exception e) {
            System.out.println(e);
            result.message = "There was an error in deleting the user. Error Message: " + e.getMessage();
            result.statusCode = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return result;
    }

    @Override
    @Transactional
    public SQLResult<List<User>> findAll(int limit, int offset) {
        String sqlQuery = "SELECT * FROM user ORDER BY user_id LIMIT ? OFFSET ?";
        var result = new SQLResult<List<User>>();

        try {
            var response = template.query(sqlQuery, this.mapper, limit, offset);
            result.response = response;

            if (!response.isEmpty()) {
                result.message = "Users fetched successfully.";
                result.statusCode = HttpStatus.OK;
            } else {
                result.message = "There are no registered users.";
                result.statusCode = HttpStatus.NO_CONTENT;
            }
        } catch (Exception e) {
            System.out.println(e);
            result.message = "There was an error in fetching the users. Error Message: " + e.getMessage();
            result.statusCode = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return result;
    }

    @Override
    @Transactional
    public void updateLoginTime(int userId) {
        String sqlQuery = "UPDATE user SET last_login = ? WHERE user_id = ?";
        try {
            template.update(sqlQuery, new Timestamp(System.currentTimeMillis()), userId);
        } catch (Exception e) {
            System.out.println("There was an error in updating the last login time: ");
            System.out.println(e);
        }
    }

    @Override
    @Transactional
    public boolean usernameExists(String username) {
        String sqlQuery = "SELECT COUNT(*) AS count FROM user WHERE username = ?";
        Integer count = template.query(sqlQuery, this.countMapper, username).get(0);
        return count > 0;
    }

    @Override
    @Transactional
    public boolean emailExists(String email) {
        String sqlQuery = "SELECT COUNT(*) AS count FROM user WHERE email_address = ?";
        Integer count = template.query(sqlQuery, this.countMapper, email).get(0);
        return count > 0;
    }

    @Override
    @Transactional
    public Optional<User> findByUsername(String username) {
        String sqlQuery = "SELECT * FROM user WHERE username = ?";
        var users = template.query(sqlQuery, this.mapper, username);

        if (users.size() == 0)
            return Optional.empty();

        return Optional.of(users.get(0));
    }
}

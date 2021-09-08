package it.polito.ezshop.controllers;
import it.polito.ezshop.data.User;
import it.polito.ezshop.exceptions.*;
import it.polito.ezshop.model.UserObject;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserManagement {


    public static User getUser(Integer id) throws InvalidUserIdException{
        if(id == null || id <= 0) throw new InvalidUserIdException();
        try {
            Connection connection = DB.getConnectionToDB();
            String sql = "SELECT rowid, * FROM users WHERE rowid=?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if(resultSet.next()) {
                String name = resultSet.getString("username");
                String password = resultSet.getString("password");
                String role = resultSet.getString("role");
                return new UserObject(name, password, role, id);
            }
            return null;
        } catch (SQLException exception) {
            //exception.printStackTrace();
            return null;
        }
    }

    public static User getUserByUsername(String username) {
        try {
            Connection connection = DB.getConnectionToDB();
            String search = "SELECT rowid, * FROM users WHERE username=?";
            PreparedStatement preparedStatement = connection.prepareStatement(search);
            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();
            if(resultSet.next()) {
                int id = resultSet.getInt("rowid");
                String password = resultSet.getString("password");
                String role = resultSet.getString("role");
                return new UserObject(username, password, role, id);
            }
            return null;
        } catch (SQLException exception) {
            //exception.printStackTrace();
            return null;
        }
    }

    public static Integer createUser(String username, String password, String role) throws InvalidUsernameException, InvalidRoleException, InvalidPasswordException {
        if(username == null || username.isEmpty()) throw new InvalidUsernameException();
        if(password == null || password.isEmpty()) throw new InvalidPasswordException();
        validateRole(role);
        try {
            if(getUserByUsername(username) != null) return -1;
            Connection connection = DB.getConnectionToDB();
            String sql = "INSERT INTO users(username, password, role) VALUES(?,?,?)";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            preparedStatement.setString(3, role);
            if(preparedStatement.executeUpdate() != 0) {
                User u = getUserByUsername(username);
                if(u != null) return u.getId();
            }
            return -1;
        } catch (SQLException exception) {
            //exception.printStackTrace();
            return -1;
        }

    }

    public static boolean deleteUser(Integer userID) throws InvalidUserIdException {
        if(userID == null || userID <= 0) throw new InvalidUserIdException();
        try {
            Connection connection = DB.getConnectionToDB();
            String sql = "DELETE FROM users WHERE rowid=?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, userID);
            return preparedStatement.executeUpdate()!=0;

        } catch (SQLException exception) {
            //exception.printStackTrace();
            return false;
        }
    }

    public static List<User> getAllUsers(){
        List<User> result = new ArrayList<>();
        try {
            Connection connection = DB.getConnectionToDB();
            String sql = "SELECT rowid, * FROM users";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            while(resultSet.next()) {
                int id = resultSet.getInt("rowid");
                String username = resultSet.getString("username");
                String password = resultSet.getString("password");
                String role = resultSet.getString("role");
                result.add(new UserObject(username, password, role, id));
            }
        } catch (SQLException exception) {
            //exception.printStackTrace();
            return null;
        }
        return result;
    }

    public static boolean updateUserRights(Integer userId, String newRole) throws InvalidRoleException, InvalidUserIdException{
        if(userId == null || userId <= 0) throw new InvalidUserIdException();
        validateRole(newRole);
        if(getUser(userId) == null) return false;
        try {
            Connection connection = DB.getConnectionToDB();
            String sql = "UPDATE users SET role=? WHERE rowid=?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, newRole);
            preparedStatement.setInt(2, userId);
            preparedStatement.executeUpdate();
            return true;
        } catch (SQLException exception) {
            //exception.printStackTrace();
            return false;
        }
    }

    private static void validateRole(String role) throws InvalidRoleException {
        if (role == null || !(role.equals("Cashier") || role.equals("Administrator") || role.equals("ShopManager")))
            throw new InvalidRoleException();
    }
}

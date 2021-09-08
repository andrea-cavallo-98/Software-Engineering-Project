package it.polito.ezshop.model;
import  it.polito.ezshop.data.User;

public class UserObject implements User {
    private String username;
    private String password;
    private String role;
    private Integer id;

    public UserObject(String username, String password, String role, Integer id) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.id = id;
    }



    @Override
    public Integer getId() {
        return this.id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public void setUsername(String username) {
        this.username=username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String getRole() {
        return role;
    }

    @Override
    public void setRole(String role) {
        this.role = role;
    }
}

package com.example.project_1.Model;

// POJO class
public class User {
    private int id;
    private String userName;
    private String password;
    private String email;
    private String specialInfo; // Added specialInfo field

    public User(int id, String userName, String password, String email, String specialInfo) {
        this.id = id;
        this.userName = userName;
        this.password = password;
        this.email = email;
        this.specialInfo = specialInfo;
    }

    public User() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSpecialInfo() {
        return specialInfo;
    }

    public void setSpecialInfo(String specialInfo) {
        this.specialInfo = specialInfo;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", userName='" + userName + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", specialInfo='" + specialInfo + '\'' +
                '}';
    }
}

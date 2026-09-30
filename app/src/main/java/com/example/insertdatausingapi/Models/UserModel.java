package com.example.insertdatausingapi.Models;

public class UserModel {

    String id, name, gender, age, profileImage;

    public UserModel() {
    }

    public UserModel(String id, String name, String gender, String age, String profileImage) {
        this.id = id;
        this.name = name;
        this.gender = gender;
        this.age = age;
        this.profileImage = profileImage;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
}

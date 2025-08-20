package com.prathameshAIcourceBuilder.AiCourceBuild.dto;

public class UserDTO {
    private Long id;
    private String email;
    private String name;
    private String profilePicture;
    private String role;

    public UserDTO() {}

    public UserDTO(Long id, String email, String name, String profilePicture, String role) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.profilePicture = profilePicture;
        this.role = role;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getProfilePicture() { return profilePicture; }
    public void setProfilePicture(String profilePicture) { this.profilePicture = profilePicture; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
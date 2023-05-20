package io.jeremy.account.model;

import io.jeremy.account.dto.requests.UserRequest;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String password;
    private String name;
    private String lastname;

    @ManyToMany(fetch = FetchType.EAGER, cascade = {
            CascadeType.PERSIST,
            CascadeType.MERGE
    })
    @JoinTable(name = "user_groups",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "group_id"))
    private Set<Group> userGroups = new HashSet<>();

    @Column(name = "account_is_locked", columnDefinition = "boolean default false")
    private boolean isLocked;

    @Column(name = "failed_attempt", columnDefinition = "integer default 0")
    private int failedAttempt;

    public User() {
    }

    public User(UserRequest userRequest, String password) {
        this.password = password;
        this.name = userRequest.getName();
        this.lastname = userRequest.getLastname();
        this.username = userRequest.getEmail();
    }

    public void addGroup(Group group) {
        userGroups.add(group);
        group.getUsers().add(this);
    }

    public void removeGroup(Group group) {
        userGroups.remove(group);
        group.getUsers().remove(this);
    }

    public boolean hasGroup(String groupName) {
        return userGroups.stream()
                .anyMatch(group -> group.getName().equals(groupName));
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public Set<Group> getUserGroups() {
        return userGroups;
    }

    public void setUserGroups(Set<Group> userGroups) {
        this.userGroups = userGroups;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }

    public int getFailedAttempt() {
        return failedAttempt;
    }

    public void setFailedAttempt(int failedAttempt) {
        this.failedAttempt = failedAttempt;
    }

    @Override
    public String toString() {
        return "User{"
                + "id=" + id
                + ", username='" + username + '\''
                + ", password='" + password + '\''
                + ", name='" + name + '\''
                + ", lastname='" + lastname + '\''
                + ", isLocked=" + isLocked
                + ", failedAttempt=" + failedAttempt
                + '}';
    }
}

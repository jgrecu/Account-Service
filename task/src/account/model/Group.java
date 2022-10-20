package account.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "groups")
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @ManyToMany(mappedBy = "userGroups")
    private Set<User> users;

    @JsonIgnore
    private boolean isBusiness;

    @JsonIgnore
    private boolean isAdministrative;

    public Group(String name) {
        this.name = name;
        users = new HashSet<>();

        if (name.equals("ROLE_ADMINISTRATOR")) {
            isBusiness = false;
            isAdministrative = true;
        }

        if (name.equals("ROLE_USER") || name.equals("ROLE_ACCOUNTANT") || name.equals("ROLE_AUDITOR")) {
            isBusiness = true;
            isAdministrative = false;
        }
    }

    public Group() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }

    public boolean isBusiness() {
        return isBusiness;
    }

    public void setBusiness(boolean business) {
        isBusiness = business;
    }

    public boolean isAdministrative() {
        return isAdministrative;
    }

    public void setAdministrative(boolean administrative) {
        isAdministrative = administrative;
    }
}

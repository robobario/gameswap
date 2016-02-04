package org.gameswap.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Objects;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(name = "users")
@NamedQueries({
        @NamedQuery(name = "User.findAll", query = "SELECT u FROM User u"),
        @NamedQuery(name = "User.findByName", query = "SELECT u FROM User u WHERE u.username = :username"),
        @NamedQuery(name = "User.findByGoogle", query = "SELECT u FROM User u WHERE u.google = :google")})
public class User {

    @Id
    @GeneratedValue
    private long id;

    @Column(name = "username", length = 18)
    @Size(min = 6)
    private String username;

    @Column(name = "password")
    @Size(min = 6)
    private String password;

    @Column(name = "email")
    private String email;


    @Column(name = "display_name")
    private String displayName;


    @Column(name = "google")
    private String google;



    public long getId() {
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


    public String getEmail() {
        return email;
    }


    public void setEmail(String email) {
        this.email = email;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User that = (User) o;

        return Objects.equal(this.id, that.id) && Objects.equal(this.username, that.username) && Objects.equal(this.password, that.password)
        && Objects.equal(this.email, that.email)&& Objects.equal(this.google, that.google)&& Objects.equal(this.displayName, that.displayName);
    }


    @Override
    public int hashCode() {
        return Objects.hashCode(id, username, password, email, google, displayName);
    }

    @JsonIgnore
    public int getSignInMethodCount() throws IllegalArgumentException, IllegalAccessException,
            NoSuchFieldException, SecurityException {
        int count = 0;

        if (this.getPassword() != null) {
            count++;
        }

        for (final Provider p : Provider.values()) {
            if (this.getClass().getDeclaredField(p.name).get(this) != null) {
                count++;
            }
        }

        return count;
    }

    public void setProviderId(Provider provider, String id) {
        switch (provider) {
            case GOOGLE:
                this.google = id;
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }


    public enum Provider {
        GOOGLE("google");

        String name;

        Provider(final String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }

        public String capitalize() {
            return StringUtils.capitalize(this.name);
        }
    }
}

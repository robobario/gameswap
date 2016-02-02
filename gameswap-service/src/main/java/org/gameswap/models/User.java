package org.gameswap.models;

import com.google.common.base.Objects;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue
    private long id;

    @NotNull
    @Column(name = "username", length = 18)
    @Size(min = 6, max = 18)
    private String username;

    @NotNull
    @Column(name = "password")
    @Size(min = 6)
    private String password;

    @Column(name = "email")
    private String email;


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
        && Objects.equal(this.email, that.email);
    }


    @Override
    public int hashCode() {
        return Objects.hashCode(id, username, password, email);
    }
}

package com.upgrad.FoodOrderingApp.service.entity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(name = "customer")
@NamedQueries(
        {
                @NamedQuery(name = "userByUuid", query = "select u from CustomerEntity u where u.uuid = :uuid"),
                @NamedQuery(name = "userByEmail", query = "select u from CustomerEntity u where u.email =:email"),
                @NamedQuery(name = "userByContact", query = "select u from CustomerEntity u where u.contactNumber =:contactNumber")
        }
)
public class CustomerEntity {
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "UUID")
    @Size(max = 200)
    private String uuid;

    @Column(name = "FIRSTNAME")
    @NotNull
    @Size(max = 30)
    private String firstName;

    @Column(name = "LASTNAME")
    @NotNull
    @Size(max = 30)
    private String lastName;

    @Column(name = "EMAIL")
    @NotNull
    @Size(max = 50)
    private String email;

    //@ToStringExclude
    @Column(name = "PASSWORD")
    @NotNull
    @Size(max = 255)
    private String password;

    @Column(name = "SALT")
    @NotNull
    @Size(max = 200)
    private String salt;

    @Column(name = "CONTACTNUMBER")
    @NotNull
    @Size(max = 30)
    private String contactNumber;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }


}

package com.example.demo.entities.concretes;

import com.example.demo.entities.abstracts.BaseEntity;
import com.example.demo.enums.UserType;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Users")
public class Users extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer id;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "personnel_name")
    private String personnelName;

    @Column(name = "personnel_surname")
    private String personnelSurname;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type")
    private UserType userType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id")
    private Vendor vendor;

}
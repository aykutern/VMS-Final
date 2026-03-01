package com.example.demo.entities.concretes;

import com.example.demo.entities.abstracts.BaseEntity;
import com.example.demo.enums.PersonnelTitle;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ProductManager")
public class ProductManager extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pm_id")
    private Integer id;

    @Column(name = "pm_name")
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "title")
    private PersonnelTitle title;



}

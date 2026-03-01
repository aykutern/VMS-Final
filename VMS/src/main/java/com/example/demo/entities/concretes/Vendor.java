package com.example.demo.entities.concretes;

import com.example.demo.entities.abstracts.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Table(name = "Vendor")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Vendor extends BaseEntity {

    @Id
    @Column(name = "vendor_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "vendor_name")
    private String vendorName;

}
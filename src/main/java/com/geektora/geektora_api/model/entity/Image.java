package com.geektora.geektora_api.model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "image")
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idImage;

    @Column(name = "url", nullable = false)
    private String url;

    @Column(name = "delete_hash", nullable = false)
    private String deleteHash;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "laststate",nullable = false)
    private boolean laststate;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "idProduct")
    private Product product;

}
package com.geektora.geektora_api.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "client")
@Data()
public class Client extends User{

    @ManyToOne
    @JoinColumn(name = "idProfilePhoto",nullable = false, referencedColumnName = "idProfilePhoto")
    private ProfilePhoto profilePhoto;
}

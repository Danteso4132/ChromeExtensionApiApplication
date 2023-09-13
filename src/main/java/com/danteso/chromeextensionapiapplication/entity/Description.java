package com.danteso.chromeextensionapiapplication.entity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Cascade;

import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "descriptions")
@EqualsAndHashCode
@Getter
@Setter
@ToString
public class Description {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "description_id")
    private UUID descriptionId;

    @Column(name = "description")
    private String description;


}

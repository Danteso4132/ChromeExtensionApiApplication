package com.danteso.chromeextensionapiapplication.entity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Cascade;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "terms")
@EqualsAndHashCode
@ToString
@Setter
@Getter
public class Term {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "term_id")
    private UUID id;

    @Column(name = "name")
    private String name;

    @OneToMany
    @Cascade(org.hibernate.annotations.CascadeType.ALL)
    private Set<Description> descriptions;

    @OneToOne(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Score score;



}

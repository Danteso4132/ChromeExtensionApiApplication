package com.danteso.chromeextensionapiapplication.entity;

import com.danteso.chromeextensionapiapplication.security.entity.User;
import jakarta.persistence.*;
import lombok.*;
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

    public Term(){
    }

    public Term(String name, String description){
        this.name = name;
        this.descriptions = Set.of(new Description(description));
    }

    public Term(String id, String name, Set<Description> descriptions, Score score) {
        this.id = UUID.fromString(id);
        this.name = name;
        this.descriptions = descriptions;
        this.score = score;
    }

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

    @OneToOne(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private User user;
}

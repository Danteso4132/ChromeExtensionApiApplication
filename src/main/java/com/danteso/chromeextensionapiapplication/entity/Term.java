package com.danteso.chromeextensionapiapplication.entity;

import com.danteso.chromeextensionapiapplication.security.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Cascade;

import java.util.*;

@Entity
@Table(name = "terms")
@EqualsAndHashCode
@ToString
@Setter
@Getter
@Builder
@AllArgsConstructor
public class Term {

    public Term(){
    }

    public Term(String name, String description){
        this.name = name;
        this.descriptions = Set.of(new Description(description));
    }

    public Term(String id, String name, Set<Description> descriptions, Score score, User user) {
        this.id = UUID.fromString(id);
        this.name = name;
        this.descriptions = descriptions;
        this.scoreForUser = new HashMap<>(Map.of(user, score));
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "term_id")
    private UUID id;

    @Column(name = "name", unique = true)
    private String name;

    @OneToMany(fetch = FetchType.EAGER)
    @Cascade(org.hibernate.annotations.CascadeType.ALL)
    private Set<Description> descriptions;

    @OneToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Map<User, Score> scoreForUser;

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE})
    private List<User> user;

    public Score getScoreForUser(User user) {
        return scoreForUser.get(user);
    }
}

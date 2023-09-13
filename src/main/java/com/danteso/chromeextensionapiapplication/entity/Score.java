package com.danteso.chromeextensionapiapplication.entity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.UUID;

@Entity
@Table(name = "scores")
@EqualsAndHashCode
@ToString
@Getter
@Setter
public class Score {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Term term;

    @PrePersist
    private void prePersist(){
        if (correct == null){
            correct = 0;
        }
        if (errors == null){
            errors = 0;
        }
    }

    @Column(name = "correct")
    private Integer correct = 0;

    @Column(name = "errors")
    private Integer errors = 0;


}

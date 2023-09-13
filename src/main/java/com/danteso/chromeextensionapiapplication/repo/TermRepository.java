package com.danteso.chromeextensionapiapplication.repo;

import com.danteso.chromeextensionapiapplication.entity.Term;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TermRepository extends JpaRepository<Term, UUID> {


    Term findByName(String name);
}

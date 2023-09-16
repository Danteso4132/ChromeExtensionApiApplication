package com.danteso.chromeextensionapiapplication.repo;

import com.danteso.chromeextensionapiapplication.entity.Score;
import com.danteso.chromeextensionapiapplication.entity.Term;
import com.danteso.chromeextensionapiapplication.security.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TermRepository extends JpaRepository<Term, UUID> {


    Term findByName(String name);



    List<Term> findByScore_CorrectLessThan(Integer correct);

    List<Term> findByScore_CorrectIsLessThanEqualAndUser(Integer correct, User user);


}

package com.danteso.chromeextensionapiapplication.repo;


import com.danteso.chromeextensionapiapplication.entity.Score;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ScoresRepository extends JpaRepository<Score, UUID> {

}

package com.danteso.chromeextensionapiapplication.game;

import com.danteso.chromeextensionapiapplication.entity.Description;
import com.danteso.chromeextensionapiapplication.entity.Term;
import com.danteso.chromeextensionapiapplication.repo.TermRepository;
import com.danteso.chromeextensionapiapplication.security.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class GameEngine {

    static final Integer SCORE_BOUND = 3;
    static final Integer TERMS_IN_ONE_GAME = 4;

    @Autowired
    TermRepository termRepository;

    public List<Term> getRandomTerms(User user){
        //List<Term> byScore_correctLessThan = termRepository.findByScore_CorrectLessThan(SCORE_BOUND);
        List<Term> byScore_correctLessThan = termRepository.findByScore_CorrectIsLessThanEqualAndUser(SCORE_BOUND, user);
        System.out.println("Found terms for User " + user);
        System.out.println("Terms: " + byScore_correctLessThan);
        Collections.shuffle(byScore_correctLessThan);
        List<Term> randomTerms = new ArrayList<>();
        for (int i = 0; i < TERMS_IN_ONE_GAME && i < byScore_correctLessThan.size(); i++){
            randomTerms.add(byScore_correctLessThan.get(i));
        }
        for (int i = randomTerms.size(); i < TERMS_IN_ONE_GAME; i++){
            randomTerms.add(new Term("Empty term", "No description"));
        }
        return randomTerms;
    }

    public Map<Term, List<Description>> getTermWithRandomDescriptions(User user){
        List<Term> randomTerms = getRandomTerms(user);
        Map<Term, List<Description>> termWithRandomDescriptions = new HashMap<>();
        List<Description> randomDescriptionsFromTerms = new ArrayList<>();
        for (Term randomTerm : randomTerms) {
            //randomDescriptionsFromTerms.add(randomTerm.getDescriptions().iterator().next());
            Set<Description> descriptions = randomTerm.getDescriptions();
            Description description = descriptions.stream().skip(new Random().nextInt(descriptions.size())).findFirst().orElse(null);
            randomDescriptionsFromTerms.add(description);
        }
        Collections.shuffle(randomDescriptionsFromTerms);
        termWithRandomDescriptions.put(randomTerms.get(0), randomDescriptionsFromTerms);
        return termWithRandomDescriptions;
    }

    public Boolean verifyAnswer(String name, String textDescription){
        Term termByName = termRepository.findByName(name);
        if (termByName == null){
            return false;
        }
        Set<Description> descriptions = termByName.getDescriptions();
        Set<Description> collect = descriptions.stream().filter(t -> t.getDescription().equals(textDescription)).collect(Collectors.toSet());
        return collect.size() > 0;
    }

    private void changeScoreForTerm(String termName, int value){
        Term termByName = termRepository.findByName(termName);
        if (termByName == null){
            return;
        }
        if (value > 0){
            termByName.getScore().setCorrect(termByName.getScore().getCorrect() + 1);
            termRepository.save(termByName);
        }
        else if (value < 0){
            termByName.getScore().setErrors(termByName.getScore().getErrors() + 1);
            termRepository.save(termByName);
        }
    }

    public void incrementScoreForTerm(String term){
        changeScoreForTerm(term, 1);
    }

    public void decrementScoreForTerm(String term){
        changeScoreForTerm(term, -1);
    }
}

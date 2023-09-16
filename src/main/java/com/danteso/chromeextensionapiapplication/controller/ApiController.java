package com.danteso.chromeextensionapiapplication.controller;

import com.danteso.chromeextensionapiapplication.entity.Description;
import com.danteso.chromeextensionapiapplication.entity.Score;
import com.danteso.chromeextensionapiapplication.entity.Term;
import com.danteso.chromeextensionapiapplication.game.GameEngine;
import com.danteso.chromeextensionapiapplication.parser.CambridgeParser;
import com.danteso.chromeextensionapiapplication.repo.TermRepository;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/api/")
public class ApiController {

    private final Logger logger = LoggerFactory.getLogger(ApiController.class);

    @Autowired
    CambridgeParser cambridgeParser;

    @Autowired
    TermRepository termRepository;

    @Autowired
    GameEngine gameEngine;




    @PostMapping("/save")
    @ResponseBody
    @CrossOrigin
    public String saveFromRequest(@RequestBody String request){
        System.out.println(request);
        String wordToDescription = request.replaceAll("[^a-zA-Z0-9]", "");
        Set<Description> descriptionFromUrl = saveFromGivenTermName(wordToDescription);
        Gson g = new Gson();
        return g.toJson(descriptionFromUrl);
    }

    private Set<Description> saveFromGivenTermName(String termName){
        Term fromRepo = termRepository.findByName(termName);
        if (fromRepo != null){
            return fromRepo.getDescriptions();
        }
        String url = "https://dictionary.cambridge.org/dictionary/english/"+ termName;
        System.out.println(url);
        Set<Description> descriptionFromUrl = cambridgeParser.getDescriptionFromUrl(url);
        Term t = new Term();
        if (descriptionFromUrl.size() > 0){
            t.setDescriptions(descriptionFromUrl);
            t.setName(termName);
            t.setScore(new Score());
            termRepository.save(t);
            Gson g = new Gson();
            return descriptionFromUrl;
        }
        else {
            return Set.of(new Description());
        }

    }

    @PostMapping("/saveForTermName")
    @CrossOrigin
    public String saveFromRequestedName(@ModelAttribute("word") String term){
        saveFromGivenTermName(term);
        return "redirect:/api/showAll";
    }

    @GetMapping("/showDescription")
    @ResponseBody
    public String getDefinition(@RequestParam(value = "word") String word){
        Term fromRepo = termRepository.findByName(word);
        if (fromRepo != null){
            return fromRepo.toString();
        }
        Set<Description> descriptionForTerm = cambridgeParser.getDescriptionForTerm(word);
        Term t = new Term();
        t.setDescriptions(descriptionForTerm);
        t.setName(word);
        t.setScore(new Score());
        termRepository.save(t);

        return descriptionForTerm.toString();
    }

    @GetMapping("/showAll")
    public String getAllTerms(Model model){
        model.addAttribute("terms", termRepository.findAll());
        return "showAll";
    }

    @GetMapping("/showScores")
    public String getAllScores(Model model){
        List<Term> allTerms = termRepository.findAll();
//        StringBuilder sb = new StringBuilder();
//        all.stream().forEach(t -> sb.append(t.getName() + " " + t.getScore().getCorrect() + " | " +  t.getScore().getErrors()));
//        return sb.toString();
        //List<Score> scores = allTerms.stream().map(t -> t.getScore()).collect(Collectors.toList());
        Map<String, Score> scores = allTerms.stream().
                collect(Collectors.toMap(Term::getName, Term::getScore));
        System.out.println(scores);
        model.addAttribute("scoresMap", scores);

        return "scores";
    }

    @GetMapping("/random")
    public String getRandomTerm(Model model){
        System.out.println("Playing game");
        Map<Term, List<Description>> randomTerms = gameEngine.getTermWithRandomDescriptions();
        Term term = randomTerms.keySet().iterator().next();
        List<Description> descriptions = randomTerms.get(term);
        model.addAttribute("term", term);
        model.addAttribute("descriptions", descriptions);
        return "game";
    }

    @PostMapping("/verifyAnswer")
    public String verifyAnswer(@ModelAttribute("d") Description desc
            , @ModelAttribute("t")String term
            , Model m){
        System.out.println("Got term: " + term);
        System.out.println("Description: " + desc.getDescription());
        Boolean verifyRes = gameEngine.verifyAnswer(term, desc.getDescription());
        if (verifyRes) {
            gameEngine.incrementScoreForTerm(term);
        }
        else{
            gameEngine.decrementScoreForTerm(term);
        }

        return "redirect:/api/random";
    }
}

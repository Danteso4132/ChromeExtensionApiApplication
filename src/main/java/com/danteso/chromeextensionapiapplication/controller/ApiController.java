package com.danteso.chromeextensionapiapplication.controller;

import com.danteso.chromeextensionapiapplication.entity.Description;
import com.danteso.chromeextensionapiapplication.entity.Score;
import com.danteso.chromeextensionapiapplication.entity.Term;
import com.danteso.chromeextensionapiapplication.parser.CambridgeParser;
import com.danteso.chromeextensionapiapplication.repo.TermRepository;
import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
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





    @PostMapping("/save")
    @ResponseBody
    @CrossOrigin
    public String postSelection(@RequestBody String request){

        System.out.println(request);
        String wordToDescription = request.replaceAll("[^a-zA-Z0-9]", "");
        Term fromRepo = termRepository.findByName(wordToDescription);
        if (fromRepo != null){
            return fromRepo.toString();
        }
        String url = "https://dictionary.cambridge.org/dictionary/english/"+ wordToDescription;
        System.out.println(url);
        Set<Description> descriptionFromUrl = cambridgeParser.getDescriptionFromUrl(url);
        Term t = new Term();
        t.setDescriptions(descriptionFromUrl);
        t.setName(wordToDescription);
        t.setScore(new Score());
        termRepository.save(t);
        Gson g = new Gson();
        return g.toJson(descriptionFromUrl);
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
}

package com.danteso.chromeextensionapiapplication.controller;

import com.danteso.chromeextensionapiapplication.entity.Description;
import com.danteso.chromeextensionapiapplication.entity.Score;
import com.danteso.chromeextensionapiapplication.entity.Term;
import com.danteso.chromeextensionapiapplication.game.GameEngine;
import com.danteso.chromeextensionapiapplication.parser.CambridgeParser;
import com.danteso.chromeextensionapiapplication.repo.TermRepository;
import com.danteso.chromeextensionapiapplication.security.entity.User;
import com.danteso.chromeextensionapiapplication.security.repo.UserRepository;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/api/")
public class ApiController {

    private final Logger LOG = LoggerFactory.getLogger(ApiController.class);

    @Autowired
    CambridgeParser cambridgeParser;

    @Autowired
    TermRepository termRepository;

    @Autowired
    GameEngine gameEngine;

    @Autowired
    UserRepository userRepo;




    @PostMapping("/save")
    @ResponseBody
    @CrossOrigin
    public String saveFromRequest(@RequestBody String request, Principal principal){
        LOG.debug(request);
        User user = userRepo.findByUsername(principal.getName());
        String wordToDescription = request.replaceAll("[^a-zA-Z0-9]", "");
        Set<Description> descriptionFromUrl = saveFromGivenTermName(wordToDescription, user);
        Gson g = new Gson();
        return g.toJson(descriptionFromUrl);
    }

    private Set<Description> saveFromGivenTermName(String termName, User user){
        Term fromRepo = termRepository.findByName(termName);
        if (fromRepo != null){
            if (!fromRepo.getUser().contains(user)){
                LOG.debug("got term from repo = {}", fromRepo);
                fromRepo.getUser().add(user);
                fromRepo.getScoreForUser().put(user, new Score());
                termRepository.save(fromRepo);
            }
            return fromRepo.getDescriptions();
        }
        String url = "https://dictionary.cambridge.org/dictionary/english/"+ termName;
        System.out.println(url);
        Set<Description> descriptionFromUrl = cambridgeParser.getDescriptionFromUrl(url);
        Term t = new Term();
        if (descriptionFromUrl.size() > 0){
            t.setDescriptions(descriptionFromUrl);
            t.setName(termName);
            t.setScoreForUser(new HashMap<User, Score>(Map.of(user, new Score())));
            if (t.getUser() != null){
                t.getUser().add(user);
            }
            else {
                t.setUser(new ArrayList<>(List.of(user)));
            }
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
    public String saveFromRequestedName(@ModelAttribute("word") String term, Principal principal){
        User user = userRepo.findByUsername(principal.getName());
        saveFromGivenTermName(term, user);
        return "redirect:/api/myTerms";
    }

    @GetMapping("/showDescription")
    @ResponseBody
    public String getDefinition(@RequestParam(value = "word") String word, Principal principal){
        User user = userRepo.findByUsername(principal.getName());
        Term fromRepo = termRepository.findByName(word);
        if (fromRepo != null){
            return fromRepo.toString();
        }
        Set<Description> descriptionForTerm = cambridgeParser.getDescriptionForTerm(word);
        Term t = new Term();
        t.setDescriptions(descriptionForTerm);
        t.setName(word);
        t.setScoreForUser(new HashMap<User, Score>(Map.of(user, new Score())));
        if (t.getUser() != null){
            t.getUser().add(user);
        }
        else {
            t.setUser(new ArrayList<>(List.of(user)));
        }
        termRepository.save(t);

        return descriptionForTerm.toString();
    }

    @GetMapping("/showAll")
    public String getAllTerms(Model model){
        model.addAttribute("terms", termRepository.findAll());
        return "showAll";
    }

    @GetMapping("/myTerms")
    public String getMyTerms(Model model, Principal principal){
        User user = userRepo.findByUsername(principal.getName());
        model.addAttribute("terms", termRepository.findByUser(user));
        return "showAll";
    }

    @GetMapping("/showScores")
    public String getAllScores(Model model, Principal principal){
        User user = userRepo.findByUsername(principal.getName());
        //List<Term> allTerms = termRepository.findAll();
        List<Term> allTerms = termRepository.findByUser(user);
//        Map<String, Score> scores = allTerms.stream().
//                collect(Collectors.toMap(Term::getName, Term::getScore));
//        System.out.println(scores);
        Map<String, Score> scores = new HashMap<>();
        for (Term allTerm : allTerms) {
            scores.put(allTerm.getName(), allTerm.getScoreForUser(user));
        }
        LOG.debug("scores for user = {}", scores);

        model.addAttribute("scoresMap", scores);

        return "scores";
    }

    @GetMapping("/random")
    public String getRandomTerm(Model model, Principal principal){
        LOG.debug("User " + principal);
        User user = userRepo.findByUsername(principal.getName());
        Map<Term, List<Description>> randomTerms = gameEngine.getTermWithRandomDescriptions(user);
        Term term = randomTerms.keySet().iterator().next();
        List<Description> descriptions = randomTerms.get(term);
        model.addAttribute("term", term);
        model.addAttribute("descriptions", descriptions);
        return "game";
    }

    @PostMapping("/verifyAnswer")
    public String verifyAnswer(@ModelAttribute("d") Description desc
            , @ModelAttribute("t")String term
            , Model m
            , Principal principal){
        User user = userRepo.findByUsername(principal.getName());
        LOG.debug("Got term: " + term);
        LOG.debug("Description: " + desc.getDescription());
        Boolean verifyRes = gameEngine.verifyAnswer(term, desc.getDescription());
        if (verifyRes) {
            gameEngine.incrementScoreForTerm(term, user);
        }
        else{
            gameEngine.decrementScoreForTerm(term, user);
        }

        return "redirect:/api/random";
    }

    @GetMapping("/telegram/token")
    public String showTelegramToken(Model model, Principal principal){
        User user = userRepo.findByUsername(principal.getName());
        model.addAttribute("token", user.getId());
        return "showTelegramToken";
    }
//
//    @GetMapping("/error")
//    public String errorGet() {
//        LOG.debug("Getting /error");
//        return "redirect:/api/showAll";
//    }
//    @PostMapping("/error")
//    public String errorPost(){
//        LOG.debug("Posting /error");
//        return "redirect:/api/showAll";
//    }

}

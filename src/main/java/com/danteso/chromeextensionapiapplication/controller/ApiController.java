package com.danteso.chromeextensionapiapplication.controller;

import com.danteso.chromeextensionapiapplication.parser.CambridgeParser;
import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class ApiController {

    private final Logger logger = LoggerFactory.getLogger(ApiController.class);

    @Autowired
    CambridgeParser cambridgeParser;






    @PostMapping("/api/save")
    @ResponseBody
    @CrossOrigin
    public String postSelection(@RequestBody String request){
        System.out.println(request);
        String url = "https://dictionary.cambridge.org/dictionary/english/"+request.replaceAll("[^a-zA-Z0-9]", "");
        System.out.println(url);
        String descriptionFromUrl = cambridgeParser.getDescriptionFromUrl(url);
        Gson g = new Gson();
        return g.toJson(descriptionFromUrl);
    }

    @GetMapping("/api/showDescription")
    @ResponseBody
    public String getDefinition(@RequestParam(value = "word") String word){
        return cambridgeParser.getDescriptionForTerm(word);
    }
}

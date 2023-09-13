package com.danteso.chromeextensionapiapplication.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CambridgeParser implements Parser{
    @Override
    public String getDescriptionFromUrl(String url) {
        try {
            String des = "";
            Document document = Jsoup.connect(url).get();
            System.out.println("Document has text: " + document.hasText());
            Elements foundObjects = document.getElementsByClass("def ddef_d db");
            for (Element foundObject : foundObjects) {
                des += foundObject.text() + "\n";
            }
            System.out.println("Returning " + des);
            if (des.length() == 0){
                des = "No definition for term!";
            }
            return des;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getDescriptionForTerm(String term){
        String url = "https://dictionary.cambridge.org/dictionary/english/" + term;
        return getDescriptionFromUrl(url);
    }
}


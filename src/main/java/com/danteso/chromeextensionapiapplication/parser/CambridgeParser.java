package com.danteso.chromeextensionapiapplication.parser;

import com.danteso.chromeextensionapiapplication.entity.Description;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.format.DecimalStyle;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class CambridgeParser implements Parser{
    @Override
    public Set<Description> getDescriptionFromUrl(String url) {
        try {
            Set<Description> des = new HashSet<>();
            Document document = Jsoup.connect(url).get();
            System.out.println("Document has text: " + document.hasText());
            Elements foundObjects = document.getElementsByClass("def ddef_d db");
            for (Element foundObject : foundObjects) {
                Description d = new Description();
                d.setDescription(foundObject.text());
                des.add(d);
            }
            System.out.println("Returning " + des);
            if (des.size() == 0){
                //Description d = new Description();
                //d.setDescription("No definition for term!");
                //des.add(d);
            }
            return des;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Set<Description> getDescriptionForTerm(String term){
        String url = "https://dictionary.cambridge.org/dictionary/english/" + term;
        return getDescriptionFromUrl(url);
    }
}


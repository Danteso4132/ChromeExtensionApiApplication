package com.danteso.chromeextensionapiapplication.parser;

import com.danteso.chromeextensionapiapplication.entity.Description;

import java.util.List;
import java.util.Set;

public interface Parser {

    public Set<Description> getDescriptionFromUrl(String url);

    public Set<Description> getDescriptionForTerm(String term);

}

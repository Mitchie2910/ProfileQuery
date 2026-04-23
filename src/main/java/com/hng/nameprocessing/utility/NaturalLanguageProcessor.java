package com.hng.nameprocessing.utility;

import com.hng.nameprocessing.dtos.QueryParameters;
import com.hng.nameprocessing.exceptions.ServiceValidationException;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class NaturalLanguageProcessor {

    private Rules rules;

    public NaturalLanguageProcessor(Rules rules) {
        this.rules = rules;
    }

    public QueryParameters processQuery(String query) {

        String lowerCaseQuery = query.toLowerCase();
        List<String> tokens = Arrays.asList(lowerCaseQuery.split("\\s+"));

        QueryParameters queryParameters = new QueryParameters();

        rules.extractGender(queryParameters, tokens);
        rules.extractAgeLimits(queryParameters, lowerCaseQuery, tokens);
        rules.extractAgeGroup(queryParameters, tokens);
        rules.extractCountryId(queryParameters, tokens);
        rules.extractMinGenderProbability(queryParameters, query);
        rules.extractMinCountryProbability(queryParameters, query);

        if(queryParameters.isNull()) {
            throw new ServiceValidationException("Unable to interpret query", 404);
        }

        return queryParameters;


    }
}

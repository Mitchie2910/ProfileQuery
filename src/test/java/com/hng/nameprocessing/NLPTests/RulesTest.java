package com.hng.nameprocessing.NLPTests;

import com.hng.nameprocessing.dtos.QueryParameters;
import com.hng.nameprocessing.exceptions.ServiceValidationException;
import com.hng.nameprocessing.utility.NaturalLanguageProcessor;
import com.hng.nameprocessing.utility.Rules;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
public class RulesTest {
    @Autowired
    NaturalLanguageProcessor naturalLanguageProcessor;

    @Test
    public void shouldReturnQueryParameters(){
        String query = "female and male teenager less than 15, more than 13, with gender confidence above 0.5 from angola and country confidence above 0.7";

        QueryParameters queryParameters = naturalLanguageProcessor.processQuery(query);

        assertThat(queryParameters).isNotNull();
        System.out.println(queryParameters);
    }

    @Test
    public void shouldThrowServiceValidationError(){
        String query = "cray way";

        assertThatThrownBy(() -> naturalLanguageProcessor.processQuery(query))
                .isInstanceOf(ServiceValidationException.class);
    }
}

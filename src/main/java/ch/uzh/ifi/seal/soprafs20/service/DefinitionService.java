package ch.uzh.ifi.seal.soprafs20.service;

import ch.uzh.ifi.seal.soprafs20.exceptions.SopraServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
@Transactional
public class DefinitionService {
    private final Logger log = LoggerFactory.getLogger(DefinitionService.class);
    private final RestTemplate restTemplate;

    public DefinitionService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    public String getDefinitionOfWord(String word) {
        return this.getEnglishDefinition(word);
    }

    private String getEnglishDefinition(String word) {
        String url = "https://od-api.oxforddictionaries.com/api/v2/entries/en-gb/" + word;

        // set Headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("app_id", "62bbc06c");
        headers.set("app_key", "aeeb82ba1944920c2d4f8b689330a293");

        // build request
        HttpEntity request = new HttpEntity(headers);

        ResponseEntity<String> response = this.restTemplate.exchange(url, HttpMethod.GET, request, String.class, 1);

        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            throw new SopraServiceException("Either the external API is offline or there are no more request for this user.");
        }
    }
}

package ch.uzh.ifi.seal.soprafs20.bots;

import ch.uzh.ifi.seal.soprafs20.constant.Language;
import ch.uzh.ifi.seal.soprafs20.entity.MysteryWord;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import javax.swing.text.html.HTMLDocument;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

public class CrazyBot implements Bot {
    private URL url;

    RestTemplate restTemplate = new RestTemplate();
    ObjectMapper mapper = new ObjectMapper();
    ResponseEntity<String> response;

    private HashMap<Language, String> urls = new HashMap<Language, String>(){{
        put(Language.EN, "https://random-word-api.herokuapp.com//word?number=");
        put(Language.DE, "http:german-api");
    }};


    public CrazyBot(Language language){
        try {
            this.url = new URL(urls.get(language));
        } catch (MalformedURLException e){
        }

    }

    @Override
    public String getClue(MysteryWord mysteryWord) {
        String crazyWord;
        try {
            response = restTemplate.getForEntity(url + "1", String.class);
        } catch(Exception e){
            return "Server not reachable";
        }
        try{
            JsonNode root = mapper.readTree(response.getBody());
            crazyWord = root.get(0).asText();
        } catch (Exception e){
            return "No Random Word found";
        }
        return crazyWord;
    }
}

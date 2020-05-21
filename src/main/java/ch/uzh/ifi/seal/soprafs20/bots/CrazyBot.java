package ch.uzh.ifi.seal.soprafs20.bots;

import ch.uzh.ifi.seal.soprafs20.constant.Language;
import ch.uzh.ifi.seal.soprafs20.entity.MysteryWord;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.EnumMap;

public class CrazyBot implements Bot {
    private URL url;

    RestTemplate restTemplate = new RestTemplate();
    ObjectMapper mapper = new ObjectMapper();
    ResponseEntity<String> response;

    private static final EnumMap<Language, String> URLS = new EnumMap<>(Language.class);
    static{
        URLS.put(Language.EN, "https://random-word-api.herokuapp.com//word?number=");
        URLS.put(Language.DE, "http:german-api");
    }


    public CrazyBot(Language language){
        try {
            this.url = new URL(URLS.get(language));
        } catch (MalformedURLException e){}

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

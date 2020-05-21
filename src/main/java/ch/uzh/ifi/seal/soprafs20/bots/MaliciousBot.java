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

public class MaliciousBot implements Bot{
    private URL url;

    RestTemplate restTemplate = new RestTemplate();
    ObjectMapper mapper = new ObjectMapper();
    ResponseEntity<String> response;

    private static final EnumMap<Language, String> URLS = new EnumMap<>(Language.class);

    static{
        URLS.put(Language.EN, "https://api.datamuse.com/words?rel_ant=");
        URLS.put(Language.DE, "http:german-api");
    }


    public MaliciousBot(Language language){
        try {
            this.url = new URL(URLS.get(language));
        } catch (MalformedURLException e){ }
    }



    @Override
    public String getClue(MysteryWord mysteryWord) {
        String antonym;
        try {
            response = restTemplate.getForEntity(url + mysteryWord.getWord(), String.class);
        } catch(Exception e){
            return "Server not reachable";
        }
        try{
            JsonNode root = mapper.readTree(response.getBody());
            JsonNode synonymNode = root.get(0);
            antonym = synonymNode.get("word").asText();
        } catch (Exception e){
            return "No Antonym found";
        }
        return antonym;
    }
}




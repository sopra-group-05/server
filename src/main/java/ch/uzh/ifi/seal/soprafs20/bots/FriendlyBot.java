package ch.uzh.ifi.seal.soprafs20.bots;

import ch.uzh.ifi.seal.soprafs20.constant.Language;
import ch.uzh.ifi.seal.soprafs20.entity.MysteryWord;
import ch.uzh.ifi.seal.soprafs20.exceptions.SopraServiceException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.EnumMap;
import java.util.HashMap;

public class FriendlyBot implements Bot {
    private URL url;
    private HashMap<String, String> cluesTable;

    RestTemplate restTemplate = new RestTemplate();
    ObjectMapper mapper = new ObjectMapper();
    ResponseEntity<String> response;


    private static final EnumMap<Language, String> URLS = new EnumMap<>(Language.class);

    static{
        URLS.put(Language.EN, "https://api.datamuse.com/words?ml=");
        URLS.put(Language.DE, "http:german-api");
    }


    public FriendlyBot(Language language){
        try {
            this.url = new URL(URLS.get(language));
        } catch (MalformedURLException e){}
        buildHasmap();
    }


    private String getClueFromAPI(MysteryWord mysteryWord) {
        String synonym;
        try {
            response = restTemplate.getForEntity(url + mysteryWord.getWord(), String.class);
        } catch(Exception e){
            return "Server not reachable";
        }
        try{
            JsonNode root = mapper.readTree(response.getBody());
            JsonNode synonymNode = root.get(0);
            synonym = synonymNode.get("word").asText();
        } catch (Exception e){
            return "No Synonym found";
        }
        return synonym;
    }

    @Override
    public String getClue(MysteryWord mysteryWord) {
        if(cluesTable.containsKey(mysteryWord.getWord()) && !cluesTable.get(mysteryWord.getWord()).equals("")) {
                return cluesTable.get(mysteryWord.getWord());
        } else {
            return getClueFromAPI(mysteryWord);
        }
    }

    private void buildHasmap(){
        String csvFile = "src/main/resources/FriendlyBotData.csv";
        String line = "";
        String cvsSplitBy = ",";
        cluesTable = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {

            while ((line = br.readLine()) != null) {

                // use comma as separator
                String[] clue = line.split(cvsSplitBy);
                //
                try{
                    cluesTable.put(clue[0], clue[1]);
                }catch (ArrayIndexOutOfBoundsException e){
                    cluesTable.put(clue[0], "");
                }


            }

        } catch (IOException e) {
            throw new SopraServiceException("Hashmap for FriendlyBot couldn't get generated");
        }
    }
}

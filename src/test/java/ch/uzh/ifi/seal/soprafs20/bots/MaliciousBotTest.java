package ch.uzh.ifi.seal.soprafs20.bots;

import ch.uzh.ifi.seal.soprafs20.constant.Language;
import ch.uzh.ifi.seal.soprafs20.entity.MysteryWord;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.MalformedURLException;
import java.net.URL;

public class MaliciousBotTest {
    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private MaliciousBot maliciousBot = new MaliciousBot(Language.EN);

    MysteryWord mysteryWord = new MysteryWord();
    String json;
    private URL url;

    @BeforeEach
    public void setup(){
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void botTestSuccessAPI() throws MalformedURLException {
        json = "[{\"word\":\"old\",\"score\":1973}]";
        mysteryWord.setWord("new");
        ResponseEntity<String> response = new ResponseEntity<String>(json, HttpStatus.OK);
        url = new URL("https://api.datamuse.com/words?rel_ant=");
        Mockito.when(restTemplate.getForEntity(url + "new", String.class)).thenReturn(response);
        String clue = maliciousBot.getClue(mysteryWord);
        Assertions.assertEquals("old", clue);
    }



    @Test
    public void botTestSuccessHashmap(){
        mysteryWord.setWord("Alcatraz");
        String clue = maliciousBot.getClue(mysteryWord);
        Assertions.assertEquals("Freedom", clue);
    }
}


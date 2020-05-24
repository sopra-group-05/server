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

public class CrazyBotTest {
    @Mock
    private RestTemplate restTemplate;
    @InjectMocks
    private CrazyBot crazyBot = new CrazyBot(Language.EN);
    private MysteryWord mysteryWord = new MysteryWord();
    private URL url;
    private String json;

    @BeforeEach
    public void setup(){
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void getRandomWord() throws MalformedURLException {
        url = new URL("https://random-word-api.herokuapp.com//word?number=");
        String urlAdd = "1";
        json = "[\"random\"]";
        ResponseEntity<String> response = new ResponseEntity<String>(json, HttpStatus.OK);
        Mockito.when(restTemplate.getForEntity(url + urlAdd, String.class)).thenReturn(response);
        mysteryWord.setWord("");
        String clue = crazyBot.getClue(mysteryWord);
        Assertions.assertEquals("random", clue);
    }

}

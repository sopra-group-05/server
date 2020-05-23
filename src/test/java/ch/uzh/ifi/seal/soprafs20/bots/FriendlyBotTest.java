package ch.uzh.ifi.seal.soprafs20.bots;

import ch.uzh.ifi.seal.soprafs20.constant.Language;
import ch.uzh.ifi.seal.soprafs20.entity.MysteryWord;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URL;

public class FriendlyBotTest {
    @Mock
    private RestTemplate restTemplate;
/*
    @InjectMocks
    private Bot friendlyBot = new FriendlyBot(Language.EN);
*

 */
    MysteryWord mysteryWord = new MysteryWord();
    String json;

    @BeforeEach
    public void setup(){
        restTemplate = new RestTemplate();
    }

    /*todo fix test
    @Test
    public void botTestSuccess() {
        json = "[{\"word\":\"exam\",\"score\":93919,\"tags\":[\"syn\",\"n\"]}";
        mysteryWord.setWord("test");
        ResponseEntity<String> response = new ResponseEntity<String>(json, HttpStatus.OK);
        Mockito.when(restTemplate.getForEntity("https://api.datamuse.com/words?ml=" + mysteryWord.getWord(), String.class)).thenReturn(response);
        String clue = friendlyBot.getClue(mysteryWord);
        Assertions.assertEquals(clue, "exam");
    }

*/

    @Test
    public void test(){
        FriendlyBot friendlyBot = new FriendlyBot(Language.EN);
    }
}

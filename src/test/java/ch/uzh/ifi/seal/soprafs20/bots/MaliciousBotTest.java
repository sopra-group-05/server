package ch.uzh.ifi.seal.soprafs20.bots;

import ch.uzh.ifi.seal.soprafs20.constant.Language;
import ch.uzh.ifi.seal.soprafs20.entity.MysteryWord;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MaliciousBotTest {
    Bot friendlyBot = new MalicousBot(Language.EN);
    MysteryWord mysteryWord = new MysteryWord();
    /*
    @Test
    public void botTest(){
        mysteryWord.setWord("early");
        String result = friendlyBot.getClue(mysteryWord);
        Assertions.assertEquals("late", result);
    }

     */
}


package ch.uzh.ifi.seal.soprafs20.bots;

import ch.uzh.ifi.seal.soprafs20.constant.Language;
import ch.uzh.ifi.seal.soprafs20.entity.MysteryWord;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FriendlyBotTest {
    Bot friendlyBot = new FriendlyBot(Language.EN);
    MysteryWord mysteryWord = new MysteryWord();
    /*
    @Test
    public void botTest(){
        mysteryWord.setWord("test");
        String result = friendlyBot.getClue(mysteryWord);
        Assertions.assertEquals("exam", result);
    }

     */
}

package ch.uzh.ifi.seal.soprafs20.bots;

import ch.uzh.ifi.seal.soprafs20.constant.Language;
import ch.uzh.ifi.seal.soprafs20.entity.MysteryWord;

public class MaliciousBotTest {
    Bot maliciousBot = new MaliciousBot(Language.EN);
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


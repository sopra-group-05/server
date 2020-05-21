package ch.uzh.ifi.seal.soprafs20.bots;

import ch.uzh.ifi.seal.soprafs20.constant.Language;
import ch.uzh.ifi.seal.soprafs20.entity.MysteryWord;

public class MaliciousBotTest {
    Bot friendlyBot = new MaliciousBot(Language.EN);
    MysteryWord mysteryWord = new MysteryWord();
    /*todo add tests that are not dependent on api
    @Test
    public void botTest(){
        mysteryWord.setWord("early");
        String result = friendlyBot.getClue(mysteryWord);
        Assertions.assertEquals("late", result);
    }

     */
}


package ch.uzh.ifi.seal.soprafs20.service;

import ch.uzh.ifi.seal.soprafs20.constant.Language;
import ch.uzh.ifi.seal.soprafs20.entity.Card;
import ch.uzh.ifi.seal.soprafs20.exceptions.ForbiddenException;
import ch.uzh.ifi.seal.soprafs20.repository.CardRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Card Service
 * This class is the "worker" and responsible for all functionality related to the Card
 * (e.g., it converts, modifies, deletes, finds). The result will be passed back to the caller.
 */
@Service
@Transactional
public class CardService {

    private final Logger log = LoggerFactory.getLogger(CardService.class);

    private final CardRepository cardRepository;

    private final MysteryWordService mysteryWordService;

    @Autowired
    public CardService(@Qualifier("cardRepository") CardRepository cardRepository, MysteryWordService mysteryWordService) {
        this.cardRepository = cardRepository;
        this.mysteryWordService = mysteryWordService;
    }

    /**
     * This method will get a specific Card by ID from the Card Repository
     *
     * @return The requested Card
     * @see Card
     */
    public Card getCardById(Long id)
    {
        Optional<Card> card = cardRepository.findById(id);
        return card.orElseThrow(()->new ForbiddenException("Card not found"));
    }

    /**
     * Removes the card from Card repository
     *
     * @param - the card to be removed from the Card repository
     * */
    public void delete(Card card) {
        if(card != null) {
            cardRepository.delete(card);
        }
    }

    /**
     * Saves the card using Card repository
     *
     * @param - the card to be updated to the Card repository
     * */
    public void save(Card card) {
        if(card != null) {
            cardRepository.save(card);
        }
    }

    /***
     *
     * Generates 13 cards for one deck
     *
     * @param language*/
    public List<Card> get13Cards(Language language) {
        List<Card> cardList = cardRepository.findByLanguage(language);
        //Random rand = new Random();
        //TODO set back to 13 cards
        final int maxCard = 7;
        SecureRandom rand = new SecureRandom();
        int streamSize = cardList.size() > maxCard ? maxCard : cardList.size();
        List<Card> wordList = rand.
                ints(streamSize, 0, cardList.size()).
                mapToObj(i -> cardList.get(i)).
                collect(Collectors.toList());

        return wordList;
    }

}

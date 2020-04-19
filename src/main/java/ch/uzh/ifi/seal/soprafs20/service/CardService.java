package ch.uzh.ifi.seal.soprafs20.service;

import ch.uzh.ifi.seal.soprafs20.entity.Card;
import ch.uzh.ifi.seal.soprafs20.exceptions.ForbiddenException;
import ch.uzh.ifi.seal.soprafs20.repository.CardRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

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

    @Autowired
    public CardService(@Qualifier("cardRepository") CardRepository cardRepository) {
        this.cardRepository = cardRepository;
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
     * Removes the cards from Card repository
     *
     * @param - the cards to be removed from the Card repository
     * */
    public void deleteAll(List<Card> cards) {
        if(cards != null && !cards.isEmpty()) {
            cardRepository.deleteAll(cards);
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

    /**
     * Saves the cards using Card repository
     *
     * @param - the cards to be updated to the Card repository
     * */
    public void saveAll(List<Card> cards) {
        if(cards != null && !cards.isEmpty()) {
            cardRepository.saveAll(cards);
        }
    }

}

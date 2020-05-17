package ch.uzh.ifi.seal.soprafs20.service;

import ch.uzh.ifi.seal.soprafs20.constant.Language;
import ch.uzh.ifi.seal.soprafs20.entity.Card;
import ch.uzh.ifi.seal.soprafs20.entity.Deck;
import ch.uzh.ifi.seal.soprafs20.exceptions.ForbiddenException;
import ch.uzh.ifi.seal.soprafs20.repository.DeckRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Deck Service
 * This class is the "worker" and responsible for all functionality related to the Deck
 * (e.g., it converts, modifies, deletes, finds). The result will be passed back to the caller.
 */
@Service
@Transactional
public class DeckService {

    private final Logger log = LoggerFactory.getLogger(DeckService.class);

    private final DeckRepository deckRepository;

    private final CardService cardService;

    @Autowired
    public DeckService(@Qualifier("deckRepository") DeckRepository deckRepository, CardService cardService) {
        this.deckRepository = deckRepository;
        this.cardService = cardService;
    }

    /**
     * This method will get a specific Deck by ID from the Deck Repository
     *
     * @return The requested Deck
     * @see Deck
     */
    public Deck getDeckById(Long id)
    {
        Optional<Deck> deck = deckRepository.findById(id);
        return deck.orElseThrow(()->new ForbiddenException("Deck not found"));
    }

    /**
     * Removes the deck from Deck repository
     *
     * @param - the deck to be removed from the Deck repository
     * */
    public void deleteDeck(Deck deck) {
        if(deck != null) {
            deckRepository.delete(deck);
        }
    }

    /**
     * Saves the deck using Deck repository
     *
     * @param - the deck to be updated to the Deck repository
     * */
    public void save(Deck deck) {
        if(deck != null) {
            deckRepository.save(deck);
        }
    }

    /**
     * Constructs cards and deck for a new game
     *
     * @param language*/
    public Deck constructDeckForLanguage(Language language, int numberOfCards) {
        Deck deck = new Deck();
        List<Card> cards = cardService.get13Cards(language, numberOfCards);
        deck.clearAndAddCards(cards);
        deck = deckRepository.save(deck);
        return deck;
    }

}

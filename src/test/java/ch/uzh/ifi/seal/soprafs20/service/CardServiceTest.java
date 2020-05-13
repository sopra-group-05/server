package ch.uzh.ifi.seal.soprafs20.service;

import ch.uzh.ifi.seal.soprafs20.constant.Language;
import ch.uzh.ifi.seal.soprafs20.entity.Card;
import ch.uzh.ifi.seal.soprafs20.exceptions.ForbiddenException;
import ch.uzh.ifi.seal.soprafs20.repository.CardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

public class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @InjectMocks
    private CardService cardService;

    private Card card;

    /**
     * Prepares mock data for each test
     */
    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);

        // given
        card = Card.getInstance();
        card.setId(1L);
        card.setDrawn(Boolean.FALSE);
        card.setLanguage(Language.DE);

        // when -> any object is being save in the cardRepository -> return the dummy testUser
        when(cardRepository.save(Mockito.any())).then(AdditionalAnswers.returnsFirstArg());
        when(cardRepository.saveAll(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0, List.class));
        Mockito.when(cardRepository.findById(card.getId())).thenReturn(Optional.of(card));
        doNothing().when(cardRepository).delete(Mockito.any());
        doNothing().when(cardRepository).deleteAll(Mockito.any());
    }

    /**
     * Checks save Card function with correct input
     */
    @Test
    public void getCard_validInputs_success() {
        // when -> any object is being save in the cardRepository -> return the dummy testUser
        Card cardLocal = cardService.getCardById(card.getId());

        assertEquals(card.getId(), cardLocal.getId());
        assertEquals(card.getLanguage(), cardLocal.getLanguage());
    }


    /**
     * Throws ForbiddenException for given invalid input
     */
    @Test
    public void getCardById_InvalidInput_throwsException() {
        // then -> attempt to create second user with same user -> check that an error is thrown
        String exceptionMessage = "Card not found";
        ForbiddenException exception = assertThrows(ForbiddenException.class, () -> cardService.getCardById(2L), exceptionMessage);
        assertEquals(exceptionMessage, exception.getMessage());
    }

    /**
     * saves new Word and gets the same
     */
    @Test
    public void saveAndGet_CardById_ValidInput() {

        Card card1 = getCard(2L);
        card1.setDrawn(Boolean.TRUE);

        Mockito.when(cardRepository.findById(card1.getId())).thenReturn(Optional.of(card1));

        Card dbCard = cardService.getCardById(2L);

        assertEquals(card1.getId(), dbCard.getId());
        assertEquals(card1.getLanguage(), dbCard.getLanguage());
    }

    /**
     * Checks save Card function with correct input
     */
    @Test
    public void get13Cards_validInputs_success() {

        List<Card> cardList = new ArrayList<>();
        cardList.add(card);
        cardList.add(getCard(3L));
        //TODO Revert after demo
        //cardList.add(getCard(4L));
        //cardList.add(getCard(5L));
        Mockito.when(cardRepository.findByLanguage(Language.EN)).thenReturn(cardList);

        // when -> any object is being save in the cardRepository -> return the dummy testUser
        List<Card> cardLocalList = cardService.get13Cards(Language.EN,3);

        assertEquals(cardList.size(), cardLocalList.size());
    }

    private Card getCard(long l) {
        Card newCard = Card.getInstance();
        newCard.setId(l);
        newCard.setLanguage(Language.EN);
        return newCard;
    }

}

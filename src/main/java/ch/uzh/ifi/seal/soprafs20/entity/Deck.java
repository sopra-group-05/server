package ch.uzh.ifi.seal.soprafs20.entity;

import ch.uzh.ifi.seal.soprafs20.exceptions.SopraServiceException;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "DECK")
public class Deck implements Serializable
{
    @Id
    @Column(nullable = false, unique = true)
    @GeneratedValue
    private Long deckId;

    @ElementCollection
    private List<Card> cards = new ArrayList<>();

    @OneToOne
    private Card activeCard;

    public Long getDeckId() {
        return deckId;
    }

    public void setDeckId(Long deckId) {
        this.deckId = deckId;
    }

    public List<Card> getCards() {
        return cards;
    }

    public void addCard(Card card) {
        if(getCards().size()<13){
            this.cards.add(card);
        } else{
            throw new SopraServiceException("the maximum number of cards has been reached for this deck");
        }

    }

    public Card getActiveCard() {
        return activeCard;
    }

    public void setActiveCard(Card activeCard) {
        this.activeCard = activeCard;
    }
}

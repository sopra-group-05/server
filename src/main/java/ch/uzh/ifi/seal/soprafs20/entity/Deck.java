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

    //@ElementCollection
    @ManyToMany(cascade = {CascadeType.ALL})
    @JoinTable(name="Deck_Cards",
            joinColumns={@JoinColumn(name="deckId")},
            inverseJoinColumns={@JoinColumn(name="cardId")})
    private List<Card> cards = new ArrayList<>();

    @ManyToOne
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

    public void clearAndAddCards(List<Card> cards) {
        if(cards.size() > 13) {
            throw new SopraServiceException("the maximum number of cards for a deck is only 13 cards");
        } else {
            this.cards.clear();
            this.cards.addAll(cards);
        }
    }

    public Card getActiveCard() {
        return activeCard;
    }

    public void setActiveCard(Card activeCard) {
        this.activeCard = activeCard;
    }
}

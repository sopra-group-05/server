package ch.uzh.ifi.seal.soprafs20.entity;

import org.springframework.stereotype.Component;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "DECK")
public class Deck implements Serializable
{
    @Id
    @Column(nullable = false, unique = true)
    @GeneratedValue
    private Long deckId;

    public Long getDeckId() {
        return deckId;
    }

    public void setDeckId(Long deckId) {
        this.deckId = deckId;
    }
}

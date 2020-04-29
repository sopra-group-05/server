package ch.uzh.ifi.seal.soprafs20.entity;

import ch.uzh.ifi.seal.soprafs20.exceptions.SopraServiceException;
import net.bytebuddy.implementation.bytecode.Throw;
import org.hibernate.annotations.Cascade;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

@Entity
@Table(name = "CARD")
public final class Card implements Serializable {
    @Id
    @Column(nullable = false, unique = true)
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private Boolean drawn;

    @ElementCollection
    private List<MysteryWord> mysteryWords = new ArrayList<>();

    Card(){
        //making it to be default constructor to be better controlled
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getDrawn() {
        return drawn;
    }

    public void setDrawn(Boolean drawn) {
        this.drawn = drawn;
    }

    public List<MysteryWord> getMysteryWords() {
        return Collections.unmodifiableList(mysteryWords);
    }

    public void addMysteryWord(MysteryWord mysteryWord) {
        if(getMysteryWords().size()<5){
            this.mysteryWords.add(mysteryWord);
        } else{
            throw new SopraServiceException("the maximum number of mysterywords has been reached for this card");
        }

    }

    public static Card getInstance(){
         Card card = new Card();
         return card;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Card card = (Card) o;
        return Objects.equals(getId(), card.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }


}

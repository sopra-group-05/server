package ch.uzh.ifi.seal.soprafs20.entity;

import ch.uzh.ifi.seal.soprafs20.constant.MysteryWordStatus;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "MYSTERYWORD")
public class MysteryWord implements Serializable {

    @Id
    @GeneratedValue
    @Column(nullable = false, unique = true)
    private Long id;

    @OneToOne
    private Card card;

    @Column(nullable = false)
    private String word;

    @Column(nullable = false)
    private MysteryWordStatus status;

    @Column(nullable = false)
    private Boolean guessedCorrectly;

    @Column(nullable = false)
    private Date timedrawn;

    @Column(nullable = false)
    private Date timeForDues;

    @Column(nullable = false)
    private String description;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Card getCard() {
        return card;
    }

    public void setCard(Card card) {
        this.card = card;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public MysteryWordStatus getStatus() {
        return status;
    }

    public void setStatus(MysteryWordStatus status) {
        this.status = status;
    }

    public Boolean getGuessedCorrectly() {
        return guessedCorrectly;
    }

    public void setGuessedCorrectly(Boolean guessedCorrectly) {
        this.guessedCorrectly = guessedCorrectly;
    }

    public Date getTimedrawn() {
        return timedrawn;
    }

    public void setTimedrawn(Date timedrawn) {
        this.timedrawn = timedrawn;
    }

    public Date getTimeForDues() {
        return timeForDues;
    }

    public void setTimeForDues(Date timeForDues) {
        this.timeForDues = timeForDues;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MysteryWord that = (MysteryWord) o;
        return getId().equals(that.getId()) &&
                getCard().equals(that.getCard());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getCard());
    }
}

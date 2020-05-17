package ch.uzh.ifi.seal.soprafs20.entity;

import ch.uzh.ifi.seal.soprafs20.constant.ClueStatus;
import ch.uzh.ifi.seal.soprafs20.service.ClueService;

import javax.persistence.*;

@Entity
@Table(name="CLUE")
public class Clue {

    @Id
    @GeneratedValue
    @Column
    private long id;

    @Column
    private ClueStatus clueStatus;

    @Column
    private String hint;
    
    @Column
    private String hint2;

    @ManyToOne
    private Player player;

    @Column
    private int flagCounter;

    @ManyToOne
    private Card card;

    @ManyToOne
    private Game game;
    
    @Column
    private Long timeForClue;

    public long getId(){
        return this.id;
    }

    public ClueStatus getClueStatus() {
        return clueStatus;
    }

    public String getHint(){
        return this.hint;
    }

    public Player getPlayer(){
        return this.player;
    }

    public void setClueStatus(ClueStatus clueStatus){
        this.clueStatus = clueStatus;
    }
    public void setHint(String hint){
        this.hint = hint;
    }
    
    /*public String getHint2() {
		return hint2;
	}

	public void setHint2(String hint2) {
		this.hint2 = hint2;
	}

     */

	public void setPlayer(Player player){
        this.player = player;
    }

    public void setFlagCounter(int i){this.flagCounter = i;}

    public int getFlagCounter(){return this.flagCounter;}

    public Card getCard() {
        return this.card;
    }

    public void setCard(Card card) {
        this.card = card;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

	public Long getTimeForClue() {
		return timeForClue;
	}

	public void setTimeForClue(Long timeForClue) {
		this.timeForClue = timeForClue;
	}
    
}

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

    @ManyToOne
    private Player player;

    @ManyToOne
    private Lobby lobby;

    @Column
    private int flagCounter;

    //TODO:connect to mysterywords

    /*
    @ManyToMany
    private Mysteryword mysteryword;
    */

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

    public Lobby getLobby(){
        return this.lobby;
    }

    public void setClueStatus(ClueStatus clueStatus){
        this.clueStatus = clueStatus;
    }
    public void setHint(String hint){
        this.hint = hint;
    }

    public void setPlayer(Player player){
        this.player = player;
    }

    public void setLobby(Lobby lobby){
        this.lobby = lobby;
    }

    public void setFlagCounter(int i){this.flagCounter = i;}

    public int getFlagCounter(){return this.flagCounter;}
}

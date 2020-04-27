package ch.uzh.ifi.seal.soprafs20.entity;

import ch.uzh.ifi.seal.soprafs20.exceptions.SopraServiceException;
import org.json.JSONObject;
import org.springframework.data.util.Pair;


import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "STATISTICS")
public class Statistics implements Serializable
{
    public Statistics(){
    }

    @Id
    @GeneratedValue
    @Column(nullable = false, unique = true)
    private Long id;

    @Column(nullable = false)
    int groupStats;

    @Column(nullable = false)
    ArrayList<Pair<String, Integer>> playerStats;

    public int getGroupStats() {
        return groupStats;
    }

    public void setGroupStats(int groupStats) {
        this.groupStats = groupStats;
    }

    public ArrayList<Pair<String, Integer>> getPlayerStats() {
        return playerStats;
    }

    public void setPlayerStats(ArrayList<Pair<String, Integer>> playerStats) {
        this.playerStats = playerStats;
    }
}

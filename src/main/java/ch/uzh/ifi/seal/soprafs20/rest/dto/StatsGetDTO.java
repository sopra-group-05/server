package ch.uzh.ifi.seal.soprafs20.rest.dto;

import org.springframework.data.util.Pair;

import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class StatsGetDTO {

    private int groupStats;
    private ArrayList<Pair<String, Integer>> playerStats;

    public int getGroupStats() {
        return groupStats;
    }

    public void setGroupStats(int groupStats) {
        this.groupStats = groupStats;
    }

    public void setPlayerStats(ArrayList<Pair<String, Integer>> playerStats) {
        this.playerStats = playerStats;
    }

    public ArrayList<Pair<String, Integer>> getPlayerStats() {
        return playerStats;
    }
}

package ch.uzh.ifi.seal.soprafs20.rest.dto;

import ch.uzh.ifi.seal.soprafs20.constant.MysteryWordStatus;
import java.util.Date;

public class MysteryWordGetDto {
    private Long id;

    private String word;

    private MysteryWordStatus status;

    private Date timedrawn;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Date getTimedrawn() {
        return timedrawn;
    }

    public void setTimedrawn(Date timedrawn) {
        this.timedrawn = timedrawn;
    }
}

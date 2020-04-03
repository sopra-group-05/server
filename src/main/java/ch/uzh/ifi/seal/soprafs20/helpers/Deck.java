package ch.uzh.ifi.seal.soprafs20.helpers;

import org.springframework.stereotype.Component;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

@Component
public class Deck implements Serializable
{
    private int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}

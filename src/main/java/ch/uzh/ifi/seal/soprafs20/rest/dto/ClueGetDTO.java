package ch.uzh.ifi.seal.soprafs20.rest.dto;

public class ClueGetDTO {
    private long id;
    private String hint;

    public void setHint(String hint) {
        this.hint = hint;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public String getHint() {
        return hint;
    }
}

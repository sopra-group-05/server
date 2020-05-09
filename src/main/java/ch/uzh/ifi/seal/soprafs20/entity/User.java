package ch.uzh.ifi.seal.soprafs20.entity;

import ch.uzh.ifi.seal.soprafs20.constant.UserStatus;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Internal User Representation
 * This class composes the internal representation of the user and defines how the user is stored in the database.
 * Every variable will be mapped into a database field with the @Column annotation
 * - nullable = false -> this cannot be left empty
 * - unique = true -> this value must be unqiue across the database -> composes the primary key
 */
@Entity
@Table(name = "USER")
public class User implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
    @GeneratedValue
    @Column(nullable = false, unique = true)
	private Long id;

    @Column(nullable = false)
    private LocalDateTime created = LocalDateTime.now();
	
	@Column(nullable = false, unique = true) 
	private String username;

	@Column(nullable = false)
    private String password;
	
	@Column(nullable = false, unique = true) 
	private String token;

	@Column(nullable = false)
	private UserStatus status;

    @Column(nullable = true)
    private String birthday;

    @Column (nullable = false)
    private long score = 0;

    @Column (nullable = false)
    private long correctGuesses = 0;

    @Column(nullable = false)
    private long bestClues = 0;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

    public LocalDateTime getCreated() {
        return this.created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public String getBirthday() {
        return this.birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
	    return this.password;
    }

    public void setPassword(String password) {
	    this.password = password;
    }

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

    public long getScore() {
        return score;
    }

    public void setScore(long score) {
        this.score = score;
    }

    public void addScore(long score) {
	    this.score = this.score + score;
    }

    public long getCorrectGuesses() {
        return correctGuesses;
    }

    public void setCorrectGuesses(long correctGuesses) {
        this.correctGuesses = correctGuesses;
    }

    public void incrementCorrectGuessCount(long correctGuesses){
	    this.correctGuesses += correctGuesses;
    }

    public long getBestClues() {
        return bestClues;
    }

    public void setBestClues(long bestClues) {
        this.bestClues = bestClues;
    }

    public void incBestCluesCount(long bestCluesCount) {
	    this.bestClues += bestCluesCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public UserStatus getStatus() {
		return status;
	}

	public void setStatus(UserStatus status) {
		this.status = status;
	}


}

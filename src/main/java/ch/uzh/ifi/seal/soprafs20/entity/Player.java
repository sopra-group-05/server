package ch.uzh.ifi.seal.soprafs20.entity;

import ch.uzh.ifi.seal.soprafs20.constant.UserStatus;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Internal Player Representation
 * This class composes the internal representation of the player and defines how the player is stored in the database.
 * Every variable will be mapped into a database field with the @Column annotation
 * - nullable = false -> this cannot be left empty
 * - unique = true -> this value must be unqiue across the database -> composes the primary key
 */
@Entity
@Table(name = "PLAYER")
public class Player implements Serializable {

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

	public UserStatus getStatus() {
		return status;
	}

	public void setStatus(UserStatus status) {
		this.status = status;
	}
}

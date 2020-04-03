package ch.uzh.ifi.seal.soprafs20.entity;

import ch.uzh.ifi.seal.soprafs20.constant.PlayerRole;
import ch.uzh.ifi.seal.soprafs20.constant.PlayerStatus;
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
public class Player {

    public Player(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.token = user.getToken();
        this.status = PlayerStatus.JOINED;
    }

	@Id
    @Column(nullable = false, unique = true)
	private Long id;
	
	@Column(nullable = false, unique = true) 
	private String username;
	
	@Column(nullable = false, unique = true) 
	private String token;

	@Column(nullable = false)
	private PlayerStatus status;

    @Column(nullable = false)
    private PlayerRole role;

	public Long getId() {
		return id;
	}

	public String getUsername() {
		return username;
	}

	public String getToken() {
		return token;
	}

	public PlayerStatus getStatus() {
		return status;
	}

	public void setStatus(PlayerStatus status) {
		this.status = status;
	}

    public PlayerRole getRole() {
        return role;
    }

    public void setRole(PlayerRole role) {
        this.role = role;
    }
}

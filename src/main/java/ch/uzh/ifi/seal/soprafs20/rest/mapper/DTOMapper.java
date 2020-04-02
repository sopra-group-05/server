package ch.uzh.ifi.seal.soprafs20.rest.mapper;

import ch.uzh.ifi.seal.soprafs20.constant.GameModeStatus;
import ch.uzh.ifi.seal.soprafs20.entity.Lobby;
import ch.uzh.ifi.seal.soprafs20.entity.User;
import ch.uzh.ifi.seal.soprafs20.rest.dto.*;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;

/**
 * DTOMapper
 * This class is responsible for generating classes that will automatically transform/map the internal representation
 * of an entity (e.g., the User) to the external/API representation (e.g., UserGetDTO for getting, UserPostDTO for creating)
 * and vice versa.
 * Additional mappers can be defined for new entities.
 * Always created one mapper for getting information (GET) and one mapper for creating information (POST).
 */
@Mapper
public interface DTOMapper {

    DTOMapper INSTANCE = Mappers.getMapper(DTOMapper.class);

    @Mapping(source = "password", target = "password")
    @Mapping(source = "username", target = "username")
    User convertUserPostDTOtoEntity(UserPostDTO userPostDTO);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "username", target = "username")
    @Mapping(source = "token", target = "token")
    @Mapping(source = "birthday", target = "birthday")
    User convertUserPutDTOtoEntity(UserPutDTO userPutDTO);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "username", target = "username")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "created", target = "created")
    @Mapping(source = "birthday", target = "birthday")
    UserGetDTO convertEntityToUserGetDTO(User user);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "token", target = "token")
    UserLoginGetDTO convertEntityOfLoggedInUserGetDTO(User user);

    @Mapping(source = "lobbyName", target = "lobbyName")
    @Mapping(source = "gameMode", target = "gameMode", qualifiedByName = "convertToEnum")
    Lobby convertLobbyPostDTOtoEntity(LobbyPostDTO lobbyPostDTO);


    @Named("convertToEnum")
    default GameModeStatus convertToEnum(Integer gameMode) {
        if (gameMode == 0) {
            return GameModeStatus.HUMANS;
        }
        else { return GameModeStatus.BOTS; }
    }

    @Mapping(source = "id", target = "id")
    @Mapping(source = "lobbyName", target = "lobbyName")
    @Mapping(source = "deck", target = "deck")
    @Mapping(source = "lobbyStatus", target = "lobbyStatus")
    @Mapping(source = "players", target = "players")
    @Mapping(source = "gameMode", target = "gameMode")
    @Mapping(source = "creator", target = "creator")
    LobbyGetDTO convertEntityToLobbyGetDTO(Lobby createdLobby);
}

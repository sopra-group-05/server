package ch.uzh.ifi.seal.soprafs20.rest.mapper;

import ch.uzh.ifi.seal.soprafs20.constant.GameModeStatus;
import ch.uzh.ifi.seal.soprafs20.constant.Language;
import ch.uzh.ifi.seal.soprafs20.entity.*;
import ch.uzh.ifi.seal.soprafs20.rest.dto.*;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;


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
    @Mapping(source = "gameMode", target = "gameMode", qualifiedByName = "convertInt")
    @Mapping(source = "language", target = "language", qualifiedByName = "convertString")
    Lobby convertLobbyPostDTOtoEntity(LobbyPostDTO lobbyPostDTO);

    @Named("convertInt")
    default GameModeStatus convertIntToEnum(Integer gameMode) {
        try {
            if (gameMode == 0) {
                return GameModeStatus.HUMANS;
            }
            else if (gameMode == 1) { return GameModeStatus.BOTS; }
            else {throw new IllegalArgumentException("Valid values are: (0, 1)." +
                    "Default gameMode HUMANS was returned.");}
        }
        catch (IllegalArgumentException e) {
            String msg = "Please provide an Integer value to the gameMode key." +
                    "Valid values are: (0, 1)." +
                    "Default gameMode HUMANS was returned.";
           throw new IllegalArgumentException(msg, e);
        }
    }
    @Named("convertString")
    default Language convertStringToEnum(String languageString) {
        try {
            return Language.valueOf(languageString);
        }
        catch (IllegalArgumentException e) {
            String msg = "Please provide a String value to the language key." +
                    "Valid values are: ('EN', 'DE')." +
                    "Default language ENGLISH was returned";
            throw new IllegalArgumentException(msg, e);
        }
    }

    @Mapping(source = "id", target = "id")
    @Mapping(source = "lobbyName", target = "lobbyName")
    @Mapping(source = "lobbyStatus", target = "lobbyStatus")
    @Mapping(source = "players", target = "players")
    @Mapping(source = "gameMode", target = "gameMode")
    @Mapping(source = "creator", target = "creator")
    @Mapping(source = "language", target = "language")
    LobbyGetDTO convertEntityToLobbyGetDTO(Lobby createdLobby);


    @Mapping(source = "id", target = "id")
    @Mapping(source = "username", target = "username")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "role", target = "role")
    PlayerGetDTO convertEntityToPlayerGetDTO(PlayerGetDTO convertedPlayer);

    @Mapping(source = "password", target = "password")
    User convertUserDeleteDTOToEntity(UserDeleteDTO userDeleteDTO);

    @Mapping(source = "hint", target = "hint")
    Clue convertCluePOSTDTOToEntity(CluePostDTO cluePostDTO);

    @Mapping(source = "hint", target= "hint")
    @Mapping(source = "id", target = "id")
    ClueGetDTO convertClueToClueGetDTO(Clue clue);

    @Mapping(source = "word", target= "word")
    @Mapping(source = "id", target = "id")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "timedrawn", target = "timedrawn")
    MysteryWordGetDto convertMysteryWordToMysteryWordGetDTO(MysteryWord mysteryWord);
}

package ch.uzh.ifi.seal.soprafs20.service;

import ch.uzh.ifi.seal.soprafs20.constant.MysteryWordStatus;
import ch.uzh.ifi.seal.soprafs20.entity.MysteryWord;
import ch.uzh.ifi.seal.soprafs20.exceptions.ForbiddenException;
import ch.uzh.ifi.seal.soprafs20.repository.MysteryWordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

public class MysteryWordServiceTest {

    @Mock
    private MysteryWordRepository mysteryWordRepository;

    @InjectMocks
    private MysteryWordService mysteryWordService;

    private MysteryWord mysteryWord;

    /**
     * Prepares mock data for each test
     */
    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);

        // given
        mysteryWord = new MysteryWord();
        mysteryWord.setId(1L);
        mysteryWord.setNumber(1);
        mysteryWord.setWord("Star");
        mysteryWord.setDescription("is a luminous object");
        mysteryWord.setStatus(MysteryWordStatus.NOT_USED);

        // when -> any object is being save in the mysteryWordRepository -> return the dummy testUser
        when(mysteryWordRepository.save(Mockito.any())).then(AdditionalAnswers.returnsFirstArg());
        when(mysteryWordRepository.saveAll(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0, List.class));
        Mockito.when(mysteryWordRepository.findById(mysteryWord.getId())).thenReturn(Optional.of(mysteryWord));
        doNothing().when(mysteryWordRepository).delete(Mockito.any());
        doNothing().when(mysteryWordRepository).deleteAll(Mockito.any());
    }

    /**
     * Checks save MysteryWord function with correct input
     */
    @Test
    public void saveMysteryWord_validInputs_success() {
        // when -> any object is being save in the mysteryWordRepository -> return the dummy testUser
        MysteryWord mysteryWordLocal = mysteryWordService.getMysteryWordById(mysteryWord.getId());

        assertEquals(mysteryWord.getId(), mysteryWordLocal.getId());
        assertEquals(mysteryWord.getWord(), mysteryWordLocal.getWord());
        assertEquals(mysteryWord.getDescription() ,mysteryWordLocal.getDescription());
    }

    /**
     * Gets valid MysteryWord for given id
     */
    @Test
    public void getMysteryWordById_ValidInput() {
        MysteryWord dbMysteryWord = mysteryWordService.getMysteryWordById(mysteryWord.getId());

        assertEquals(mysteryWord.getId(), dbMysteryWord.getId());
        assertEquals(mysteryWord.getWord(), dbMysteryWord.getWord());
    }


    /**
     * Throws ForbiddenException for given invalid input
     */
    @Test
    public void getMysteryWordById_InvalidInput_throwsException() {

        // then -> attempt to create second user with same user -> check that an error is thrown
        String exceptionMessage = "MysteryWord not found";
        ForbiddenException exception = assertThrows(ForbiddenException.class, () -> mysteryWordService.getMysteryWordById(2L), exceptionMessage);
        assertEquals(exceptionMessage, exception.getMessage());
    }

    /**
     * saves a collection of mystery word and retrieves one
     */
    @Test
    public void saveAndGet_MysteryWordById_ValidInput() {

        MysteryWord mysteryWordNew = new MysteryWord();
        mysteryWordNew.setId(2L);
        mysteryWordNew.setNumber(2);
        mysteryWordNew.setWord("Earth");
        mysteryWordNew.setDescription("is a unique planet");
        mysteryWordNew.setStatus(MysteryWordStatus.NOT_USED);

        Mockito.when(mysteryWordRepository.findById(mysteryWordNew.getId())).thenReturn(Optional.of(mysteryWordNew));

        List<MysteryWord> list = new ArrayList<>();
        list.add(mysteryWord);
        list.add(mysteryWordNew);

        mysteryWordService.saveAll(list);

        MysteryWord dbMysteryWord = mysteryWordService.getMysteryWordById(2L);

        assertEquals(mysteryWordNew.getId(), dbMysteryWord.getId());
        assertEquals(mysteryWordNew.getWord(), dbMysteryWord.getWord());
    }

}

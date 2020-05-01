package ch.uzh.ifi.seal.soprafs20.service;

import ch.uzh.ifi.seal.soprafs20.entity.MysteryWord;
import ch.uzh.ifi.seal.soprafs20.exceptions.ForbiddenException;
import ch.uzh.ifi.seal.soprafs20.repository.MysteryWordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * MysteryWord Service
 * This class is the "worker" and responsible for all functionality related to the MysteryWord
 * (e.g., it converts, modifies, deletes, finds). The result will be passed back to the caller.
 */
@Service
@Transactional
public class MysteryWordService {

    private final Logger log = LoggerFactory.getLogger(MysteryWordService.class);

    private final MysteryWordRepository mysteryWordRepository;

    @Autowired
    public MysteryWordService(@Qualifier("mysteryWordRepository") MysteryWordRepository mysteryWordRepository) {
        this.mysteryWordRepository = mysteryWordRepository;
    }

    /**
     * This method will get a specific MysteryWord by ID from the MysteryWord Repository
     *
     * @return The requested MysteryWord
     * @see MysteryWord
     */
    public MysteryWord getMysteryWordById(Long id)
    {
        Optional<MysteryWord> mysteryWord = mysteryWordRepository.findById(id);
        return mysteryWord.orElseThrow(()->new ForbiddenException("MysteryWord not found"));
    }

    /**
     * Removes the mysteryWord from MysteryWord repository
     *
     * @param - the mysteryWord to be removed from the MysteryWord repository
     * */
    public void delete(MysteryWord mysteryWord) {
        if(mysteryWord != null) {
            mysteryWordRepository.delete(mysteryWord);
        }
    }

    /**
     * Removes the mysteryWords from MysteryWord repository
     *
     * @param - the mysteryWords to be removed from the MysteryWord repository
     * */
    public void deleteAll(List<MysteryWord> mysteryWords) {
        if(mysteryWords != null && !mysteryWords.isEmpty()) {
            mysteryWordRepository.deleteAll(mysteryWords);
        }
    }

    /**
     * Saves the mysteryWord using MysteryWord repository
     *
     * @param - the mysteryWord to be updated to the MysteryWord repository
     * */
    public void save(MysteryWord mysteryWord) {
        if(mysteryWord != null) {
            mysteryWordRepository.save(mysteryWord);
        }
    }

    /**
     * Saves the mysteryWords using MysteryWord repository
     *
     * @param - the mysteryWords to be updated to the MysteryWord repository
     * */
    public void saveAll(List<MysteryWord> mysteryWords) {
        if(mysteryWords != null && !mysteryWords.isEmpty()) {
            mysteryWordRepository.saveAll(mysteryWords);
        }
    }

    /**
     * Returns randomly selected Mystery words
     * */
    public List<MysteryWord> getMysteryWordsRandomly() {
        List<MysteryWord> mysteryWords = mysteryWordRepository.findAllLimit65Words();
        return mysteryWords;
    }

}

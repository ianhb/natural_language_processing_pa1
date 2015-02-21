import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.process.PTBTokenizer;

/**
 * Created by Ian on 2/20/2015.
 */
public interface NGram {

    public void makeMap(PTBTokenizer<Word> tokens);

    public String generateSentence();

}

import java.util.ArrayList;

/**
 * Created by Ian on 2/20/2015.
 */
public interface NGram {

    public void makeMap(ArrayList<String> tokens);

    public String generateSentence();

}

import java.util.ArrayList;

/**
 * Created by Ian on 2/23/2015.
 */
public class Email {


    boolean downspeak;
    boolean test;
    ArrayList<String> text;

    public Email(boolean down, ArrayList<String> words) {
        downspeak = down;
        text = words;
        test = false;
    }

    public Email(ArrayList<String> words) {
        test = true;
        text = words;
    }

    public boolean testCorrectness(boolean downspeak) {
        return !test && downspeak == this.downspeak;
    }

}

import java.util.ArrayList;

/**
 * Created by Ian on 2/23/2015.
 */
public class Email {


    boolean downspeak;
    ArrayList<String> text;

    public Email(boolean down, ArrayList<String> words) {
        downspeak = down;
        text = words;
    }

    public Email(ArrayList<String> words) {
        text = words;
    }

}

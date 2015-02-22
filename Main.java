import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.WordTokenFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.StringReader;
import java.util.ArrayList;

/**
 * Created by Ian on 2/19/2015.
 */
public class Main {

    public static void main(String[] argv) {
        BufferedReader buf;
        String line;
        String up_Train = "";
        String down_Train = "";
        String up_validation = "";
        String down_validation = "";
        String updown_Test = "";
        try {
            buf = new BufferedReader(new FileReader("training.txt"));
            while ((line = buf.readLine()) != null) {
                boolean downspeak = line.equals("DOWNSPEAK");
                buf.readLine();
                while (!(line = buf.readLine()).equals("**EOM**")) {
                    if (downspeak) {
                        down_Train += line + " ";
                    } else {
                        up_Train += line + " ";
                    }
                }
            }
            buf.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Trigram trigram = new Trigram();
        Bigram bigram = new Bigram();
        bigram.makeMap(arrayifyTokens(tokenizeString(up_Train)));

        trigram.makeMap(arrayifyTokens(tokenizeString(up_Train)));
        for (Trigram.WordTriple triple : trigram.popularTokens) {
            System.out.println(triple.toString() + ":" + triple.getCount());
        }
        System.out.println(trigram.generateSentence());
    }

    private static PTBTokenizer<Word> tokenizeString(String words) {
        words = words.replaceAll("\\. ", "</s> <s>");
        words = "<s> " + words;
        words = words.substring(0, words.lastIndexOf("<s>"));
        return new PTBTokenizer<Word>(new StringReader(words), new WordTokenFactory(), "ptb3Escaping=false");
    }

    private static ArrayList<String> arrayifyTokens(PTBTokenizer<Word> tokens) {
        String last;
        String current = null;
        ArrayList<String> words = new ArrayList<String>();
        while (tokens.hasNext()) {
            last = current;
            current = tokens.next().word();
            if (!current.equals(last)) {
                words.add(current);
            }
        }
        return words;
    }
}

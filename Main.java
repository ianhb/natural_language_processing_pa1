import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.WordTokenFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.StringReader;

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
                buf.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Bigram up_train = new Bigram();
        up_train.makeMap(tokenizeString(up_Train));
        Unigram up_train_uni = new Unigram();
        up_train_uni.makeMap(tokenizeString(up_Train));
        for (Word w : up_train_uni.popularTokens) {
            System.out.println(w + ":" + up_train_uni.map.get(w));
        }
        for (Bigram.WordDouble wordDouble : up_train.popularTokens) {
            System.out.println(wordDouble + ":" + up_train.map.get(wordDouble.lastWord).map.get(wordDouble.curWord));
        }
        System.out.println(up_train.generateSentence());
    }

    private static PTBTokenizer<Word> tokenizeString(String words) {
        words = words.replaceAll("\\. ", "</s> <s>");
        words = "<s> " + words;
        words = words.substring(0, words.lastIndexOf("<s>"));
        words = words.replaceAll("\\(", "");
        words = words.replaceAll("\\)", "");
        return new PTBTokenizer<Word>(new StringReader(words), new WordTokenFactory(), "ptb3Escaping=false");
    }
}

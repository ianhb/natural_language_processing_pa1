import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * Class to model a bigram of a language model
 * <p/>
 * <p/>
 * Created by Ian on 2/20/2015.
 */
public class Bigram implements NGram {

    HashMap<String, Unigram> map = new HashMap<String, Unigram>();
    ArrayList<WordDouble> popularTokens = new ArrayList<WordDouble>();
    int count = 0;

    public String generateSentence() {
        String sentence = "<s>";
        String second = sentence;
        String first = "";
        Random random = new Random();
        while (!first.equals("</s>")) {
            Unigram lastGram = map.get(first);
            int x = random.nextInt(lastGram.tokens.size() - 1);
            second = lastGram.tokens.get(x);
            sentence += " " + first;
        }
        return sentence;
    }

    /**
     * Takes in a tokenizer and matches all of the bigrams to a map of word to unigram
     * if a bigram is (foo | bar), it is mapped to bar -> (foo -> int) where (foo -> int) is a unigram
     *
     * @param tokens tokenizer containing words
     */
    public void makeMap(ArrayList<String> tokens) {
        String second = "<s>";
        String first;
        for (String s : tokens) {
            first = second;
            second = s;
            second = second.toLowerCase();
            count++;
            if (first.equals("</s>")) {
                first = "<s>";
            }
            if (map.containsKey(second)) {
                Unigram unigram = map.get(second);
                unigram.put(first);
                checkForPopular(first, second);
            } else {
                Unigram unigram = new Unigram();
                map.put(second, unigram);
                unigram.put(first);
                if (popularTokens.size() < 10) {
                    popularTokens.add(new WordDouble(first, second));
                }
            }
        }
    }

    public void put(String second, String first) {
        count++;
        if (second != null && second.equals("</s>")) {
            second = "<s>";
        }
        if (map.containsKey(second)) {
            Unigram unigram = map.get(second);
            unigram.put(first);
            checkForPopular(first, second);
        } else {
            Unigram unigram = new Unigram();
            map.put(second, unigram);
            unigram.put(first);
            if (popularTokens.size() < 10) {
                popularTokens.add(new WordDouble(first, second));
            }
        }
    }

    public float getProbability(String first, String second) {
        return ((float) map.get(second).map.get(first)) / ((float) map.get(second).count);
    }

    private void checkForPopular(String first, String second) {
        //if the popularTokens doesn't contain this bigram
        if (!contains(first, second)) {
            popularTokens.add(new WordDouble(first, second));
            int i = popularTokens.size() - 1;
            int x = popularTokens.get(i).getCount();
            int y = popularTokens.get(i - 1).getCount();
            while (i > 1 && x > y) {
                WordDouble temp = popularTokens.get(i);
                popularTokens.set(i, popularTokens.get(i - 1));
                popularTokens.set(i - 1, temp);
                i--;
                y = popularTokens.get(i - 1).getCount();
            }
            popularTokens.remove(popularTokens.size() - 1);
        }
        //popularTokens contains the bigram
        else {
            ArrayList<WordDouble> newPopular = new ArrayList<WordDouble>();
            while (!popularTokens.isEmpty()) {
                int i = 0;
                WordDouble topWord = null;
                for (WordDouble word : popularTokens) {
                    if (word.getCount() > i) {
                        i = word.getCount();
                        topWord = word;
                    }
                }
                popularTokens.remove(topWord);
                newPopular.add(topWord);
            }
            popularTokens = newPopular;
        }
    }

    private boolean contains(String first, String second) {
        for (WordDouble w : popularTokens) {
            if (w.first.equals(first) && w.second.equals(second)) {
                return true;
            }
        }
        return false;
    }


    /**
     * Wrapper class for a bigram that can also be written as (second | first)
     */
    public class WordDouble {
        String first;
        String second;

        public WordDouble(String fir, String sec) {
            first = fir;
            second = sec;
        }

        public int getCount() {
            return map.get(second).map.get(first);
        }

        @Override
        public String toString() {
            return "(" + second + "|" + first + ")";
        }
    }

}

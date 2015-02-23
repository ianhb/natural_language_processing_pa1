import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
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
    ArrayList<WordDouble> tokens = new ArrayList<WordDouble>();
    ArrayList<String> types = new ArrayList<String>();

    int count = 0;

    /**
     * Returns a randomly generated string starting with "<s>" and ending with "</s>"
     * Uses the probability from the bigram map to find a sentence.
     *
     * @return a randomly generated string that models a sentence
     */
    @Override
    public String generateSentence() {
        String sentence = "<s>";
        String first;
        String second = "<s>";
        Random random = new Random();
        while (!second.equals("</s>")) {
            first = second;
            Unigram lastGram = map.get(first);
            int x;
            if (lastGram.tokens.size() < 2) {
                x = 0;
            } else {
                x = random.nextInt(lastGram.tokens.size() - 1);
            }
            second = lastGram.tokens.get(x);
            while (map.get(first).tokens == null) {
                x = random.nextInt(lastGram.tokens.size() - 1);
                second = lastGram.tokens.get(x);
            }
            sentence += " " + second;
        }
        return sentence;
    }

    /**
     * Takes in a tokenizer and matches all of the bigrams to a map of string to unigram
     * if a bigram is (second | first), it is mapped to first -> (second -> int) where (second -> int) is a unigram
     *
     * @param tokens List of words to make map from
     */
    @Override
    public void makeMap(ArrayList<String> tokens) {
        String second = "<s>";
        String first;
        for (String s : tokens) {
            if (!types.contains(s)) {
                types.add(s);
            }
            first = second;
            second = s;
            second = second.toLowerCase();
            count++;
            if (first.equals("</s>")) {
                continue;
            }
            this.tokens.add(new WordDouble(first, second));
            if (map.containsKey(first)) {
                Unigram unigram = map.get(first);
                unigram.put(second);
                checkForPopular(first, second);
            } else {
                Unigram unigram = new Unigram();
                map.put(first, unigram);
                unigram.put(second);
                if (popularTokens.size() < 10) {
                    popularTokens.add(new WordDouble(first, second));
                }
            }
        }
        laplaceSmooth();
    }

    @Override
    public String toString() {
        String ret = "";
        for (Map.Entry<String, Unigram> entry : map.entrySet()) {
            String second = entry.getKey();
            for (Map.Entry<String, Integer> ent : entry.getValue().map.entrySet()) {
                String first = ent.getKey();
                ret += second + ":" + first + ":" + ent.getValue() + "\n";
            }
        }
        return ret;
    }

    public void put(String second, String first) {
        if (!types.contains(first)) {
            types.add(first);
        }
        count++;
        this.tokens.add(new WordDouble(first, second));
        if (map.containsKey(first)) {
            Unigram unigram = map.get(first);
            unigram.put(second);
            checkForPopular(first, second);
        } else {
            Unigram unigram = new Unigram();
            map.put(first, unigram);
            unigram.put(second);
            if (popularTokens.size() < 10) {
                popularTokens.add(new WordDouble(first, second));
            }
        }
    }

    public float getProbability(String first, String second) {
        return ((float) map.get(second).map.get(first)) / ((float) map.get(second).count);
    }

    private void laplaceSmooth() {
        SmoothingThread thread = new SmoothingThread(this);
        new Thread(thread).start();
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
            return map.get(first).map.get(second);
        }

        @Override
        public String toString() {
            return "(" + second + "|" + first + ")";
        }
    }

    private class SmoothingThread implements Runnable {

        Bigram bigram;

        public SmoothingThread(Bigram bigram) {
            this.bigram = bigram;
        }

        @Override
        public void run() {
            System.out.println("Smoothing with " + bigram.types.size() + " types");
            for (String unigramString : bigram.types) {
                if (!bigram.map.containsKey(unigramString)) {
                    bigram.map.put(unigramString, new Unigram());
                }
                for (String integerString : bigram.types) {
                    bigram.count++;
                    if (!bigram.map.get(unigramString).map.containsKey(integerString)) {
                        bigram.map.get(unigramString).map.put(integerString, 1);
                    } else {
                        int x = bigram.map.get(unigramString).map.get(integerString);
                        bigram.map.get(unigramString).map.put(integerString, x + 1);
                    }
                }
            }
            System.out.println("Bigram Smoothing Complete");
        }
    }

}

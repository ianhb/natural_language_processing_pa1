import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Class to model a trigram of a language model
 *
 *
 * Created by Ian on 2/21/2015.
 */
public class Trigram implements NGram {

    HashMap<String, Bigram> map = new HashMap<String, Bigram>();
    ArrayList<WordTriple> popularTokens = new ArrayList<WordTriple>();
    ArrayList<String> types = new ArrayList<String>();
    int count = 0;


    public Trigram() {
        types.add(Main.UNKNOWN);
        map.put(Main.UNKNOWN, new Bigram());
    }

    /**
     * Takes in a tokenizer and matches all of the trigrams to a map of string to bigram
     * if a trigram is (third | first second), it is mapped to first -> (second -> (third -> int)) where (third -> int) is a unigram
     * and (second -> (third -> int)) is a bigram
     *
     * @param tokens List of words to make map from
     */
    @Override
    public void makeMap(ArrayList<String> tokens) {

        String first;
        String second = "<s>";
        String third = "<s>";
        for (String s : tokens) {
            if (!types.contains(s)) {
                types.add(s);
            }
            first = second;
            second = third;
            third = s;
            third = third.toLowerCase();
            count++;
            if (second.equals("</s>") || first.equals("</s>")) {
                continue;
            }
            if (map.containsKey(first)) {
                Bigram bigram = map.get(first);
                bigram.put(third, second);
                checkForPopular(first, second, third);
            } else {
                Bigram bigram = new Bigram();
                map.put(first, bigram);
                if (popularTokens.size() < 10) {
                    popularTokens.add(new WordTriple(first, second, third));
                }
                bigram.put(third, second);
            }
        }
    }

    /**
     * Returns a randomly generated string starting with "<s>" and ending with "</s>"
     * Uses the probability from the trigram map to find a sentence.
     *
     * @return a randomly generated string that models a sentence
     */
    @Override
    public String generateSentence() {
        String sentence = "<s>";
        String first = "<s>";
        Random random = new Random();
        Bigram startBigram = map.get(first);
        int y = random.nextInt(startBigram.tokens.size() - 1);
        Bigram.WordDouble wordDouble = startBigram.tokens.get(y);
        String second = wordDouble.first;
        String third = wordDouble.second;
        sentence += " " + second + " " + third;
        System.out.println(sentence);

        while (!third.equals("</s>")) {
            Bigram bigram = map.get(first);
            Unigram unigram = bigram.map.get(second);
            first = second;
            second = third;
            System.out.println(unigram);
            int x;
            if (unigram.tokens.size() < 2) {
                x = 0;
            } else {
                x = random.nextInt(unigram.tokens.size() - 1);
            }
            third = unigram.tokens.get(x);

            Bigram testBigram = map.get(second);
            Unigram testUnigram = testBigram.map.get(third);
            while (testUnigram == null) {
                if (unigram.tokens.size() < 2) {
                    x = 0;
                } else {
                    x = random.nextInt(unigram.tokens.size() - 1);
                }
                third = unigram.tokens.get(x);
                testBigram = map.get(second);
                testUnigram = testBigram.map.get(third);
            }

            sentence += " " + third;
            System.out.println(sentence);
        }
        return sentence;
    }

    @Override
    public String toString() {
        String ret = "";
        for (Map.Entry<String, Bigram> entry : map.entrySet()) {
            String third = entry.getKey();
            for (Map.Entry<String, Unigram> ent : entry.getValue().map.entrySet()) {
                String second = ent.getKey();
                for (Map.Entry<String, Integer> entry1 : ent.getValue().map.entrySet()) {
                    String first = entry1.getKey();
                    ret += third + ":" + second + ":" + first + ":" + entry1.getValue() + "\n";
                }
            }
        }
        return ret;
    }

    private void checkForPopular(String first, String second, String third) {
        //if the popularTokens doesn't contain this bigram
        if (!contains(first, second, third)) {
            popularTokens.add(new WordTriple(first, second, third));
            int i = popularTokens.size() - 1;
            int x = popularTokens.get(i).getCount();
            int y = popularTokens.get(i - 1).getCount();
            while (i > 1 && x > y) {
                WordTriple temp = popularTokens.get(i);
                popularTokens.set(i, popularTokens.get(i - 1));
                popularTokens.set(i - 1, temp);
                i--;
                y = popularTokens.get(i - 1).getCount();
            }
            popularTokens.remove(popularTokens.size() - 1);
        }
        //popularTokens contains the bigram
        else {
            ArrayList<WordTriple> newPopular = new ArrayList<WordTriple>();
            while (!popularTokens.isEmpty()) {
                int i = 0;
                WordTriple topWord = null;
                for (WordTriple word : popularTokens) {
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

    public void showResults(String first, String second, String third) {
        if (!types.contains(first)) {
            first = Main.UNKNOWN;
        }
        if (!types.contains(second)) {
            second = Main.UNKNOWN;
        }
        if (!types.contains(third)) {
            second = Main.UNKNOWN;
        }


    }

    public float unsmoothedProbability(String first, String second, String third) {
        if (!types.contains(first)) {
            first = Main.UNKNOWN;
        }
        if (!types.contains(second)) {
            second = Main.UNKNOWN;
        }
        if (!types.contains(third)) {
            second = Main.UNKNOWN;
        }
        float num;
        if (!map.containsKey(first) || !map.get(first).map.containsKey(second) || !map.get(first).map.get(second).map.containsKey(third)) {
            return 0;
        } else {
            num = (float) map.get(first).map.get(second).map.get(third);
        }
        float denom = (float) map.get(first).map.get(second).count;
        return num / denom;
    }

    public float laplaceSmoothProbability(String first, String second, String third) {
        if (!types.contains(first)) {
            first = Main.UNKNOWN;
        }
        if (!types.contains(second)) {
            second = Main.UNKNOWN;
        }
        if (!types.contains(third)) {
            second = Main.UNKNOWN;
        }
        float num;
        if (!map.containsKey(first) || !map.get(first).map.containsKey(second) || !map.get(first).map.get(second).map.containsKey(third)) {
            num = 1;
        } else {
            num = (float) map.get(first).map.get(second).map.get(third) + 1;
        }
        float denom;
        if (!map.containsKey(first) || !map.get(first).map.containsKey(second)) {
            denom = types.size();
        } else {
            denom = (float) map.get(first).map.get(second).count + types.size();
        }
        return num / denom;
    }

    public double perplexity(ArrayList<String> testSet) {
        double total = 0;
        String first;
        String second = null;
        String third = null;
        int N = 0;
        for (String s : testSet) {
            N++;
            first = second;
            second = third;
            third = s;
            if (first == null) {
                N--;
                continue;
            }
            if (first.equals("</s>") || second.equals("</s>")) {
                continue;
            }
            double x = -Math.log(laplaceSmoothProbability(first, second, third));
            total += x;
        }
        return total * (1.0 / N);
    }

    private boolean contains(String first, String second, String third) {
        for (WordTriple w : popularTokens) {
            if (w.first.equals(first) && w.second.equals(second) && w.third.equals(third)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Class that wraps a trigram that can be also written as (third | first second)
     */
    public class WordTriple {

        String first;
        String second;
        String third;

        public WordTriple(String fir, String sec, String thi) {
            first = fir;
            second = sec;
            third = thi;
        }

        public int getCount() {
            return map.get(first).map.get(second).map.get(third);
        }

        @Override
        public String toString() {
            return "(" + third + " | " + first + " " + second + ")";
        }
    }
}

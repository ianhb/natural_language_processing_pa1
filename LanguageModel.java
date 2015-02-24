import java.util.ArrayList;
import java.util.HashMap;

/**
 * A class to represent a language model that serves to house a unigram, bigram and trigram
 * and is able to calculate interpolated probabilities for the entire set.
 * <p/>
 * Created by Ian on 2/23/2015.
 */
public class LanguageModel {

    Trigram trigram;
    Bigram bigram;
    Unigram unigram;
    HashMap<Integer, Integer> goodTuringMap;


    public LanguageModel(Unigram unigram, Bigram bigram, Trigram trigram) {
        this.unigram = unigram;
        this.bigram = bigram;
        this.trigram = trigram;
    }

    /**
     * Takes in an Arraylist of strings and returns the perplexity of that arraylist
     * appearing the language model
     *
     * @param words a list of words
     * @return the perplexity of words appearing the model
     */
    public double perplexity(ArrayList<String> words) {
        double total = 0;
        String first;
        String second = null;
        String third = null;
        double N = 0;
        goodTuringMap = unigram.prepgoodTuring();
        for (String s : words) {
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
            double z = trigramInterpolation(first, second, third);
            double x = (-Math.log(z));
            if (Double.isNaN(x) || Double.isNaN(z)) {
                System.out.println(z + " " + x);
                System.exit(1);
            }
            total += x;
        }
        double exp = total / N;
        return Math.pow(Math.E, exp);
    }

    private float goodTuring(String string) {
        return unigram.goodTuring(string, goodTuringMap);
    }

    /**
     * Returns the probability of a word appearing in a language model by combining
     * trigram, bigram and unigram probabilities.
     *
     * @param first  the first word in a sequence (only used in trigram)
     * @param second the second word in a sequence (used in the bigram and trigram)
     * @param third  the third word in a sequence (used in all n-grams)
     * @return the interpolated probability the word appears in the model
     */
    private double trigramInterpolation(String first, String second, String third) {
        return ((1.0 / 2) * trigram.laplaceSmoothProbability(first, second, third)) + ((1.0 / 3) *
                bigram.laplaceSmoothProbability(second, third)) + ((1.0 / 3) * unigram.laplaceSmoothProbabilty(third));
    }


}

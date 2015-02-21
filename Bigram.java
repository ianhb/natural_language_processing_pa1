import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.process.PTBTokenizer;

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

    HashMap<Word, Unigram> map = new HashMap<Word, Unigram>();
    ArrayList<WordDouble> popularTokens = new ArrayList<WordDouble>();
    int count = 0;

    public String generateSentence() {
        String sentence = "<s>";
        String lastWord = "<s>";
        String curWord = "";
        Random random = new Random();
        while (!curWord.equals("</s>")) {
            Word last = new Word(lastWord);
            Unigram lastGram = map.get(last);
            int x = random.nextInt(lastGram.tokens.size() - 1);
            Word cur = lastGram.tokens.get(x);
            curWord = cur.word();
            sentence += " " + curWord;
        }
        return sentence;
    }

    /**
     * Takes in a tokenizer and matches all of the bigrams to a map of word to unigram
     * if a bigram is (foo | bar), it is mapped to bar -> (foo -> int) where (foo -> int) is a unigram
     *
     * @param tokens tokenizer containing words
     */
    public void makeMap(PTBTokenizer<Word> tokens) {
        Word lastWord;
        Word curWord = null;
        while (tokens.hasNext()) {
            lastWord = curWord;
            curWord = tokens.next();
            if (lastWord != null && curWord.word().equals(lastWord.word())) {
                System.out.println(curWord);
            }
            curWord.setWord(curWord.word().toLowerCase());
            if (lastWord != null && lastWord.word().equals("</s>")) {
                continue;
            }
            if (map.containsKey(lastWord)) {
                count++;
                Unigram unigram = map.get(lastWord);
                unigram.put(curWord);
                checkForPopular(curWord, lastWord);
            } else {
                count++;
                Unigram unigram = new Unigram();
                map.put(lastWord, unigram);
                unigram.put(curWord);
                if (popularTokens.size() < 10) {
                    popularTokens.add(new WordDouble(curWord, lastWord));
                }
            }

        }
    }

    private void checkForPopular(Word curWord, Word lastWord) {

        //if the popularTokens doesn't contain this bigram
        if (!contains(curWord, lastWord)) {
            popularTokens.add(new WordDouble(curWord, lastWord));
            for (int i = popularTokens.size() - 1; i > 0 &&
                    map.get(popularTokens.get(i - 1).lastWord).map.get(popularTokens.get(i - 1).curWord)
                            <= map.get(popularTokens.get(i).lastWord).map.get(popularTokens.get(i).curWord); i--) {
                WordDouble temp = popularTokens.get(i);
                popularTokens.set(i, popularTokens.get(i - 1));
                popularTokens.set(i - 1, temp);
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
                    if (map.get(lastWord).map.get(curWord) > i) {
                        i = map.get(lastWord).map.get(curWord);
                        topWord = word;
                    }
                }
                popularTokens.remove(topWord);
                newPopular.add(topWord);
            }
            popularTokens = newPopular;
        }
    }

    private boolean contains(Word cur, Word last) {
        for (WordDouble w : popularTokens) {
            if (w.curWord.equals(cur) && w.lastWord.equals(last)) {
                return true;
            }
        }
        return false;
    }

    public static class WordDouble {
        Word curWord;
        Word lastWord;

        public WordDouble(Word cur, Word last) {
            curWord = cur;
            lastWord = last;
        }

        @Override
        public String toString() {
            return "(" + curWord + "|" + lastWord + ")";
        }
    }


}

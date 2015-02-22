import java.util.*;

/**
 * Created by Ian on 2/21/2015.
 */
public class Trigram implements NGram {

    HashMap<String, Bigram> map = new HashMap<String, Bigram>();
    ArrayList<WordTriple> popularTokens = new ArrayList<WordTriple>();
    int count = 0;

    @Override
    public void makeMap(ArrayList<String> tokens) {
        String first;
        String second = "<s>";
        String third = "<s>";
        for (String s : tokens) {
            first = second;
            second = third;
            third = s;
            third = third.toLowerCase();
            count++;
            if (second.equals("</s>")) {
                second = "<s>";
                first = "<s>";
            }
            if (map.containsKey(first)) {
                Bigram bigram = map.get(first);
                bigram.put(second, third);
                checkForPopular(first, second, third);
            } else {
                Bigram bigram = new Bigram();
                map.put(first, bigram);
                if (popularTokens.size() < 10) {
                    popularTokens.add(new WordTriple(first, second, third));
                }
                bigram.put(second, third);
            }
        }
    }


    @Override
    public String generateSentence() {
        String first = "<s>";
        String second = first;
        String third = first;
        String sentence = first + " " + second + " " + third;
        Random random = new Random();
        while (!third.equals("</s>")) {
            Bigram firstGram = map.get(first);
            Unigram secondGram = firstGram.map.get(second);
            first = second;
            second = third;
            int x = random.nextInt(secondGram.tokens.size() - 1);
            third = secondGram.tokens.get(x);
            sentence += " " + third;
        }
        return sentence;
    }

    @Override
    public String toString() {
        Iterator<Map.Entry<String, Bigram>> iterator = map.entrySet().iterator();
        String ret = "";
        while (iterator.hasNext()) {
            Map.Entry<String, Bigram> entry = iterator.next();
            String third = entry.getKey();
            Iterator<Map.Entry<String, Unigram>> bigIt = entry.getValue().map.entrySet().iterator();
            while (bigIt.hasNext()) {
                Map.Entry<String, Unigram> ent = bigIt.next();
                String second = ent.getKey();
                Iterator<Map.Entry<String, Integer>> uniIt = ent.getValue().map.entrySet().iterator();
                while (uniIt.hasNext()) {
                    Map.Entry<String, Integer> entry1 = uniIt.next();
                    String first = entry1.getKey();
                    ret += third + ":" + second + ":" + first + ":" + entry1.getValue() + " ";
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
            if (!map.get(first).map.get(second).map.containsKey(third)) {
                System.out.println("third null");
            }
            return map.get(first).map.get(second).map.get(third);
        }

        @Override
        public String toString() {
            return "(" + third + " | " + first + " " + second + ")";
        }
    }
}

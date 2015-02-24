import java.util.*;

/**
 * Class to represent a unigram in a language model
 * <p/>
 * Created by Ian on 2/20/2015.
 */
public class Unigram {

    HashMap<String, Integer> map = new HashMap<String, Integer>();
    ArrayList<String> popularTokens = new ArrayList<String>();
    ArrayList<String> tokens = new ArrayList<String>();
    ArrayList<String> types = new ArrayList<String>();

    int count = 0;

    public Unigram() {
        types.add(Main.UNKNOWN);
        map.put(Main.UNKNOWN, 0);
    }

    public void makeMap(ArrayList<String> tokens) {
        for (String s : tokens) {
            put(s);
        }
    }

    public void put(String w) {
        count++;
        w = w.toLowerCase();
        tokens.add(w);
        if (map.containsKey(w)) {
            int x = map.get(w);
            map.put(w, x + 1);
            checkForPopular(w);
        } else {
            if (popularTokens.size() < 10) {
                popularTokens.add(w);
            }
            map.put(w, 1);
        }
    }

    public float unsmoothedProbability(String word) {
        return ((float) map.get(word)) / ((float) count);
    }

    public float laplaceSmoothProbabilty(String word) {
        if (map.containsKey(word)) {
            return ((float) map.get(word) + 1) / ((float) count + types.size());
        } else {
            return 1 / types.size();
        }
    }

    public HashMap<Integer, Integer> prepgoodTuring() {
        HashMap<Integer, Integer> NsubI = new HashMap<Integer, Integer>();
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            int count = entry.getValue();
            if (NsubI.containsKey(count)) {
                NsubI.put(count, NsubI.get(count) + 1);
            } else {
                NsubI.put(count, 1);
            }
        }
        return NsubI;
    }

    public float goodTuring(String word, HashMap<Integer, Integer> NsubI) {
        if (!types.contains(word)) {
            word = Main.UNKNOWN;
        }
        int wordCount = map.get(word);
        float num = (wordCount + 1) * (NsubI.get(wordCount + 1) / NsubI.get(wordCount));
        float denom = (float) count;
        return num / denom;
    }

    public double perplexity(ArrayList<String> words) {
        double total = 0;
        int N = 0;
        for (String word : words) {
            N++;
            if (word.equals("<s>")) {
                N--;
                continue;
            }
            double x = -Math.log(laplaceSmoothProbabilty(word));
            total = total + x;
        }
        double exp = total / (float) N;

        return Math.pow(Math.E, exp);
    }


    public String generateSentence() {
        String sentence = "<s>";
        Random random = new Random();
        String word;
        while (!(word = tokens.get(random.nextInt(tokens.size() - 1))).equals("</s>")) {
            if (!word.equals("<s>")) {
                sentence += " " + word;
            }
        }
        sentence += " " + word;
        return sentence;
    }

    @Override
    public String toString() {
        Iterator iterator = map.entrySet().iterator();
        String text = "";
        while (iterator.hasNext()) {
            Map.Entry pair = (Map.Entry) iterator.next();
            text += pair.getKey().toString() + ":" + pair.getValue().toString() + "\n";
        }
        return text;
    }

    private void checkForPopular(String w) {
        if (!popularTokens.contains(w)) {
            popularTokens.add(w);
            for (int i = popularTokens.size() - 1; i > 0 && map.get(popularTokens.get(i - 1)) <= map.get(popularTokens.get(i)); i--) {
                String temp = popularTokens.get(i);
                popularTokens.set(i, popularTokens.get(i - 1));
                popularTokens.set(i - 1, temp);
            }
            popularTokens.remove(popularTokens.size() - 1);
        } else {
            ArrayList<String> newPopular = new ArrayList<String>();
            while (!popularTokens.isEmpty()) {
                int i = 0;
                String topWord = null;
                for (String word : popularTokens) {
                    if (map.get(word) > i) {
                        i = map.get(word);
                        topWord = word;
                    }
                }
                popularTokens.remove(topWord);
                newPopular.add(topWord);
            }
            popularTokens = newPopular;
        }
    }
}

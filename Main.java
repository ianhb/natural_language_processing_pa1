import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Properties;

/**
 * Main class for PA1
 *
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

        ArrayList<String> upTrainTokens = preprocess(up_Train);
        ArrayList<String> downTrainTokens = preprocess(down_Train);
        Trigram upTrainTri = new Trigram();
        Bigram upTrainBi = new Bigram();
        Unigram upTrainUni = new Unigram();
        upTrainBi.makeMap(upTrainTokens);
        upTrainUni.makeMap(upTrainTokens);
        upTrainTri.makeMap(upTrainTokens);
        Trigram downTrainTri = new Trigram();
        Bigram downTrainBi = new Bigram();
        Unigram downTrainUni = new Unigram();
        downTrainBi.makeMap(downTrainTokens);
        downTrainUni.makeMap(downTrainTokens);
        downTrainTri.makeMap(downTrainTokens);
        for (String s : upTrainUni.popularTokens) {
            System.out.println(s + ":" + upTrainUni.map.get(s));
        }
        for (Bigram.WordDouble wordDouble : upTrainBi.popularTokens) {
            System.out.println(wordDouble.toString() + ":" + wordDouble.getCount());
        }
        for (Trigram.WordTriple triple : upTrainTri.popularTokens) {
            System.out.println(triple.toString() + ":" + triple.getCount());
        }
        for (String s : downTrainUni.popularTokens) {
            System.out.println(s + ":" + upTrainUni.map.get(s));
        }
        for (Bigram.WordDouble wordDouble : downTrainBi.popularTokens) {
            System.out.println(wordDouble.toString() + ":" + wordDouble.getCount());
        }
        for (Trigram.WordTriple triple : downTrainTri.popularTokens) {
            System.out.println(triple.toString() + ":" + triple.getCount());
        }
        System.out.println("Unigram Sentence: " + upTrainUni.generateSentence());
        System.out.println("Bigram Sentence: " + upTrainBi.generateSentence());
        System.out.println("Trigram Sentence: " + upTrainTri.generateSentence());
    }

    private static ArrayList<String> preprocess(String words) {
        words = words.replaceAll("\\. ", "</s> <s>");
        words = "<s> " + words;
        words = words.substring(0, words.lastIndexOf("<s>"));
        words = words.replaceAll("\\d", "");
        words = words.replaceAll("[,!?@()\\-:;.$'\\\\*~]", "");

        ArrayList<String> tokens = new ArrayList<String>();

        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props, false);
        Annotation document = pipeline.process(words);

        for (CoreMap sentence : document.get(CoreAnnotations.SentencesAnnotation.class)) {
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                String lemma = token.get(CoreAnnotations.LemmaAnnotation.class);
                tokens.add(lemma);
            }
        }
        return tokens;
    }
}

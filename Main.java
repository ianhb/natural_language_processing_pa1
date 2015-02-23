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

    public static final String UNKNOWN = "<UNK>";

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
            System.out.println("Reading training.txt");
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
        try {
            buf = new BufferedReader(new FileReader("validation.txt"));
            System.out.println("Reading validation.txt");
            while ((line = buf.readLine()) != null) {
                boolean downspeak = line.equals("DOWNSPEAK");
                buf.readLine();
                while (!(line = buf.readLine()).equals("**EOM**")) {
                    if (downspeak) {
                        down_validation += line + " ";
                    } else {
                        up_validation += line + " ";
                    }
                }
            }
            buf.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Preprocessing up train");
        ArrayList<String> upTrainTokens = preprocess(up_Train);
        System.out.println("Preprocessing down train");
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

        System.out.println("Up Train Unigram Popular Tokens");
        for (String s : upTrainUni.popularTokens) {
            System.out.println(s + ":" + upTrainUni.map.get(s));
        }
        System.out.println("Up Train Bigram Popular Tokens");
        for (Bigram.WordDouble wordDouble : upTrainBi.popularTokens) {
            System.out.println(wordDouble.toString() + ":" + wordDouble.getCount());
        }
        System.out.println("Up Train Trigram Popular Tokens");
        for (Trigram.WordTriple triple : upTrainTri.popularTokens) {
            System.out.println(triple.toString() + ":" + triple.getCount());
        }
        System.out.println("Down Train Unigram Popular Tokens");
        for (String s : downTrainUni.popularTokens) {
            System.out.println(s + ":" + upTrainUni.map.get(s));
        }
        System.out.println("Down Train Bigram Popular Tokens");

        for (Bigram.WordDouble wordDouble : downTrainBi.popularTokens) {
            System.out.println(wordDouble.toString() + ":" + wordDouble.getCount());
        }
        System.out.println("Down Train Trigram Popular Tokens");

        for (Trigram.WordTriple triple : downTrainTri.popularTokens) {
            System.out.println(triple.toString() + ":" + triple.getCount());
        }
        //System.out.println("Unigram Sentence: " + upTrainUni.generateSentence());
        //System.out.println("Bigram Sentence: " + upTrainBi.generateSentence());
        //System.out.println("Trigram Sentence: " + upTrainTri.generateSentence());
        ArrayList<String> upValidTokens = preprocess(up_validation);
        System.out.println(upTrainUni.perplexity(downTrainTokens) < downTrainUni.perplexity(downTrainTokens));
        System.out.println(upTrainBi.perplexity(downTrainTokens) < downTrainBi.perplexity(downTrainTokens));
        System.out.println(upTrainTri.perplexity(downTrainTokens) < downTrainTri.perplexity(downTrainTokens));

        System.out.println(upTrainUni.perplexity(upTrainTokens) > downTrainUni.perplexity(upTrainTokens));
        System.out.println(upTrainBi.perplexity(upTrainTokens) > downTrainBi.perplexity(upTrainTokens));
        System.out.println(upTrainTri.perplexity(upTrainTokens) > downTrainTri.perplexity(upTrainTokens));

        System.out.println(upTrainUni.perplexity(upValidTokens) > downTrainUni.perplexity(upValidTokens));
        System.out.println(upTrainBi.perplexity(upValidTokens) > downTrainBi.perplexity(upValidTokens));
        System.out.println(upTrainTri.perplexity(upValidTokens) > downTrainTri.perplexity(upValidTokens));
    }

    private static ArrayList<String> preprocess(String words) {
        words = words.replaceAll("\\. ", "</s> <s>");
        words = "<s> " + words;
        words = words.substring(0, words.lastIndexOf("<s>"));
        words = words.replaceAll("\\d", "");
        words = words.replaceAll("[/,!?@()&\\-:;.$'\\\\*~]", "");

        ArrayList<String> tokens = new ArrayList<String>();

        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props, false);
        Annotation document = pipeline.process(words);

        for (CoreMap sentence : document.get(CoreAnnotations.SentencesAnnotation.class)) {
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                String word = token.get(CoreAnnotations.TextAnnotation.class);
                tokens.add(word);
            }
        }
        return tokens;
    }

    private static ArrayList<String> preprocess2(String words) {
        words = words.replaceAll("\\. ", "</s> <s>");
        words = "<s> " + words;
        words = words.substring(0, words.lastIndexOf("<s>"));
        words = words.replaceAll("\\d", "");
        words = words.replaceAll("[,!?@()&\\-:;.$'\\\\*~]", "");

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

    private float goodTuring(Unigram unigram, String string) {
        return unigram.goodTuring(string, unigram.prepgoodTuring());
    }

    private float bigramInterpolation(String first, String second, Unigram unigram, Bigram bigram) {
        return (1 / 2) * bigram.unsmoothedProbability(first, second) + (1 / 2) * unigram.unsmoothedProbability(second);
    }

    private float trigramInterpolation(String first, String second, String third, Unigram unigram, Bigram bigram, Trigram trigram) {
        return (1 / 3) * trigram.unsmoothedProbability(first, second, third) + (1 / 3) * bigram.unsmoothedProbability(second, third) + (1 / 3) * unigram.unsmoothedProbability(third);
    }
}

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

import java.io.*;
import java.util.ArrayList;
import java.util.Properties;

/**
 * Main class for PA1
 *
 * Created by Ian on 2/19/2015.
 */
public class Main {

    public static final String UNKNOWN = "<UNK>";
    public static Properties lemmaProps = new Properties();
    public static Properties tokenProps = new Properties();
    public static StanfordCoreNLP lemmaPipeline;
    public static StanfordCoreNLP tokenPipeline;


    public static void main(String[] argv) {
        System.out.println("Setting up tokenizer");
        tokenProps.put("annotators", "tokenize, ssplit, pos");
        lemmaProps.put("annotators", "tokenize, ssplit, pos, lemma");
        tokenPipeline = new StanfordCoreNLP(tokenProps, false);
        lemmaPipeline = new StanfordCoreNLP(lemmaProps, false);
        BufferedReader buf;
        String line;
        String up_Train = "";
        String down_Train = "";
        String up_validation = "";
        String down_validation = "";
        ArrayList<Email> upDownTest = new ArrayList<Email>();
        ArrayList<Email> validEmails = new ArrayList<Email>();

        // read validation text to string
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


        // read validation text to string
        try {
            buf = new BufferedReader(new FileReader("validation.txt"));
            System.out.println("Reading validation.txt");
            while ((line = buf.readLine()) != null) {
                String message = "";
                boolean downspeak = line.equals("DOWNSPEAK");
                buf.readLine();
                while (!(line = buf.readLine()).equals("**EOM**")) {
                    if (downspeak) {
                        down_validation += line + " ";
                    } else {
                        up_validation += line + " ";
                    }
                    message += line;
                }
                validEmails.add(new Email(downspeak, preprocess2(message)));
            }
            buf.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // read test text to string
        try {
            buf = new BufferedReader(new FileReader("test.txt"));
            System.out.println("Reading text.txt");
            while ((line = buf.readLine()) != null) {
                buf.readLine();
                String message = "";
                while (!(line = buf.readLine()).equals("**EOM**")) {
                    message += line;
                }
                upDownTest.add(new Email(preprocess2(message)));
            }
            buf.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


        //process text to tokens
        System.out.println("Preprocessing up train");
        ArrayList<String> upTrainTokens = preprocess2(up_Train);
        System.out.println("Preprocessing down train");
        ArrayList<String> downTrainTokens = preprocess2(down_Train);
        System.out.println("Preprocessing up validation");
        ArrayList<String> upValidTokens = preprocess2(up_validation);
        System.out.println("Preprocessing down validation");
        ArrayList<String> downValidTokens = preprocess2(down_validation);

        //Make upTraining n-grams
        Trigram upTrainTri = new Trigram();
        Bigram upTrainBi = new Bigram();
        Unigram upTrainUni = new Unigram();
        upTrainBi.makeMap(upTrainTokens);
        upTrainUni.makeMap(upTrainTokens);
        upTrainTri.makeMap(upTrainTokens);
        LanguageModel upSpeak = new LanguageModel(upTrainUni, upTrainBi, upTrainTri);

        //Make downTraining n-grams
        Trigram downTrainTri = new Trigram();
        Bigram downTrainBi = new Bigram();
        Unigram downTrainUni = new Unigram();
        downTrainBi.makeMap(downTrainTokens);
        downTrainUni.makeMap(downTrainTokens);
        downTrainTri.makeMap(downTrainTokens);
        LanguageModel downSpeak = new LanguageModel(downTrainUni, downTrainBi, downTrainTri);


        System.out.println();
        System.out.println();

        // print the most popular n-grams in each model
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
            System.out.println(s + ":" + downTrainUni.map.get(s));
        }
        System.out.println("Down Train Bigram Popular Tokens");

        for (Bigram.WordDouble wordDouble : downTrainBi.popularTokens) {
            System.out.println(wordDouble.toString() + ":" + wordDouble.getCount());
        }
        System.out.println("Down Train Trigram Popular Tokens");

        for (Trigram.WordTriple triple : downTrainTri.popularTokens) {
            System.out.println(triple.toString() + ":" + triple.getCount());
        }

        System.out.println();
        System.out.println();

        // print random sentences
        System.out.println("Random Unigram Sentence");
        System.out.println(downTrainUni.generateSentence());
        System.out.println(downTrainUni.generateSentence());
        System.out.println(downTrainUni.generateSentence());
        System.out.println(downTrainUni.generateSentence());
        System.out.println(downTrainUni.generateSentence());
        System.out.println("Random Bigram Sentence");
        System.out.println(downTrainBi.generateSentence());
        System.out.println(downTrainBi.generateSentence());
        System.out.println(downTrainBi.generateSentence());
        System.out.println(downTrainBi.generateSentence());
        System.out.println(downTrainBi.generateSentence());
        /*
        System.out.println("Random Trigram Sentence");
        System.out.println(upTrainTri.generateSentence(upTrainBi, upTrainUni));
        System.out.println(upTrainTri.generateSentence(upTrainBi, upTrainUni));
        System.out.println(upTrainTri.generateSentence(upTrainBi, upTrainUni));
        System.out.println(upTrainTri.generateSentence(upTrainBi, upTrainUni));
        System.out.println(upTrainTri.generateSentence(upTrainBi, upTrainUni));
        */

        System.out.println();
        System.out.println();


        //print the perplexity of trigrams
        System.out.println("UPTRAIN TRIGRAM PERPLEXITY: " + upTrainTri.perplexity(upValidTokens));
        System.out.println("DOWNTRAIN TRIGRAM PERPLEXITY: " + downTrainTri.perplexity(downValidTokens));

        // print the perplexity of bigrams
        System.out.println("UPTRAIN BIGRAM PERPLEXITY: " + upTrainBi.perplexity(upValidTokens));
        System.out.println("DOWNTRAIN BIGRAM PERPLEXITY: " + downTrainBi.perplexity(downValidTokens));

        // print the perplexity of unigrams
        System.out.println("UPTRAIN UNIGRAM PERPLEXITY: " + upTrainUni.perplexity(upValidTokens));
        System.out.println("DOWNTRAIN UNIGRAM PERPLEXITY: " + downTrainUni.perplexity(downValidTokens));

        System.out.println();
        System.out.println();

        checkValidEmails(validEmails, downSpeak, upSpeak);
        processTestEmails(upDownTest, downSpeak, upSpeak);

    }

    /**
     * Processes a string of words to an arraylist of strings representing tokens
     * order is preserved and numbers and punctuation are filtered out
     * ". " is interpreted as the end of a sentence and is processed as such.
     *
     * @param words the initial text to be tokenized
     * @return an arraylist of strings make tokens from words
     */
    private static ArrayList<String> preprocess(String words) {
        words = words.replaceAll("\\. ", "</s> <s>");
        words = "<s> " + words + "</s>";
        if (words.lastIndexOf("<s>") != 0) {
            words = words.substring(0, words.lastIndexOf("<s>"));
        }
        words = words.replaceAll("\\d", "");
        words = words.replaceAll("[,!?@()&\\-:;.$'\\\\*~]", "");
        ArrayList<String> tokens = new ArrayList<String>();

        Annotation document = tokenPipeline.process(words);

        for (CoreMap sentence : document.get(CoreAnnotations.SentencesAnnotation.class)) {
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                String word = token.get(CoreAnnotations.TextAnnotation.class);
                tokens.add(word);
            }
        }
        return tokens;
    }

    /**
     * Processes a string of words to an arraylist of strings representing tokens
     * order is preserved and numbers and punctuation are filtered out
     * ". " is interpreted as the end of a sentence and is processed as such.
     * Tokens are matched to their lemma form as opposed to strict form
     *
     * @param words the initial text to be tokenized
     * @return an arraylist of strings make tokens from words
     */
    private static ArrayList<String> preprocess2(String words) {
        words = words.replaceAll("\\. ", "</s> <s>");
        words = "<s> " + words + "</s>";
        if (words.lastIndexOf("<s>") != 0) {
            words = words.substring(0, words.lastIndexOf("<s>"));
        }
        words = words.replaceAll("\\d", "");
        words = words.replaceAll("[,!?@`#()&\\-:;.$'\\\\*~]", "");

        ArrayList<String> tokens = new ArrayList<String>();

        Annotation document = lemmaPipeline.process(words);

        for (CoreMap sentence : document.get(CoreAnnotations.SentencesAnnotation.class)) {
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                String lemma = token.get(CoreAnnotations.LemmaAnnotation.class);
                tokens.add(lemma);
            }
        }
        return tokens;
    }

    /**
     * Test procedure to determine the accuracy of the model
     *
     * @param emails an arraylist of emails from the validation set
     * @param down   a language model made from emails to employees
     * @param up     a language model made from emails to supervisors
     */
    private static void checkValidEmails(ArrayList<Email> emails, LanguageModel down, LanguageModel up) {
        int score = 0;
        int n = 0;
        for (Email email : emails) {
            n++;
            ArrayList<String> text = email.text;
            boolean downSpeak = (down.perplexity(text) < up.perplexity(text));
            if (downSpeak == email.downspeak) {
                score++;
            }
        }
        System.out.println("Predicted " + score + " correctly out of " + n + " for a percentage of " + (float) score / n);
    }

    /**
     * Outputs the results of the language model's predictions for the test set
     *
     * @param emails an arraylist of emails from the test set
     * @param down   a language model made from emails to employees
     * @param up     a language model made from emails to supervisors
     */
    private static void processTestEmails(ArrayList<Email> emails, LanguageModel down, LanguageModel up) {
        int n = 0;
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File("output.txt")));
            bufferedWriter.write("Id,Prediction \n");
            for (Email email : emails) {
                n++;
                ArrayList<String> text = email.text;
                boolean downSpeak = (down.perplexity(text) < up.perplexity(text));
                if (downSpeak) {
                    bufferedWriter.write(n + "," + 0 + "\n");
                } else {
                    bufferedWriter.write(n + "," + 1 + "\n");
                }
            }
            bufferedWriter.flush();
            bufferedWriter.close();
            System.out.println("Test File Classified");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}

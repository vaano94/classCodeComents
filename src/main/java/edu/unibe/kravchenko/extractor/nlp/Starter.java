package edu.unibe.kravchenko.extractor.nlp;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class Starter {

    public static final String REPLACE_REGEXP = "[^a-zA-Z\\.\\ ]+"; // dot saved

    public static void main(String[] args) throws IOException {

//        CSVParser parser = new CSVParser(new FileReader("C:\\Users\\vaano\\IdeaProjects\\ClassCodeComments\\src\\main\\resources\\test.csv"), CSVFormat.DEFAULT);
        CSVParser parser = new CSVParser(new FileReader("C:\\Users\\vaano\\IdeaProjects\\ClassCodeComments\\src\\main\\resources\\guava.csv"), CSVFormat.DEFAULT);

        List<CSVRecord> records = parser.getRecords();

        StanfordCoreNLP pipeline = Analyzer.getAnalyzer();

        records = records.subList(1, records.size());

        int totalRate = 0;

        for (CSVRecord r : records) {

            String fromCsv = r.get(4);
            String replaced = fromCsv.replaceAll(REPLACE_REGEXP, " ").replaceAll("\\n", " ")
//                    .split("\\.")
                ;
//            for (String rep : replaced) {

                Annotation document = new Annotation(replaced);
                // run all Annotators on this text
                pipeline.annotate(document);

                List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
                for (CoreMap sentence: sentences) {
//                    CoreMap sentence = sentences.get(0);
                    Tree tree = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
                    int score = RNNCoreAnnotations.getPredictedClass(tree);
                    System.out.println("Sentiment score: " + score);
                    totalRate = totalRate + (score - 2);

                    List<CoreLabel> coreLabels = sentence.get(CoreAnnotations.TokensAnnotation.class);
                    for (int i = 0; i < coreLabels.size(); i++) {
                        CoreLabel coreLabel = coreLabels.get(i);
                        String partOfSpeech = coreLabel.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                        String nameEntityTag = coreLabel.get(CoreAnnotations.NamedEntityTagAnnotation.class);
                        System.out.println(partOfSpeech);
                    }
                }

            }

//        }

    }

}

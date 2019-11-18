package edu.unibe.kravchenko.extractor.nlp;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;

import java.util.Properties;

public class Analyzer {

     public static StanfordCoreNLP INSTANCE = null;

     static StanfordCoreNLP getAnalyzer() {
        if (INSTANCE == null) {
            Properties props = new Properties();

        props.setProperty("annotators", "tokenize, ssplit, pos, parse, sentiment, lemma, ner, dcoref");
//            props.setProperty("annotators", "tokenize, ssplit, pos");

            StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
            INSTANCE = pipeline;
            return pipeline;
        } else {
            return INSTANCE;
        }
    }

}

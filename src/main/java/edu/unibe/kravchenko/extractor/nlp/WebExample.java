package edu.unibe.kravchenko.extractor.nlp;

import edu.stanford.nlp.pipeline.StanfordCoreNLPServer;

import java.io.IOException;
import java.util.Properties;

public class WebExample {

    public static void main(String[] args) throws IOException {
        Properties props = new Properties();

        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");

        StanfordCoreNLPServer pipeline = new StanfordCoreNLPServer();

    }
}

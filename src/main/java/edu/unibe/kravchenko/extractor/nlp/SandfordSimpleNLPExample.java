package edu.unibe.kravchenko.extractor.nlp;

import edu.stanford.nlp.coref.CorefCoreAnnotations;
import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.AnnotationPipeline;
import edu.stanford.nlp.pipeline.MorphaAnnotator;
import edu.stanford.nlp.pipeline.POSTaggerAnnotator;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.pipeline.TokenizerAnnotator;
import edu.stanford.nlp.pipeline.WordsToSentencesAnnotator;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.time.TimeAnnotator;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class SandfordSimpleNLPExample {

    public static AnnotationPipeline buildPipeline() {
        AnnotationPipeline pl = new AnnotationPipeline();
        pl.addAnnotator(new TokenizerAnnotator(false));
        pl.addAnnotator(new WordsToSentencesAnnotator(false));
        pl.addAnnotator(new POSTaggerAnnotator(false));
        pl.addAnnotator(new MorphaAnnotator(false));
//        pl.addAnnotator(new TimeAnnotator("sutime", props));
//        pl.addAnnotator(new PhraseAnnotator(phrasesFile, false));
        return pl;
    }

    public static void main( String[] args )

    {

        // creates a StanfordCoreNLP object, with POS tagging, lemmatization,

        // NER, parsing, and coreference resolution

        Properties props = new Properties();

        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");

        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        // read some text in the text variable

        String text = "Karma of humans is AI";

        // create an empty Annotation just with the given text

        Annotation document = new Annotation(text);

        // run all Annotators on this text

        pipeline.annotate(document);



        // these are all the sentences in this document

        List <CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);

        List<String> words = new ArrayList<>();

        List<String> posTags = new ArrayList<>();

        List<String> nerTags = new ArrayList<>();

        for (CoreMap sentence : sentences) {

            // traversing the words in the current sentence

            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {

                // this is the text of the token

                String word = token.get(CoreAnnotations.TextAnnotation.class);

                words.add(word);

                // this is the POS tag of the token

                String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);

                posTags.add(pos);

                // this is the NER label of the token

                String ne = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);

                nerTags.add(ne);

            }

            // This is the syntactic parse tree of sentence

            Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);

            System.out.println("Tree:\n"+ tree);

            // This is the dependency graph of the sentence

            SemanticGraph dependencies = sentence.get(SemanticGraphCoreAnnotations.CollapsedDependenciesAnnotation.class);

            System.out.println("Dependencies\n:"+ dependencies);

        }

        System.out.println("Words: " + words.toString());

        System.out.println("posTags: " + posTags.toString());

        System.out.println("nerTags: " + nerTags.toString());

        // This is a map of the chain

        Map<Integer, CorefChain> graph = document.get(CorefCoreAnnotations.CorefChainAnnotation.class);

        System.out.println("Map of the chain:\n" + graph);

        System.out.println( "End of Processing" );

        AnnotationPipeline pipeline1 = buildPipeline();
        Annotation annotation = new Annotation("It's like a topography that is made from cartography of me.");
        pipeline.annotate(annotation);
        System.out.println("end end");

    }

}
package myproject.icarus;

import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;

import java.util.List;
import java.util.Properties;

public class SentimentAnalyser
{
    private static StanfordCoreNLP pipeline;

    // Call this once at startup — it's slow to initialise (~5 seconds)
    public static void init()
    {
        if (pipeline != null) return;   // already initialised, don't reload
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
        pipeline = new StanfordCoreNLP(props);
        System.out.println("NLP pipeline ready.");
    }

    /**
     * Analyses a single comment string.
     * Returns: 2 = Positive, 1 = Neutral, 0 = Negative
     */
    public static int analyseComment(String text)
    {
        if (text == null || text.equals("null") || text.trim().isEmpty())
            return 1; // treat empty/null as neutral

        // Truncate long comments — 300 chars is enough for sentiment
        if (text.length() > 300)
            text = text.substring(0, 300);

        Annotation annotation = new Annotation(text);
        pipeline.annotate(annotation);

        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);

        int total = 0;
        int count = 0;
        for (CoreMap sentence : sentences)
        {
            String sentiment = sentence.get(SentimentCoreAnnotations.SentimentClass.class);
            total += convertSentimentToScore(sentiment);
            count++;
        }

        if (count == 0) return 1;
        return (int) Math.round((double) total / count);
    }

    /**
     * Converts CoreNLP's label to a number.
     * CoreNLP returns: Very negative, Negative, Neutral, Positive, Very positive
     */
    private static int convertSentimentToScore(String sentiment)
    {
        return switch (sentiment)
        {
            case "Very positive" -> 2;
            case "Positive"      -> 2;
            case "Neutral"       -> 1;
            case "Negative"      -> 0;
            case "Very negative" -> 0;
            default              -> 1;
        };
    }

    /**
     * Analyses all comments on a video and returns an average sentiment score.
     * Score is between 0.0 (very negative) and 2.0 (very positive).
     * Only analyses first 50 comments to save memory and speed up processing.
     */
    public static double analyseVideo(VideoData video)
    {
        if (video.comments.isEmpty()) return 1.0; // neutral default

        double total = 0;
        int count = 0;
        int limit = Math.min(video.comments.size(), 20); // max 50 comments per video

        for (int i = 0; i < limit; i++)
        {
            total += analyseComment(video.comments.get(i).textOriginal);
            count++;
        }
        return total / count;
    }
}
package myproject.icarus;

import java.util.Comparator;
import java.util.List;

public class RecommendationScorer
{
    // Scoring weights — must add up to 1.0
    // Adjust these to change how much each factor matters
    static final double WEIGHT_VIEWS     = 0.35;
    static final double WEIGHT_LIKES     = 0.25;
    static final double WEIGHT_COMMENTS  = 0.15;
    static final double WEIGHT_SENTIMENT = 0.25;

    /**
     * Computes a composite recommendation score for each video
     * and sorts the list so the best recommendation is first.
     */
    public static void rankVideos(List<VideoData> videos)
    {
        if (videos.isEmpty()) return;

        // Step 1 — find max values so we can normalise everything to 0.0–1.0
        double maxViews    = videos.stream().mapToDouble(v -> v.viewCount).max().orElse(1);
        double maxLikes    = videos.stream().mapToDouble(v -> v.likeCount).max().orElse(1);
        double maxComments = videos.stream().mapToDouble(v -> v.commentCount).max().orElse(1);

        // Step 2 — compute score for each video
        for (VideoData v : videos)
        {
            double normViews     = v.viewCount     / maxViews;
            double normLikes     = v.likeCount     / maxLikes;
            double normComments  = v.commentCount  / maxComments;
            double normSentiment = v.sentimentScore / 2.0;   // sentimentScore is 0–2, bring to 0–1

            v.recommendationScore = (WEIGHT_VIEWS     * normViews)
                    + (WEIGHT_LIKES     * normLikes)
                    + (WEIGHT_COMMENTS  * normComments)
                    + (WEIGHT_SENTIMENT * normSentiment);
        }

        // Step 3 — sort descending, best score first
        videos.sort(Comparator.comparingDouble((VideoData v) -> v.recommendationScore).reversed());
    }

    /**
     * Prints the ranked results to console in a clean readable format.
     */
    public static void printRankedResults(List<VideoData> videos)
    {
        System.out.println("\n========== TERO RECOMMENDATIONS ==========");
        for (int i = 0; i < videos.size(); i++)
        {
            VideoData v = videos.get(i);
            System.out.printf(
                    "#%-2d | Score: %.3f | Sentiment: %.2f | Views: %-9d | Likes: %-7d%n" +
                            "     %s%n" +
                            "     Channel: %s%n%n",
                    i + 1,
                    v.recommendationScore,
                    v.sentimentScore,
                    v.viewCount,
                    v.likeCount,
                    v.videoTitle,
                    v.channel.channelName
            );
        }
        System.out.println("==========================================");
    }
}
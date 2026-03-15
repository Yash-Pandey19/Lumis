package myproject.icarus;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


/**
 * Class to contain Constant Values used in the program for easier modifications for later.
 */
class constValues
{
    //static final String API_KEY = "";      //First Account
    static final String API_KEY = "AIzaSyDdcj4svUJPmEK69sECXA58eb4mktA5rRc";        //Second Account
}

/**
 * Default parameters for the search function.
 */
class defaultSearchValues
{
    static int maxResults = 50;         //Number of items to return (5 to 50)
    static int pageCount = 2;           //Reduced from 4 to 2 for speed
    static String order = "relevance";
    static String relevanceLanguage = "en";
    static String type = "video";
    static String pubAfter = "1970-01-01T00:00:00Z";
    static filterMode currentFilter = filterMode.UNIQUE_CHANNELS;
}

/**
 * Parameters used when searching for comments.
 * Number of Comments retrieved -> <= maxResults*pageCount
 */
class commentDefaultValues
{
    static int maxResults = 50;
    static int pageCount = 1;           //Reduced from 2 to 1 for speed
    static String moderationStatus = "published";
    static String order = "time";
    static String textFormat = "html";
}

public class YoutubeApiClient
{
    /**
     * @param query Query to search YouTube for.
     * @return Filtered, sentiment-scored, ranked List of VideoData objects.
     */
    public static List<VideoData> search(String query) throws IOException, URISyntaxException
    {
        String nextPageToken = "null";
        List<VideoData> videos = new ArrayList<>();
        String searchUrl = "null";
        String response = "null";

        // ── Phase 1 : Collect videos across pages ───────────────────────────
        for (int i = 0; i < defaultSearchValues.pageCount; i++)
        {
            searchUrl = ApiUrlCreator.searchApiFunctionUrl(query, nextPageToken);
            response = SendApiRequest.sendGetRequest(searchUrl);
            if (response.startsWith("Error"))
            {
                System.out.println(response);
                return videos;
            }
            videos.addAll(JSONResponseParser.parseSearchResponsePage(response));
            nextPageToken = JSONResponseParser.getNextPageToken(response);
            if (nextPageToken.equals("null"))
                break;
        }

        // ── Phase 2 : Filter duplicate channels ─────────────────────────────
        switch (defaultSearchValues.currentFilter)
        {
            case UNIQUE_CHANNELS -> FilterLists.filterUniqueChannels(videos);
        }

        // ── Phase 3 : Init NLP once, then process all videos in parallel ─────
        SentimentAnalyser.init();

        ExecutorService executor = Executors.newFixedThreadPool(4);
        List<Future<?>> futures = new ArrayList<>();

        for (VideoData v : videos)
        {
            // Skip any video that came through with no valid ID (bug fix)
            if (v.videoId == null || v.videoId.equals("null"))
            {
                System.out.println("Skipping video with null ID: " + v.videoTitle);
                continue;
            }

            futures.add(executor.submit(() ->
            {
                try
                {
                    boolean success = completeVideoData(v);
                    if (!success)
                    {
                        System.out.println("Error completing: " + v.videoTitle);
                        return;
                    }

                    // ── Phase 4 : Run NLP on this video's comments ───────────
                    v.sentimentScore = SentimentAnalyser.analyseVideo(v);
                    System.out.println("Sentiment [" + String.format("%.2f", v.sentimentScore)
                            + "] \u2192 " + v.videoTitle);
                }
                catch (Exception e)
                {
                    System.out.println("Thread error for: " + v.videoTitle
                            + " — " + e.getMessage());
                }
            }));
        }

        // Wait for all threads to finish before ranking
        for (Future<?> f : futures)
        {
            try { f.get(); }
            catch (Exception e) { System.out.println("Future error: " + e.getMessage()); }
        }
        executor.shutdown();

        // ── Phase 5 : Score and rank all videos ─────────────────────────────
        RecommendationScorer.rankVideos(videos);
        RecommendationScorer.printRankedResults(videos);

        return videos;
    }

    /**
     * Completes a partially initialised VideoData object with full stats and comments.
     * @param v VideoData object to complete.
     * @return true if successful, false if any API call failed.
     */
    private static boolean completeVideoData(VideoData v) throws IOException, URISyntaxException
    {
        // Fetch full video statistics
        String videoUrl = ApiUrlCreator.VideoApiFunctionUrl(v.videoId);
        String videoResponse = SendApiRequest.sendGetRequest(videoUrl);
        if (videoResponse.startsWith("Error"))
        {
            System.out.println(videoResponse);
            return false;
        }
        JSONResponseParser.parseToVideoData(v, videoResponse);

        // Fetch comments
        String pageToken = "null";
        String commentsUrl;
        String commentResponse;

        for (int i = 0; i < commentDefaultValues.pageCount; i++)
        {
            commentsUrl = ApiUrlCreator.commentThreadApiPageTokenUrl(v.videoId, pageToken);
            commentResponse = SendApiRequest.sendGetRequest(commentsUrl);
            if (commentResponse.startsWith("Error"))
            {
                System.out.println(commentResponse);
                return false;
            }
            v.comments.addAll(JSONResponseParser.parseCommentThreadResponse(commentResponse));
            pageToken = JSONResponseParser.getNextPageToken(commentResponse);
            if (pageToken.equals("null"))
                break;
        }
        return true;
    }
}

/**
 * Enum for different filter modes (used in YoutubeApiClient.search).
 */
enum filterMode
{
    UNIQUE_CHANNELS,
    NOTHING,
}
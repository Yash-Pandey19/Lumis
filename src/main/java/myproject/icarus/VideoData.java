package myproject.icarus;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to imitate a YouTube video with its data.
 */
public class VideoData
{
    Channel channel;
    String videoTitle;
    String videoId;
    String publishedAt;
    String description;
    String thumbnailUrl;
    String videoUrl;
    String audioLanguage;
    int likeCount;
    double sentimentScore;        // 0.0 = negative, 1.0 = neutral, 2.0 = positive
    double recommendationScore;   // final composite score — higher = better recommendation
    int viewCount;
    int commentCount;
    List<CommentData> comments;

    /**
     * Initializes the object with invalid values.
     */
    VideoData()
    {
        videoTitle = "null";
        videoId = "null";
        publishedAt = "null";
        description = "null";
        thumbnailUrl = "null";
        videoUrl = "null";
        audioLanguage = "null";
        likeCount = -1;
        viewCount = -1;
        commentCount = -1;
        sentimentScore = 1.0;
        recommendationScore = 0.0;
        channel = new Channel();
        comments = new ArrayList<CommentData>();
    }

    // ── Getters required by JavaFX PropertyValueFactory ─────────────
    public String getVideoTitle()          { return videoTitle; }
    public String getVideoId()             { return videoId; }
    public String getPublishedAt()         { return publishedAt; }
    public String getDescription()         { return description; }
    public String getThumbnailUrl()        { return thumbnailUrl; }
    public String getAudioLanguage()       { return audioLanguage; }
    public int    getLikeCount()           { return likeCount; }
    public int    getViewCount()           { return viewCount; }
    public int    getCommentCount()        { return commentCount; }
    public double getSentimentScore()      { return sentimentScore; }
    public double getRecommendationScore() { return recommendationScore; }
    public Channel getChannel()            { return channel; }
    public String getVideoUrl() { return videoUrl; }

    /**
     * Overridden toString to print values to console.
     * @return String with VideoData values.
     */
    @Override
    public String toString()
    {
        return  "Video Title : " + videoTitle +
                "\nChannel Name : " + channel.channelName +
                "\nLikes : " + likeCount +
                "\nViews : " + viewCount +
                "\nComment Count : " + commentCount +
                "\nUpload Date : " + publishedAt +
                "\nThumbnail : " + thumbnailUrl +
                "\nLanguage : " + audioLanguage +
                "\nSentiment Score : " + sentimentScore +
                "\nRecommendation Score : " + recommendationScore+
                "\nVideo URL : " + videoUrl;
    }
}

import java.time.Duration;
import java.time.Duration;

public class customYoutubeVideo {


    protected String title;
    protected String videoID;
    protected String playlistItemID;
    protected Duration length;

    public customYoutubeVideo(){
        title = null;
        videoID = null;
        playlistItemID = null;
        length = null;
    }
    public customYoutubeVideo(String title, String ID,String playlistItemID, Duration length){
        this.title = title;
        this.videoID = ID;
        this.playlistItemID = playlistItemID;
        this.length = length;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getvideoID() {
        return videoID;
    }

    public void setID(String ID) {
        this.videoID = ID;
    }

    public String getPlaylistItemID() {
        return playlistItemID;
    }

    public void setPlaylistItemID(String playlistItemID) {
        this.playlistItemID = playlistItemID;
    }
    public Duration getLength() {
        return length;
    }

    public void setLength(Duration length) {
        this.length = length;
    }

    @Override
    public String toString() {
        return "customYoutubeVideo{" +
                "title='" + title + '\'' +
                ", ID='" + videoID + '\'' +
                ", length=" + length +
                '}';
    }
}

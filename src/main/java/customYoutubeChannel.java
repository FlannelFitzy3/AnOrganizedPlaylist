import java.util.ArrayList;


public class customYoutubeChannel {
    protected String name;
    protected String channelID;
    protected String catagory;
    protected ArrayList<customYoutubeVideo> channelVideos;

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getChannelID() {
        return channelID;
    }
    public void setChannelID(String channelID) {
        this.channelID = channelID;
    }
    public String getCatagory() {
        return catagory;
    }
    public void setCatagory(String catagory) {
        this.catagory = catagory;
    }
    public ArrayList<customYoutubeVideo> getChannelVideos() {
        return channelVideos;
    }
    public void setChannelVideos(ArrayList<customYoutubeVideo> channelVideos) {
        this.channelVideos = channelVideos;
    }





}

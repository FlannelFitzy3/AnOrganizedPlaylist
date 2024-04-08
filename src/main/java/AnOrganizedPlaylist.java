/**
 * Sample Java code for youtube.channels.list
 * See instructions for running these code samples locally:
 * https://developers.google.com/explorer-help/guides/code_samples#java
 */

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
//import com.google.api.client.json.jackson2.JacksonFactory;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;
import com.sun.org.apache.xml.internal.utils.res.XResourceBundle;
import org.checkerframework.checker.units.qual.A;
import org.json.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.util.*;


public class WatchLaterOrganizer {
    private static final String CLIENT_SECRETS= "client_secret.json";
    private static final Collection<String> SCOPES = Arrays.asList("https://www.googleapis.com/auth/youtube");
    private static final String APPLICATION_NAME = "API code samples";
    private static final GsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    /**
     * Create an authorized Credential object.
     *
     * @return an authorized Credential object.
     * @throws IOException
     * copied from API documentation
     */
    public static Credential authorize(final NetHttpTransport httpTransport) throws IOException {
        // Load client secrets.
        InputStream in = WatchLaterOrganizer.class.getResourceAsStream(CLIENT_SECRETS);
        GoogleClientSecrets clientSecrets;
        Credential credential = null;
        if (in != null) {
            clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
            // Build flow and trigger user authorization request.
            GoogleAuthorizationCodeFlow flow =
                    new GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
                            .build();

            credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
        }

        return credential;
    }

    /**
     * Build and return an authorized API client service.
     *
     * @return an authorized API client service
     * @throws GeneralSecurityException, IOException
     * copied from API documentation
     */
    public static YouTube getService() throws GeneralSecurityException, IOException {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        Credential credential = authorize(httpTransport);
        return new YouTube.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    /**
     * Call function to create API service object. Define and
     * execute API request. Print API response.
     *
     * @throws GeneralSecurityException, IOException, GoogleJsonResponseException
     */
    public static void main(String[] args) throws Exception {
        YouTube youtubeService = getService();
        ArrayList <Playlist> playlists = (ArrayList<Playlist>) getPlayLists(youtubeService);
        ArrayList<customYoutubeVideo> myYoutubeVideos = new ArrayList<>();
        if(!doesOrganizedPlaylistExist(playlists)){
            createOrganizedPlaylist(youtubeService);
        }

        String playlistID = getOrganizedPlaylistId(playlists);
        YouTube.PlaylistItems.List PlaylistRequest = youtubeService.playlistItems()
                .list("snippet,contentDetails").setMaxResults(WLOConstants.MAX_RESULTS);

        if(!playlistID.equals("null"))
        {/*
            YouTube.Subscriptions.List subscriptionsRequest = youtubeService.subscriptions()
                    .list("snippet,contentDetails")
                    .setMaxResults(200L);
            SubscriptionListResponse subscriptionsResponse = subscriptionsRequest
                    .setMaxResults(200L)
                    .setMine(true)
                    .execute();
            System.out.println(subscriptionsResponse);
            ArrayList<String> channelNames = getChannelNames(subscriptionsResponse);
            ArrayList<String> channelIDs = getChannelIDs(subscriptionsResponse);
            for (int i=0;i<channelIDs.size();i++){
                System.out.println("Channel Name: " + channelNames.get(i) + "\n" + "Channel ID: " + channelIDs.get(i));
            }
            System.out.println(channelIDs.size());
            */
            //main playlist organization
            //------------------------------------------------------------------------------------
            PlaylistItemListResponse playListResponse = PlaylistRequest.setPlaylistId(playlistID).execute();
            System.out.println(playListResponse.toString());
            ArrayList<String> titles =  getVideoTitles(playListResponse);
            ArrayList<String> videoIDs = getVideoIds(playListResponse);
            ArrayList<String> playlistItemIDs = getPlaylistItemIDs(playListResponse);
            String videoIDsAsString = "";

            for (int i=0;i<titles.size();i++){

                System.out.println("Video " + (i+1));
                System.out.println(titles.get(i));
                System.out.println(videoIDs.get(i));
                System.out.println(playlistItemIDs.get(i));

                if(i==0){
                   //System.out.println("Adding first video ID");
                    videoIDsAsString += videoIDs.get(i);
                }else {
                  // System.out.println("Adding " + i + " video ID");
                    videoIDsAsString += "," + videoIDs.get(i);
                }
            }

            YouTube.Videos.List VideoRequest = youtubeService.videos()
                    .list("snippet,contentDetails");
            System.out.println("requesting video");
            VideoListResponse VideoResponse = VideoRequest.setId(videoIDsAsString).execute();

            ArrayList<Duration> videoDurations = getVideoDurations(VideoResponse);
            System.out.println(videoDurations);

            //creating youtube video instances for sorting
            for (int i=0;i<titles.size();i++) {
                customYoutubeVideo currentVideo = new customYoutubeVideo(titles.get(i),videoIDs.get(i),playlistItemIDs.get(i),videoDurations.get(i));
                myYoutubeVideos.add(currentVideo);
            }

            sortByDuration(myYoutubeVideos,0,myYoutubeVideos.size()-1);
            PlaylistItem playlistItem = new PlaylistItem();

            // Add the id string property to the PlaylistItem object.
            for(int i=0 ;i<myYoutubeVideos.size();i++ ){
                String currentPlaylistItemID = myYoutubeVideos.get(i).getPlaylistItemID();

                playlistItem.setId(currentPlaylistItemID);

                // Add the snippet object property to the PlaylistItem object.
                PlaylistItemSnippet snippet = new PlaylistItemSnippet();
                snippet.setPlaylistId(playlistID);
                snippet.setPosition((long)i);
                ResourceId resourceId = new ResourceId();
                resourceId.setKind("youtube#video");
                String currentPlaylistVideoID = myYoutubeVideos.get(i).getvideoID();
                resourceId.setVideoId(currentPlaylistVideoID);
                snippet.setResourceId(resourceId);
                playlistItem.setSnippet(snippet);

                // Define and execute the API request
                YouTube.PlaylistItems.Update request = youtubeService.playlistItems()
                        .update("snippet", playlistItem);
                PlaylistItem response = request.execute();
                //System.out.println(response);
            }

        } else {
            System.out.println("PlaylistID cannot be 'null'.");
        }
    }

    private static ArrayList<String> getChannelNames(SubscriptionListResponse subscriptionsResponse) {
        ArrayList<String> channelNames = new ArrayList<>();
        String jsonString = subscriptionsResponse.toString();
        JSONObject obj = new JSONObject(jsonString);
        JSONArray channelArray = obj.getJSONArray("items");
        if(channelArray.length()>0)
        {
            for (int i = 0; i < channelArray.length(); i++)
            {
                String currentPlayListItemID = channelArray.getJSONObject(i).getJSONObject("snippet").getString("title");
                channelNames.add(currentPlayListItemID);
            }
        }else{
            System.out.println("Channel response is empty.");
        }
        return channelNames;
    }
    private static ArrayList<String> getChannelIDs(SubscriptionListResponse subscriptionsResponse) {
        ArrayList<String> channelIDs = new ArrayList<>();
        String jsonString = subscriptionsResponse.toString();
        JSONObject obj = new JSONObject(jsonString);
        JSONArray channelArray = obj.getJSONArray("items");
        if(channelArray.length()>0)
        {
            for (int i = 0; i < channelArray.length(); i++)
            {
                String currentPlayListItemID = channelArray.getJSONObject(i).getJSONObject("snippet").getJSONObject("resourceId").getString("channelId");
                channelIDs.add(currentPlayListItemID);
            }
        }else{
            System.out.println("Channel response is empty.");
        }
        return channelIDs;
    }

    private static ArrayList<String> getPlaylistItemIDs(PlaylistItemListResponse playListResponse) {
        ArrayList<String> videoIDs = new ArrayList<>();
        String jsonString = playListResponse.toString();
        JSONObject obj = new JSONObject(jsonString);
        JSONArray playListArray = obj.getJSONArray("items");
        if(playListArray.length()>0)
        {
            for (int i = 0; i < playListArray.length(); i++)
            {
                String currentPlayListItemID = playListArray.getJSONObject(i).getString("id");
                videoIDs.add(currentPlayListItemID);
            }
        }else{
            System.out.println("Playlist is empty.");
        }
        return videoIDs;
    }
    public static ArrayList<Duration> getVideoDurations(VideoListResponse VideoResponse){
        ArrayList<String> videoDurationsAsString = new ArrayList<>();
        String jsonString = VideoResponse.toString();
        JSONObject obj = new JSONObject(jsonString);
        JSONArray playListArray = obj.getJSONArray("items");
        if(playListArray.length()>0)
        {
            for (int i = 0; i < playListArray.length(); i++)
            {
                JSONObject currentJSONObject = playListArray.getJSONObject(i);
                videoDurationsAsString.add(currentJSONObject.getJSONObject("contentDetails").getString("duration"));
            }
        }else{
            System.out.println("VideoListResponse Array is empty.");
        }
        return parseVideoDurations(videoDurationsAsString);
    }
    public static ArrayList<Duration> parseVideoDurations(ArrayList<String> videoDurationsAsString){
        ArrayList<Duration> videoDurations = new ArrayList<>();
        String workingString;
        for (String currentString:videoDurationsAsString){
            workingString = currentString;
            videoDurations.add(Duration.parse(workingString));
        }
        return videoDurations;
    }
    public static ArrayList<String> getVideoIds(PlaylistItemListResponse response) {
        ArrayList<String> videoIDs = new ArrayList<>();
        String jsonString = response.toString();
        JSONObject obj = new JSONObject(jsonString);
        JSONArray playListArray = obj.getJSONArray("items");
        if(playListArray.length()>0)
        {
            for (int i = 0; i < playListArray.length(); i++)
            {
                JSONObject currentJSONObject = playListArray.getJSONObject(i);
                videoIDs.add(currentJSONObject.getJSONObject("snippet").getJSONObject("resourceId").getString("videoId"));
            }
        }else{
            System.out.println("Playlist is empty.");
        }
        return videoIDs;
    }
    public static boolean doesOrganizedPlaylistExist(List<Playlist> PlaylistItems){
        JSONObject obj;
        boolean hasOrganizationPlaylist  = false;
        for (Playlist PlayListItem: PlaylistItems){
            try{
                obj = new JSONObject(PlayListItem.toString());
                String title = obj.getJSONObject("snippet").getJSONObject("localized").getString("title");
                if(title.equals(WLOConstants.PLAYLIST_NAME)){
                    hasOrganizationPlaylist = true;
                    break;
                }
            } catch(Exception e){
                System.out.println("\"localized\" not found.");
                //e.printStackTrace();
            }
        }
        return hasOrganizationPlaylist;
    }
    public static void createOrganizedPlaylist(YouTube myYoutubeService) throws IOException {

        // Define the Playlist object, which will be uploaded as the request body.
        Playlist playlist = new Playlist();

        // Add the snippet object property to the Playlist object.
        PlaylistSnippet snippet = new PlaylistSnippet();
        snippet.setDescription("This playlist organizes when the WatchLaterOrganizer program is run");
        snippet.setTitle(WLOConstants.PLAYLIST_NAME);
        playlist.setSnippet(snippet);

        // Add the status object property to the Playlist object.
        PlaylistStatus status = new PlaylistStatus();
        status.setPrivacyStatus("private");
        playlist.setStatus(status);

        // Define and execute the API request
        YouTube.Playlists.Insert request = myYoutubeService.playlists()
                .insert("snippet,status", playlist);
        Playlist response = request.execute();
        //System.out.println(response);
        System.out.println(WLOConstants.PLAYLIST_NAME + " created.");
    }
    public static String getOrganizedPlaylistId(List<Playlist> PlaylistItems){
        String organizedPlayListId = "null";
        JSONObject currentPlaylistItem;
        for (Playlist PlayListItem: PlaylistItems){
            try{
                currentPlaylistItem = new JSONObject(PlayListItem.toString());
                String title = currentPlaylistItem.getJSONObject("snippet").
                        getJSONObject("localized").
                        getString("title");

                    if(title.equals(WLOConstants.PLAYLIST_NAME)){
                       organizedPlayListId =  currentPlaylistItem.get("id").toString();
                        break;
                    }
            } catch(Exception e){
                System.out.println("\"PlaylistID\" not found.");
                //e.printStackTrace();
            }
        }
        return organizedPlayListId;
    }
    public static List<Playlist> getPlayLists(YouTube youtubeService) throws IOException {
        YouTube.Playlists.List PlaylistRequest = youtubeService.playlists()
                .list("snippet,contentDetails");
        PlaylistListResponse myPlaylistResponse = PlaylistRequest.setMaxResults(25L)
                .setMine(true)
                .execute();

         return myPlaylistResponse.getItems();

    }
    public static ArrayList<String> getVideoTitles(PlaylistItemListResponse response){
        ArrayList<String> title = new ArrayList<>();
        String jsonString = response.toString();
        JSONObject obj = new JSONObject(jsonString);
        JSONArray playListArray = obj.getJSONArray("items");
        if(playListArray.length()>0)
        {
            for (int i = 0; i < playListArray.length(); i++)
            {
                JSONObject currentJSONObject = playListArray.getJSONObject(i);
                title.add(currentJSONObject.getJSONObject("snippet").getString("title"));
            }
        }else{
            System.out.println("Playlist is empty.");
        }
        return title;
    }
    public static void sortByDuration(ArrayList<customYoutubeVideo> myYoutubeVideos, int low, int high){
        if(low < high){
            int pi = partition(myYoutubeVideos,low,high);
            sortByDuration(myYoutubeVideos,low, pi-1);
            sortByDuration(myYoutubeVideos,pi+1,high);
        }

    }
    public static int partition(ArrayList<customYoutubeVideo> myCustomYoutubeVideos, int low, int high){
        customYoutubeVideo pivot = myCustomYoutubeVideos.get(high);
        int i = (low - 1);

        for(int j = low;j<=high-1;j++){
            if(myCustomYoutubeVideos.get(j).getLength().compareTo(pivot.getLength())<0)
            {
                i++; //increment index of smaller element
                Collections.swap(myCustomYoutubeVideos,i,j);
            }
        }
        Collections.swap(myCustomYoutubeVideos,i+1,high);
        return i+1;
    }
}
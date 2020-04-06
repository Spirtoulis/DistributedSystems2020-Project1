import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;

import java.io.IOException;

public class MusicFile {

    private String trackName;
    private String artistName;
    private String albumInfo;
    private String genre;

    public MusicFile(){
        trackName = "";
        artistName = "";
        albumInfo = "";
        genre = "";
    }

    public MusicFile(String trackName, String artistName, String albumInfo, String genre){
        this.trackName = trackName;
        this.artistName = artistName;
        this.albumInfo = albumInfo;
        this.genre = genre;
    }

    public MusicFile(String file_path){
        try {
            Mp3File mp3 = new Mp3File(file_path);
        } catch(UnsupportedTagException u){
            u.printStackTrace();
        } catch(IOException e){
            e.printStackTrace();
        } catch(InvalidDataException i){
            i.printStackTrace();
        }
    }

    public void setTrackName(String trackName){
        this.trackName = trackName;
    }

    public String getTrackName(){
        return trackName;
    }

    public void setArtistName(String artistName){
        this.artistName = artistName;
    }

    public String getArtistName(){
        return artistName;
    }

    public void setAlbumInfo(String albumInfo){
        this.albumInfo = albumInfo;
    }

    public String getAlbumInfo(){
        return albumInfo;
    }

    public void setGenre(String genre){
        this.genre = genre;
    }

    public String getGenre(){
        return genre;
    }

}

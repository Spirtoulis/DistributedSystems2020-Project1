import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;

import java.io.IOException;
import java.io.Serializable;

public class MusicFile implements Serializable {

    private String trackName;
    private String artistName;
    private String albumInfo;
    private String genre;
    private byte[] musicFileExtract;

    public MusicFile(){
        trackName = "";
        artistName = "";
        albumInfo = "";
        genre = "";
        musicFileExtract = null;
    }

    public MusicFile(String trackName, String artistName, String albumInfo, String genre){
        this.trackName = trackName;
        this.artistName = artistName;
        this.albumInfo = albumInfo;
        this.genre = genre;
    }

    public MusicFile(String trackName, String artistName, String albumInfo, String genre, byte[] musicFileExtract){
        this.trackName = trackName;
        this.artistName = artistName;
        this.albumInfo = albumInfo;
        this.genre = genre;
        this.musicFileExtract = musicFileExtract;
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

    public void setMusicFileExtract(byte[] musicFileExtract){
        for(int i=0; i<musicFileExtract.length; i++){
            this.musicFileExtract[i] = musicFileExtract[i];
        }
    }

    public byte[] getMusicFileExtract(){
        return musicFileExtract;
    }

}

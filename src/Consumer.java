import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;
import java.io.*;
import java.net.*;

public class Consumer implements Node {

    private int port;
    private Socket connection = null;
    private ObjectOutputStream out = null;
    private ObjectInputStream in = null;

    private Broker br;
    private String artistName;
    private String songName;

    @Override
    public void init(int i) { port = i;}

    @Override
    public List<Broker> getBrokers() {
        return brokers;
    }

    @Override
    public void connect() throws IOException {
            this.updateNodes();
            connection = new Socket(InetAddress.getByName("192.168.1.7"), 7491);
            out = new ObjectOutputStream(connection.getOutputStream());
            in = new ObjectInputStream(connection.getInputStream());
            System.out.println("conntect succesfully ");
    }

    @Override
    public void disconnect() throws IOException {
        this.updateNodes();
        in.close();
        out.close();
        connection.close();
        System.out.println("discconntect succesfully ");
    }

    @Override
    public void updateNodes() { }

    public void getBrokerList() {
        for (int i = 0; i < brokers.size(); i++) {
            System.out.println("Broker " + i + ": " + brokers.get(i));
        }
    }


    public void register(Broker br, String art_name) throws IOException {
        this.br = br;
        this.artistName = art_name;
        out.writeObject("broker :" + br);
        out.writeObject("artist name : " + artistName);
    }


    public void disconnect(Broker br, ArtistName art_name) throws IOException {
        this.br = null;
        this.artistName = null;
        out.reset();
    }


    public void playData(Value val) {
        System.out.println("artist name : " + this.artistName);
        System.out.println("song name : " + val.getMusicFile().getTrackName());
        System.out.println("music file extract : " + val.getMusicFile().getMusicFileExtract());
    }

    public void setArtistName(String art_Name){
        this.artistName = art_Name;
    }
    public String getArtistName(){return artistName;}

    public void setSongName(String song_name){
        this.songName = song_name;
    }
    public String getSongName(){return songName;}


}

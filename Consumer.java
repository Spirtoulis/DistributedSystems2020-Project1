import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Consumer implements Node {

    private List<Broker> brokers = new ArrayList<>();
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
        return this.brokers;
    }

    @Override
    public void connect(){
        try {
            Scanner sc = new Scanner(System.in);
            this.updateNodes();
            connection = new Socket(InetAddress.getByName("192.168.2.3"), 6214);
            out = new ObjectOutputStream(connection.getOutputStream());
            in = new ObjectInputStream(connection.getInputStream());

            out.writeObject("Consumer");
            out.flush();

            System.out.println("Enter the name of the artist you want to hear: ");
            String artistName = sc.nextLine();
            System.out.println("Enter the name of the song you want to hear");
            String songName = sc.nextLine();
        } catch (IOException ioe){
            ioe.printStackTrace();
        }
    }

    @Override
    public void disconnect(){
        try {
            this.updateNodes();
            in.close();
            out.close();
            connection.close();
            System.out.println("discconntect succesfully ");
        } catch (IOException ioe){
            ioe.printStackTrace();
        }
    }

    @Override
    public void updateNodes() { }

    public void getBrokerList() {
        for (int i = 0; i < this.brokers.size(); i++) {
            System.out.println("Broker " + i + ": " + this.brokers.get(i));
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

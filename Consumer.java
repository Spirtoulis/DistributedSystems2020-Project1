import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Consumer implements Node, Serializable {

    String consumer_name;
    private List<Broker> brokers = new ArrayList<>();
    private int port;
    private Socket connection = null;
    private ObjectOutputStream out = null;
    private ObjectInputStream in = null;


    public Consumer(String name){
        consumer_name = name;
        this.readBrokersFromFile("C:\\Users\\teo\\IdeaProjects\\Ergasia1_Katanemimena\\Brokers.txt");
    }

    @Override
    public void init(int i) { port = i;}

    @Override
    public List<Broker> getBrokers() {
        return this.brokers;
    }

    @Override
    public void connect(){
        String message = null;
        int flag;
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
            out.writeObject(artistName);
            out.flush();

            System.out.println("Enter the name of the song you want to hear");
            String songName = sc.nextLine();
            out.writeObject(songName);
            out.flush();




            while(!message.equals("bye")){

            }
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


    public void register(Broker br, String art_name) {
        Socket socket = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        String message = null;
        int flag;

        try {
            socket = new Socket(InetAddress.getByName("192.168.2.3"), br.port);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            flag = 1;

            out.writeInt(flag);
            out.flush();

            out.writeObject(this);
            out.flush();

            out.writeObject(art_name);
            out.flush();

            flag = in.readInt();

            if (flag == 0) {
                message = (String) in.readObject();
                System.out.println(message);
            } else if (flag == 1) {
                message = (String) in.readObject();
                Broker responsibleBroker = (Broker) in.readObject();

                System.out.println(message + ((Broker) responsibleBroker).port);
                this.register(responsibleBroker, art_name);
            } else if(flag == -1){
                message = (String) in.readObject();
                System.out.println(message);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (ClassNotFoundException c){
            c.printStackTrace();
        } finally {
            try{
                in.close();
                out.close();
                socket.close();
            } catch (IOException ioe){
                ioe.printStackTrace();
            }
        }

    }


    public void disconnect(Broker br, ArtistName art_name) throws IOException {

    }


    public void playData(Value val) {

    }

    public void setArtistName(String art_Name){

    }

    public String getArtistName(){
        return null;
    }

    public void setSongName(String song_name){

    }

    public String getSongName(){
        return null;
    }


    public void readBrokersFromFile(String filename){
        File file = new File(filename);
        try{
            Scanner sc = new Scanner(file);

            while(sc.hasNext()){
                this.brokers.add(new Broker(sc.next(), Integer.parseInt(sc.next())));
            }
        } catch (FileNotFoundException f){
            f.printStackTrace();
        }
    }

    public void requestSong(){
        String message = null;
        int arr_length = 0;
        int chunk_size = 0;
        int index = 0;
        try{
            Scanner sc = new Scanner(System.in);
            connection = new Socket(InetAddress.getByName("192.168.2.3"), this.brokers.get(0).port);
            out = new ObjectOutputStream(connection.getOutputStream());
            in = new ObjectInputStream(connection.getInputStream());

            out.writeInt(2);
            out.flush();

            System.out.println("Enter the name of the artist you want to hear: ");
            String artistName = sc.nextLine();
            out.writeObject(artistName);
            out.flush();

            System.out.println("Enter the name of the song you want to hear: ");
            String songName = sc.nextLine();
            out.writeObject(songName);
            out.flush();

            arr_length = in.readInt();
            chunk_size = in.readInt();
            byte[] array = new byte[arr_length];

            for (int i = 0; i < 9; i++) {
                in.readFully(array, index, chunk_size);
                index = index + chunk_size;
                System.out.println("Streamed chunk number: " + (i + 1));
            }

            in.readFully(array, index, arr_length - index);
            System.out.println("Streamed chunk number: 10");

            String trackName = (String) in.readObject();
            artistName = (String) in.readObject();
            String albumInfo = (String) in.readObject();
            String genre = (String) in.readObject();

            MusicFile musicFile = new MusicFile(trackName, artistName, albumInfo, genre, array);

            System.out.println(musicFile.getTrackName());
            System.out.println(musicFile.getArtistName());
            System.out.println(musicFile.getAlbumInfo());
            System.out.println(musicFile.getGenre());


        } catch (IOException ioe){
            ioe.printStackTrace();
        } catch (ClassNotFoundException c){
            c.printStackTrace();
        }
    }


}

import com.mpatric.mp3agic.*;

import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.io.*;
import java.net.*;
import java.util.concurrent.TimeUnit;

public class Publisher implements Node {

    public List<Broker> brokers = new ArrayList<Broker>();
    int port;
    Socket connection = null;
    ObjectOutputStream out;
    ObjectInputStream in;
    public List<MusicFile> artistSongs = new ArrayList<MusicFile>();
    byte[] arr;
    public List<String> artistsResponsible;
    public List<MusicFile> songsResponsible;
    public String file_path;
    public ServerSocket serverSocket = null;


    public Publisher(String file_path){
        this.file_path = file_path;
        this.artistsResponsible = new ArrayList<String>();
        this.songsResponsible = new ArrayList<MusicFile>();
        File dir = new File(file_path);
        for (File file : dir.listFiles()) {
            try {
                Mp3File mp3 = new Mp3File(file);

                if (mp3.hasId3v1Tag()) {
                    ID3v1 id = mp3.getId3v1Tag();
                    MusicFile musicFile = new MusicFile(id.getTitle(), id.getArtist(), id.getAlbum(), id.getGenreDescription());
                    this.songsResponsible.add(musicFile);
                    if(!((id.getArtist() == null) || this.artistsResponsible.contains(id.getArtist()))) {
                        this.artistsResponsible.add(id.getArtist());
                    }
                } else if (mp3.hasId3v2Tag()) {
                    ID3v2 id = mp3.getId3v2Tag();
                    MusicFile musicFile = new MusicFile(id.getTitle(), id.getArtist(), id.getAlbum(), id.getGenreDescription());
                    this.songsResponsible.add(musicFile);
                    if(!((id.getArtist() == null) || this.artistsResponsible.contains(id.getArtist()))) {
                        this.artistsResponsible.add(id.getArtist());
                    }
                }

            } catch (IOException ioe) {
                ioe.printStackTrace();
            } catch (UnsupportedTagException ut) {
                ut.printStackTrace();
            } catch (InvalidDataException d) {
                d.printStackTrace();
            }
        }
        this.readBrokersFromFile("C:\\Users\\teo\\IdeaProjects\\Ergasia1_Katanemimena\\Brokers.txt");


    }

    @Override
    public void init(int i){
        port = i;
    }

    @Override
    public List<Broker> getBrokers() {
        return brokers;
    }

    @Override
    public void connect(){
        String message;

        try {
            this.updateNodes();

            connection = new Socket(InetAddress.getByName("192.168.2.3"), 6214);
            out = new ObjectOutputStream(connection.getOutputStream());
            in = new ObjectInputStream(connection.getInputStream());

            out.writeObject("Publisher");
            out.flush();

            try{
                String songRequested = (String) in.readObject();

                for(int i=0; i<this.songsResponsible.size(); i++){
                    if(songRequested.equals(this.songsResponsible.get(i).getTrackName())){
                        this.push(new ArtistName(this.songsResponsible.get(i).getArtistName()), new Value(this.songsResponsible.get(i)));
                    }
                }
            } catch(ClassNotFoundException c){
                c.printStackTrace();
            }




        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

    }

    @Override
    public void disconnect(){
        try{
            this.updateNodes();

            in.close();
            out.close();
            connection.close();

        } catch (IOException ioException){
            ioException.printStackTrace();
        }
    }

    @Override
    public void updateNodes(){
        String s = Thread.currentThread().getStackTrace()[2].getMethodName();
        if(s == "connect"){
            nodes.add(this);
        } else if(s == "disconnect"){
            int index = nodes.indexOf(this);
            nodes.remove(index);
        }
    }


    public void readBrokersFromFile(String filename) {
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


    public Broker hashTopic(String artistName) {        //8elei koitagma pali, giati kati paizei n mn ypologizw swsta
        try{
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] messageDigest = md.digest(artistName.getBytes());
            BigInteger no = new BigInteger(1, messageDigest);
            String hashText = no.toString(10);

            int index_of_minimum_broker_hash = 0;
            for(int i=1; i<this.brokers.size(); i++){
                if(this.brokers.get(i).hash_code.compareTo(this.brokers.get(index_of_minimum_broker_hash).hash_code) < 0){
                    index_of_minimum_broker_hash = i;
                }
            }

            Broker broker = null;
            String temp_min = this.brokers.get(0).hash_code;
            for(int i=0; i<this.brokers.size(); i++){
                if((hashText.compareTo(this.brokers.get(i).hash_code)<0) && this.brokers.get(i).hash_code.compareTo(temp_min)<=0){
                    broker = this.brokers.get(i);
                    temp_min = this.brokers.get(i).hash_code;
                }

            }

            return broker;

        } catch(NoSuchAlgorithmException e){
            throw new RuntimeException();
        }
    }


    public void push(ArtistName art_name, Value val) {
        try{
            arr = Files.readAllBytes(Paths.get(file_path + "\\" + val.getMusicFile().getTrackName() + ".mp3"));
        } catch (IOException e){
            e.printStackTrace();
        }

        System.out.println(arr.length);
        System.out.println(brokers.size());

        int chunk_size = arr.length/10;
        int index = 0;

        try {


            out.writeInt(arr.length);
            out.flush();

            out.writeInt(chunk_size);
            out.flush();

            for(int i=0; i<9; i++){
                out.write(arr, index, chunk_size);
                out.flush();
                index = index + chunk_size;
            }

            out.write(arr, index, arr.length-index);
            out.flush();



            out.writeObject("bye");
            out.flush();
        } catch (IOException e){
            e.printStackTrace();
        }
    }


    public void notifyFailure(Broker br) {

    }

    public void register(Broker broker, ArtistName artistname){

        Socket socket = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;

        try {
            socket = new Socket(InetAddress.getByName("192.168.2.3"), broker.port);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            int flag = 0;

            out.writeInt(flag);
            out.flush();

            out.writeObject(this);
            out.flush();

            out.writeObject(artistname);
            out.flush();


        } catch (UnknownHostException u){
            u.printStackTrace();
        } catch (IOException ioe){
            ioe.printStackTrace();
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


}

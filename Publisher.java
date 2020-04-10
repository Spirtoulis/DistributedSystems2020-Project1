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

public class Publisher implements Node, Serializable {

    public static List<Broker> brokers = new ArrayList<Broker>();
    public static List<String> brHashCodes = new ArrayList<String>();
    int port;
    Socket connection = null;
    ObjectOutputStream out;
    ObjectInputStream in;
    public List<String> artistsResponsible;
    public List<MusicFile> songsResponsible;
    public String file_path;



    public Publisher(String file_path){
        this.file_path = file_path;
        this.artistsResponsible = new ArrayList<String>();
        this.songsResponsible = new ArrayList<MusicFile>();
        byte[] arr = null;
        File dir = new File(file_path);
        for (File file : dir.listFiles()) {
            try {
                Mp3File mp3 = new Mp3File(file);

                if (mp3.hasId3v1Tag()) {
                    ID3v1 id = mp3.getId3v1Tag();
                    arr = Files.readAllBytes(Paths.get(file_path + "\\" + id.getTitle() + ".mp3"));
                    MusicFile musicFile = new MusicFile(id.getTitle(), id.getArtist(), id.getAlbum(), id.getGenreDescription(), arr);
                    this.songsResponsible.add(musicFile);
                    if(!((id.getArtist() == null) || this.artistsResponsible.contains(id.getArtist()))) {
                        this.artistsResponsible.add(id.getArtist());
                    }
                } else if (mp3.hasId3v2Tag()) {
                    ID3v2 id = mp3.getId3v2Tag();
                    arr = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
                    MusicFile musicFile = new MusicFile(id.getTitle(), id.getArtist(), id.getAlbum(), id.getGenreDescription(), arr);
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

            //int index_of_minimum_broker_hash = 0;
            brokers = this.getBrokers();
            brHashCodes = Broker.getHashCodes();
            /*for(int i=1; i<this.brokers.size(); i++){
                if(this.brokers.get(i).hash_code.compareTo(this.brokers.get(index_of_minimum_broker_hash).hash_code) < 0){
                    index_of_minimum_broker_hash = i;
                }
            }*/

            Broker broker = null;
            String hc = null;
            boolean b = false;
            for(int i=0; i<brHashCodes.size(); i++) {
                if ((hashText.compareTo(brHashCodes.get(i)) < 0)) {
                    hc = brHashCodes.get(i);
                    b = true;
                    break;
                }
            }

            if (!b){
                broker = brokers.get(0);
            }else {
                for (Broker br : brokers) {
                    if (br.getHashCode().equals(hc))
                        broker = br;
                }
            }

            System.out.println(hashText);
            return broker;

        } catch(NoSuchAlgorithmException e){
            throw new RuntimeException();
        }
    }


    public void push(Broker broker, Value val) {
        byte[] arr = val.getMusicFile().getMusicFileExtract();
        try {
            Socket socket = new Socket(InetAddress.getByName("192.168.2.3"), broker.port);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            int chunk_size = arr.length / 10;
            int index = 0;

            try {

                out.writeInt(3);
                out.flush();

                out.writeInt(arr.length);
                out.flush();

                out.writeInt(chunk_size);
                out.flush();

                for (int i = 0; i < 9; i++) {
                    out.write(arr, index, chunk_size);
                    out.flush();
                    index = index + chunk_size;
                }

                out.write(arr, index, arr.length - index);
                out.flush();

                out.writeObject(val.getMusicFile().getTrackName());
                out.flush();

                out.writeObject(val.getMusicFile().getArtistName());
                out.flush();

                out.writeObject(val.getMusicFile().getAlbumInfo());
                out.flush();

                out.writeObject(val.getMusicFile().getGenre());
                out.flush();


            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (UnknownHostException uh){
            uh.printStackTrace();
        } catch (IOException ioe){
            ioe.printStackTrace();
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

            out.writeInt(flag);     //steile 0 stn broker g n kaneis register
            out.flush();

            out.writeObject(this);
            out.flush();

            out.writeObject(artistname.getArtistName());
            out.flush();


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

    public void openServer(){
        try {
            ServerSocket serverSocket = new ServerSocket(this.port);
            Socket socket = serverSocket.accept();
            ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());

            String artistName = (String) in.readObject();
            String songName = (String) in.readObject();
            int index = 0;
            for(int i=0; i<this.songsResponsible.size(); i++){
                if(songName.equals(this.songsResponsible.get(i).getTrackName())){
                    index = i;
                    break;
                }
            }


        } catch (IOException ioe){
            ioe.printStackTrace();
        } catch (ClassNotFoundException c){
            c.printStackTrace();
        }
    }


}

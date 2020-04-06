import com.mpatric.mp3agic.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.io.*;
import java.net.*;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

public class Publisher implements Node {

    public static List<Broker> brokers = new ArrayList<Broker>();
    int port;
    Socket connection = null;
    ObjectOutputStream out;
    ObjectInputStream in;
    public List<MusicFile> artistSongs = new ArrayList<MusicFile>();
    byte[] arr;
    public TreeSet<String> artistsResponsible = new TreeSet<String>();
    public List<MusicFile> songsResponsible;
    public String file_path;


    public Publisher(String file_path){
        this.file_path = file_path;
        this.artistsResponsible = new TreeSet<String>();
        this.songsResponsible = new ArrayList<MusicFile>();
        File dir = new File(file_path);
        for (File file : dir.listFiles()) {
            try {
                Mp3File mp3 = new Mp3File(file);

                if (mp3.hasId3v1Tag()) {
                    ID3v1 id = mp3.getId3v1Tag();
                    MusicFile musicFile = new MusicFile(id.getTitle(), id.getArtist(), id.getAlbum(), id.getGenreDescription());
                    this.songsResponsible.add(musicFile);
                    if(!(id.getArtist() == null)) {
                        this.artistsResponsible.add(id.getArtist());
                    }
                } else if (mp3.hasId3v2Tag()) {
                    ID3v2 id = mp3.getId3v2Tag();
                    MusicFile musicFile = new MusicFile(id.getTitle(), id.getArtist(), id.getAlbum(), id.getGenreDescription());
                    this.songsResponsible.add(musicFile);
                    if(!(id.getArtist() == null)) {
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


    }


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

            connection = new Socket(InetAddress.getByName("192.168.2.3"), 7491);
            out = new ObjectOutputStream(connection.getOutputStream());
            in = new ObjectInputStream(connection.getInputStream());

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


    public void getBrokerList() {
        for (int i = 0; i < brokers.size(); i++) {
            System.out.println("Broker " + i + ": " + brokers.get(i));
        }
    }


    public Broker hashTopic(ArtistName art_name) {
        return null;
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


}

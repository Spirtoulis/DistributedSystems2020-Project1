import java.util.ArrayList;
import java.util.List;
import java.io.*;
import java.net.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

public class Broker extends Thread implements Node, Serializable {

    private static List<Broker> brokers = new ArrayList<Broker>();
    public List<Publisher> registeredPublishers = new ArrayList<>();
    public List<Consumer> registeredUsers = new ArrayList<>();
    public int port;
    String ip;
    public String broker_name;
    ServerSocket providerSocket = null;
    Socket connection = null;
    ObjectOutputStream out = null;
    ObjectInputStream in = null;
    List<String> artistsResponsible = new ArrayList<String>();
    private static ArrayList<String> hashCodes = new ArrayList<String>();
    String hash_code;
    String message;
    public List<MusicFile> savedSongs = new ArrayList<MusicFile>();

    public Broker(){
        readBrokersFromFile("C:\\Users\\teo\\IdeaProjects\\Ergasia1_Katanemimena\\Brokers.txt");
    }

    public Broker(String broker_name){
        this.broker_name = broker_name;
    }

    public Broker(int port){
        this.port = port;
    }

    public Broker(String name, int port){
        this.broker_name = name;
        this.port = port;
        try{
            ip = InetAddress.getLocalHost().toString();
        } catch (UnknownHostException u){
            u.printStackTrace();
        }
        this.hash(ip + Integer.toString(this.port));
    }

    public Broker(String broker_name, int port, String hash_code){
        this.broker_name = broker_name;
        this.port = port;
        this.hash_code = hash_code;
    }

    //dialegume enan ari8mo ap to 0 ws to mege8os tu pinaka twn brokers gia na dwsume ena port ston broker mas
    public void init(int i) {
        port = this.brokers.get(i).port;
        try{
            this.ip = InetAddress.getLocalHost().toString();
        } catch(UnknownHostException u){
            u.printStackTrace();
        }
        this.hash(ip + Integer.toString(port));

    }

    public static List<String> getHashCodes(){
        return hashCodes;
    }

    @Override
    public List<Broker> getBrokers() {
        return brokers;
    }

    @Override
    public void connect() {
        String message = null;
        try {
            providerSocket = new ServerSocket(port);
            while (true) {

                connection = providerSocket.accept();
                out = new ObjectOutputStream(connection.getOutputStream());
                in = new ObjectInputStream(connection.getInputStream());
                int flag;

                flag = in.readInt();

                if(flag == 0){          //an to flag einai 0 simainei oti enas publisher 8elei n kanei register
                    Publisher pub = (Publisher) in.readObject();
                    String artistName = (String) in.readObject();
                    registeredPublishers.add(pub);
                    if(!this.artistsResponsible.contains(artistName)) {
                        this.artistsResponsible.add(artistName);
                    }
                    System.out.println("Broker " + this.getName() + " accepted registration from publisher " + pub);
                    for(int i=0; i<this.registeredPublishers.size(); i++){
                        System.out.println(this.registeredPublishers.get(i).port);
                    }
                    for(int i=0; i<this.artistsResponsible.size(); i++){
                        System.out.println(this.artistsResponsible.get(i));
                    }
                } else if(flag == 1){       //an to flag einai 1 simainei oti enas consumer 8elei n kanei register
                    Consumer consumer = (Consumer) in.readObject();
                    String artistName = (String) in.readObject();
                    if(this.artistsResponsible.contains(artistName)){
                        if(!this.registeredUsers.contains(consumer)) {      //la8os, gt dn mporei na kanei compare tous consumer metaksu tous
                            this.registeredUsers.add(consumer);
                        }

                        out.writeInt(0);            //an aftos o broker einai ypef8inos g tn artist steile 0
                        out.flush();

                        out.writeObject("I am responsible for this artist and I accepted the registration");
                        out.flush();


                    } else{
                        Broker responsibleBroker = findResponsibleBroker(artistName);

                        if(responsibleBroker == null){

                            out.writeInt(-1);           //an kanenas broker dn einai ypef8inos g tn artist steile -1
                            out.flush();

                            out.writeObject("There isn 't " + artistName + " in our app");
                            out.flush();
                        } else {

                            out.writeInt(1);        //steile 1 an vrikame poios einai o ypef8inos broker g tn artist
                            out.flush();

                            out.writeObject("I am not responsible for this artist. Please contac broker with port: ");
                            out.flush();

                            out.writeObject(responsibleBroker);
                            out.flush();
                        }

                    }

                    for(int i=0; i<this.registeredUsers.size(); i++){
                        System.out.println(this.registeredUsers.get(i).consumer_name);
                    }
                } else if(flag == 2){       //an to flag einai 2 simainei oti enas consumer ekane request gia kapoio tragoudi
                    String artistName;
                    String songName;

                    artistName = (String) in.readObject();
                    songName = (String) in.readObject();

                    for(int i=0; i<this.savedSongs.size(); i++){
                        if(artistName.equals(this.savedSongs.get(i).getArtistName()) && songName.equals(this.savedSongs.get(i).getTrackName())){
                            pull(new ArtistName(artistName), songName, i);
                            break;
                        }
                    }

                } else if(flag == 3){       //an to flag einai 3 simainei oti kapoios publisher pusharei kapoio kommati
                    int arr_length = 0;
                    int chunk_size = 0;
                    int index = 0;

                    try{
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
                        String artistName = (String) in.readObject();
                        String albumInfo = (String) in.readObject();
                        String genre = (String) in.readObject();

                        this.savedSongs.add(new MusicFile(trackName, artistName, albumInfo, genre, array));
                        System.out.println(this.savedSongs.size());
                    } catch (IOException ioe){
                        ioe.printStackTrace();
                    }
                }

                Thread t = new ClientHandler(connection, out, in);
                t.start();



            }
        } catch(IOException e){
            e.printStackTrace();
        } catch (ClassNotFoundException c){
            c.printStackTrace();
        }
    }

    @Override
    public void disconnect() {
        try {

            in.close();
            out.close();
            connection.close();
            providerSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateNodes() {

    }

    public Consumer acceptConnection(Consumer con) {
        return null;
    }

    public Publisher acceptConnection(Publisher pub) {
        return null;
    }


    public void calculateKeys() {

    }


    public void notifyPublisher(String str) {

    }


    public void pull(ArtistName artistName, String songName, int i) {
        byte[] arr = this.savedSongs.get(i).getMusicFileExtract();
        int chunk_size = arr.length/10;
        int index = 0;
        try{
            out.writeInt(arr.length);
            out.flush();

            out.writeInt(chunk_size);
            out.flush();

            for(int j=0; j<9;j++){
                out.write(arr, index, chunk_size);
                out.flush();
                index = index + chunk_size;
            }

            out.write(arr, index, arr.length - index);
            out.flush();

            out.writeObject(this.savedSongs.get(i).getTrackName());
            out.flush();

            out.writeObject(this.savedSongs.get(i).getArtistName());
            out.flush();

            out.writeObject(this.savedSongs.get(i).getAlbumInfo());
            out.flush();

            out.writeObject(this.savedSongs.get(i).getGenre());
            out.flush();
        } catch (IOException ioe){
            ioe.printStackTrace();
        }
    }

    public void readBrokersFromFile(String filename){
        File file = new File(filename);
        try {
            Scanner sc = new Scanner(file);

            while(sc.hasNext()){
                this.brokers.add(new Broker(sc.next(), Integer.parseInt(sc.next())));
            }
        }catch (FileNotFoundException f){
            f.printStackTrace();
        }
    }

    public void writeToFile(){                          //mporei na xrisimopoih8ei alla dn nomizw
        try {
            FileWriter writer = new FileWriter("Brokers.txt", true);
            writer.write(this.broker_name + " ");
            writer.write(Integer.toString(this.port) + " ");
            writer.write(this.hash_code);
            writer.write("\r\n");
            writer.close();
        } catch(IOException ioe){
            ioe.printStackTrace();
        }
    }

    public void hash(String input){
        try{
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] messageDigest = md.digest(input.getBytes());
            BigInteger no = new BigInteger(1, messageDigest);
            String hashText = no.toString(10);

            this.hash_code = hashText;
        } catch(NoSuchAlgorithmException e){
            throw new RuntimeException();
        }
    }

    public Broker findResponsibleBroker(String artistName){
        for(int i=0; i<this.brokers.size(); i++){
            if(this.brokers.get(i).artistsResponsible.contains(artistName)){
                return this.brokers.get(i);
            }
        }
        return null;
    }

    public String getHashCode(){
        return hash_code;
    }
}

class ClientHandler extends Thread{

    final ObjectInputStream in;
    final ObjectOutputStream out;
    final Socket s;
    String hashArtist;

    public ClientHandler(Socket s, ObjectOutputStream out, ObjectInputStream in){
        this.s = s;
        this.out = out;
        this.in = in;
    }

    @Override
    public void run() {
        String message = null;
        int arr_length = 0;
        int chunk_size = 0;
        int index = 0;
        String flag;

        try {
            flag = (String) in.readObject();

            if (flag.equals("Publisher")) {
                do {
                    try {
                        Scanner sc = new Scanner(System.in);
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
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException in) {
                                in.printStackTrace();
                            }
                        }

                        in.readFully(array, index, arr_length - index);
                        System.out.println("Streamed chunk number: 10");


                        message = (String) in.readObject();
                        System.out.println(message);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException c) {
                        c.printStackTrace();
                    }
                } while (!message.equals("bye"));


            } else if (flag.equals("Consumer")) {

                message = (String) in.readObject();
                hashArtist(message);
                System.out.println(message);
                System.out.println(hashArtist);

                message = (String) in.readObject();
                System.out.println(message);

            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (ClassNotFoundException c) {
            c.printStackTrace();
        }

    }

    public void hashArtist(String artistName){
        try{
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] messageDigest = md.digest(artistName.getBytes());
            BigInteger no = new BigInteger(1, messageDigest);
            String hashText = no.toString(10);

            this.hashArtist = hashText;
        } catch(NoSuchAlgorithmException e){
            throw new RuntimeException();
        }
    }

}

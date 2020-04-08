import java.util.ArrayList;
import java.util.List;
import java.io.*;
import java.net.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

public class Broker extends Thread implements Node {

    private List<Broker> brokers = new ArrayList<Broker>();
    public int port;
    String ip;
    public String broker_name;
    ServerSocket providerSocket = null;
    Socket connection = null;
    ObjectOutputStream out = null;
    ObjectInputStream in = null;
    List<artistTest> artistsResponsible = new ArrayList<>();
    String hash_code;

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

    @Override
    public List<Broker> getBrokers() {
        return brokers;
    }

    @Override
    public void connect() {
        String message = null;
        try {
            providerSocket = new ServerSocket(port);
            this.updateNodes();
            while (true) {
                try {

                    connection = providerSocket.accept();
                    out = new ObjectOutputStream(connection.getOutputStream());
                    in = new ObjectInputStream(connection.getInputStream());

                    Thread t = new ClientHandler(connection, out, in);
                    t.start();


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void disconnect() {
        try {
            this.updateNodes();

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


    public void pull(ArtistName art_name) {

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
}

class ClientHandler extends Thread{

    final ObjectInputStream in;
    final ObjectOutputStream out;
    final Socket s;

    public ClientHandler(Socket s, ObjectOutputStream out, ObjectInputStream in){
        this.s = s;
        this.out = out;
        this.in = in;
    }

    @Override
    public void run(){
        String message = null;
        int arr_length = 0;
        int chunk_size = 0;
        int index = 0;
        do{
            try {
                Scanner sc = new Scanner(System.in);
                System.out.println("Enter the name of the song you want to hear: ");
                String songName = sc.nextLine();

                out.writeObject(songName);
                out.flush();

                arr_length = in.readInt();
                chunk_size = in.readInt();

                byte[] array = new byte[arr_length];

                for(int i=0; i<9; i++){
                    in.readFully(array, index, chunk_size);
                    index = index + chunk_size;
                    System.out.println("Streamed chunk number: " + (i+1));
                    try {
                        Thread.sleep(2000);
                    } catch(InterruptedException in){
                        in.printStackTrace();
                    }
                }

                in.readFully(array, index, arr_length-index);
                System.out.println("Streamed chunk number: 10");


                message = (String) in.readObject();
                System.out.println(message);
            } catch (IOException e){
                e.printStackTrace();
            } catch (ClassNotFoundException c){
                c.printStackTrace();
            }
        } while(!message.equals("bye"));
    }



}

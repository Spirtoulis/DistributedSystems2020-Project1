import java.io.IOException;

public class ConsumerMainTEST {

    public static void main(String[] args) throws IOException {
        Consumer cm = new Consumer();
        cm.setArtistName("dane ");
        System.out.print(cm.getArtistName());

        cm.connect();


    }
}

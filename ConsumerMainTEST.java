import java.io.IOException;

public class ConsumerMainTEST {

    public static void main(String[] args) {
        Consumer consumer = new Consumer("Teo Tselikis");
        consumer.register(consumer.getBrokers().get(0), "Kevin MacLeod");
        consumer.requestSong();


    }
}

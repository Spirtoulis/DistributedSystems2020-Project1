public class Consumer1 {

    public static void main(String[] args){
        Consumer consumer1 = new Consumer("TheGreatDane");
        consumer1.register(consumer1.getBrokers().get(0), "Rafael Krux");
        consumer1.requestSong();
    }
}

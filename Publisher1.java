import java.util.ArrayList;
import java.util.List;

public class Publisher1 {

    public static void main(String[] args){

        Publisher pub1 = new Publisher("C:\\Users\\teo\\Desktop\\dataset1\\dataset1\\Comedy");
        pub1.init(1453);
        pub1.connect();
        pub1.push(new ArtistName(pub1.artistsResponsible.first()), new Value(pub1.songsResponsible.get(0)));

    }
}

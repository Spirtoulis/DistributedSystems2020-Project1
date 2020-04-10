import java.util.ArrayList;
import java.util.List;

public class Publisher2 {

    public static void main(String[] args){
        Publisher pub2 = new Publisher("C:\\Users\\teo\\Desktop\\dataset1\\dataset1\\Electronic");
        pub2.init(7491);

        System.out.println(pub2.artistsResponsible);

        for(int i=0; i<pub2.brokers.size(); i++) {
            System.out.println(pub2.brokers.get(i).hash_code);
        }

        Broker br = pub2.hashTopic(pub2.artistsResponsible.get(0));
        System.out.println();
        System.out.println(br.hash_code);

        for(String artistName : pub2.artistsResponsible){
            Broker broker = pub2.hashTopic(artistName);
            pub2.register(broker, new ArtistName(artistName));
        }

        for(MusicFile musicFile : pub2.songsResponsible){
            Broker broker = pub2.hashTopic(musicFile.getArtistName());
            pub2.push(broker, new Value(musicFile));
        }

    }
}

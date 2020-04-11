public class Publisher1 {

    public static void main(String[] args){

        Publisher pub1 = new Publisher("C:\\Users\\teo\\Desktop\\dataset1\\dataset1\\Comedy");
        pub1.init(1453);

        System.out.println(pub1.artistsResponsible);

        for(int i=0; i<pub1.brokers.size(); i++) {
            System.out.println(pub1.brokers.get(i).hash_code);
        }

        System.out.println();

        for(String artistName : pub1.artistsResponsible){
            Broker broker = pub1.hashTopic(artistName);
            pub1.register(broker, new ArtistName(artistName));
        }

        for(MusicFile musicFile : pub1.songsResponsible){
            Broker broker = pub1.hashTopic(musicFile.getArtistName());
            pub1.push(broker, new Value(musicFile));
        }
    }
}

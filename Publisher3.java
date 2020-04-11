public class Publisher3 {

    public static void main(String[] args){
        Publisher pub3 = new Publisher("C:\\Users\\teo\\Desktop\\dataset1\\dataset1\\Fantasy");
        pub3.init(3000);

        System.out.println(pub3.artistsResponsible);

        for(int i=0; i<pub3.brokers.size(); i++) {
            System.out.println(pub3.brokers.get(i).hash_code);
        }

        Broker br = pub3.hashTopic(pub3.artistsResponsible.get(3));
        System.out.println();
        System.out.println(br.hash_code);

        for(String artistName : pub3.artistsResponsible){
            Broker broker = pub3.hashTopic(artistName);
            pub3.register(broker, new ArtistName(artistName));
        }

        for(MusicFile musicFile : pub3.songsResponsible){
            Broker broker = pub3.hashTopic(musicFile.getArtistName());
            pub3.push(broker, new Value(musicFile));
        }
    }


}

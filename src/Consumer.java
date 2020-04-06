import java.util.List;

public class Consumer implements Node {

    int port;

    @Override
    public void init(int i) {
        this.port = i;
    }

    @Override
    public List<Broker> getBrokers() {
        return null;
    }

    @Override
    public void connect() {

    }

    @Override
    public void disconnect() {

    }

    @Override
    public void updateNodes() {

    }


    public void register(Broker br, ArtistName art_name) {

    }


    public void disconnect(Broker br, ArtistName art_name) {

    }


    public void playData(ArtistName art_name, Value val) {

    }
}

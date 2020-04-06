import java.util.ArrayList;
import java.util.List;

public interface Node {

    public static List<Node> nodes = new ArrayList<Node>();

    public void init(int i);

    public List<Broker> getBrokers();

    public void connect();

    public void disconnect();

    public void updateNodes();
}

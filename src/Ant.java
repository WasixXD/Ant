import java.util.HashMap;
import java.util.List;

public class Ant {
    public long key;
    public native Ant conn(String host, String user, String password, String port, String dbname);
    public native List<HashMap<String, String>> query(String query);
    public native void disconnect();

    static {
        System.loadLibrary("ant");
    }

}

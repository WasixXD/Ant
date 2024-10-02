public class Ant {
    public long key;
    public native Ant conn(String host, String user, String password, String port, String dbname);

    static {
        System.loadLibrary("ant");
    }

}

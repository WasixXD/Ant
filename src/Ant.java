public class Ant {
    public long key;
    public native Ant conn(String host, String user, String password, String port, String dbname);
    public native void query(String query);

    static {
        System.loadLibrary("ant");
    }

}

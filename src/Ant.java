public class Ant {
    public native void hello();
    static {
        System.loadLibrary("ant");
    }

    public Ant() {
    }
}

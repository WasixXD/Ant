
public class Main {
    public static void main(String[] args) {
        Ant conn = new Ant().conn("localhost", "postgres", "postgres", "5432", "fastdialer");
        System.out.printf("%d\n", conn.key);
    }
    
}

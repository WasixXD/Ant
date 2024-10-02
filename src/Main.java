
public class Main {
    public static void main(String[] args) {
        Ant conn = new Ant().conn("localhost", "postgres", "postgres", "5432", "fastdialer");

        conn.query("SELECT * from call_nlp_resultado");
    }
    
}

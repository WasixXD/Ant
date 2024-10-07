import java.util.HashMap;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        Ant conn = new Ant().conn("localhost", "postgres", "postgres", "5432", "fastdialer");

        List<HashMap<String, String>> payload = conn.query("SELECT * from call");
        System.out.println(payload.size());
    
        // for(int i = 0; i < payload.size(); i++) {
        //     System.out.println("ID: " + payload.get(i).get("id"));
        // }    
    }
}

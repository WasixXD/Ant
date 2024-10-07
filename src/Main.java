import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Main {
    private final static int n_threads = Runtime.getRuntime().availableProcessors();
    private ArrayBlockingQueue<ArrayList<String>> queue = new ArrayBlockingQueue<ArrayList<String>>(n_threads);
    private int lines_count = 0;
    private final int BATCH_SIZE = 50000;
    private ArrayList<String> lines;
    public static void main(String[] args) {
        Ant conn = new Ant().conn("localhost", "postgres", "postgres", "5432", "ant");
        String path_to_file = "./csv10mb.csv";
        long startTime = System.nanoTime();
        Main main = new Main();
        main.concurrencyImpl(conn, path_to_file);
        long endTime = System.nanoTime();
        long duration = (endTime - startTime);
        System.out.println("[x] Finished in " + duration);
    }

    // 9m
    public static void naiveImplementation(Ant connection, String path) {
        String line;
        int i = 0;

        try(BufferedReader br = new BufferedReader(new FileReader(path))) {
            while((line = br.readLine()) != null) {
                if(i == 0) {
                    i++;
                    continue;
                }
                String[] parts = line.split(";");
                String sql = String.format("INSERT INTO users VALUES('%s', '%s', %s);", parts[0], parts[1], parts[2]);
                connection.query(sql);
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    // 3m
    public void concurrencyImpl(Ant connection, String path) {

        ArrayList<Thread> threads = new ArrayList<Thread>(n_threads);

        for(int i = 0; i < n_threads; i++) {
            Thread job = new Thread(() -> job());
            job.start();
            threads.add(job);
        }

        String line;
        this.lines = new ArrayList<String>();
        try(BufferedReader br = new BufferedReader(new FileReader(path))) {
            line = br.readLine(); // read first line
            while((line = br.readLine()) != null) {
                this.lines_count++;
                this.lines.add(line);

                if(this.lines_count % this.BATCH_SIZE == 0) {
                    this.queue.offer(this.lines);
                    this.lines = new ArrayList<String>();
                }
            }

            if(!this.lines.isEmpty()) {
                this.queue.offer(this.lines);
            }

            ArrayList<String> poison = new ArrayList<String>();
            poison.add("POISON");
            for(int i = 0; i < n_threads; i++) {
                this.queue.offer(poison);
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
        
        try {
            for(Thread t : threads) {
                t.join();
            }
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void job() {
        Ant connection = new Ant().conn("localhost", "postgres", "postgres", "5432", "ant");
        try {
            while(true) {
                ArrayList<String> lines = this.queue.take();
                if(lines.size() == 1 && lines.get(0).equals("POISON")) {
                    break;
                }
                for(String line : lines) {
                    String[] split = line.split(";");
                    String sql = String.format("INSERT INTO users VALUES('%s', '%s', %s);", split[0], split[1], split[2]);
                    connection.query(sql);
                }
            }
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
        System.gc();
    }
}

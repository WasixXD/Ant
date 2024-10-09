import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.*;
import java.io.File;
import java.nio.*;
import java.nio.charset.*;
import java.util.concurrent.*;
import java.util.stream.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

public class Main {
    private final static int n_threads = Runtime.getRuntime().availableProcessors();
    private ArrayBlockingQueue<ArrayList<String>> queue = new ArrayBlockingQueue<ArrayList<String>>(1000);
    // private ConcurrentLinkedQueue<ArrayList<String>> queue2 = new ConcurrentLinkedQueue<ArrayList<String>>();
    private int lines_count = 0;
    private final int BATCH_SIZE = 100_000;
    private ArrayList<String> lines;
    public static void main(String[] args) {
        Ant conn = new Ant().conn("localhost", "postgres", "postgres", "5432", "ant");
        String path_to_file = "./csv1gb.csv";
        long startTime = System.nanoTime();
        Main main = new Main();
        // main.concurrencyImpl(conn, path_to_file);
        // main.naiveImplementation(conn, path_to_file);
        // main.cBatchImpl(conn, path_to_file);
        main.threadPoolImpl(conn, path_to_file);
        long endTime = System.nanoTime();
        long duration = (endTime - startTime);
        System.out.println("[x] Finished in " + duration);
    }

    // 209712 linhas => 9 minutos - 388 linhas/s
    // 20971521 linhas => nem tenta
    public void naiveImplementation(Ant connection, String path) {
        System.out.println("Starting naive impl");
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

    // 209712 linhas => 4 minutos - 873 linhas/s
    // 20971521 linhas => trava em 1.200.000 
    public void concurrencyImpl(Ant connection, String path) {

        System.out.println("Starting concurrency impl");
        String line;
        ArrayList<Thread> threads = new ArrayList<Thread>(n_threads);

        for(int i = 0; i < n_threads; i++) {
            Thread job = new Thread(() -> job1());
            job.start();
            threads.add(job);
        }

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

            for(Thread t : threads) {
                t.join();
            }
        } catch(IOException | InterruptedException e) {
            e.printStackTrace();
        }
        
    }
    
    
    private void job1() {
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
    }

    // 209712 linhas => 0,4 segundos - 491376 linhas/s
    // 20971521 linhas => 24 segundos - 8631 linhas/s
    public void cBatchImpl(Ant connection, String path) {

        System.out.println("Starting Concurrency + Batch + String Builder impl");
        String line;
        ArrayList<Thread> threads = new ArrayList<Thread>(n_threads);

        for(int i = 0; i < n_threads; i++) {
            Thread job = new Thread(() -> job2());
            job.start();
            threads.add(job);
        }

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

            for(Thread t : threads) {
                t.join();
            }
        } catch(IOException | InterruptedException e) {
            e.printStackTrace();
        }
        
    }

    private void job2() {
        Ant connection = new Ant().conn("localhost", "postgres", "postgres", "5432", "ant");
        try {
            while(true) {
                ArrayList<String> lines = this.queue.take();
                if(lines.size() == 1 && lines.get(0).equals("POISON")) {
                    break;
                }
                StringBuilder sb = new StringBuilder();
                sb.append("INSERT INTO users VALUES ");
                for(String line : lines) {
                    String[] split = line.split(";");
                    sb.append("('"+ split[0] + "','" + split[1] +"'," + split[2]  +"),");
                }
                sb.setCharAt(sb.length() - 1, ';');
                connection.query(sb.toString());
            }
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    // 209712 linhas => 0,2 segundos 
    // 20971521 linhas => 22 segundos 
    public void threadPoolImpl(Ant connection, String path) {
        System.out.println("Starting threadPool impl");
        String line;

        ThreadPoolExecutor threads = (ThreadPoolExecutor)Executors.newFixedThreadPool(n_threads); 

        for(int i = 0; i < n_threads; i++) {
           threads.execute(() -> job3()); 
        }

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
          
            this.queue.offer(this.lines);

            ArrayList<String> poison = new ArrayList<String>();
            poison.add("POISON");

            for(int i = 0; i < n_threads; i++) {
                this.queue.offer(poison);
            }

            threads.shutdown();
            while(!threads.isTerminated()) {}
        } catch(IOException e) {
            e.printStackTrace();
        }
        
    }

    private void job3() {
        Ant connection = new Ant().conn("localhost", "postgres", "postgres", "5432", "ant");
        try {
            while(true) {
                ArrayList<String> lines = this.queue.take();
                if(lines.size() == 1 && lines.get(0).equals("POISON")) {
                    break;
                }
                StringBuilder sb = new StringBuilder();
                sb.append("INSERT INTO users VALUES ");
                for(String line : lines) {
                    StringTokenizer split = new StringTokenizer(line, ";");
                    sb.append("('"+ split.nextToken() + "','" + split.nextToken() +"'," + split.nextToken()  +"),");
                }
                sb.setCharAt(sb.length() - 1, ';');
                connection.query(sb.toString());
            }
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
    }

    

}

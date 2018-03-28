package personalised.library.GraphDB;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.*;

import personalised.library.GraphDB.SimpleGraph;
import personalised.library.GraphDB.GraphException;

public class Server {

    private int port;
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private SimpleGraph g;

    public Server(int port) throws IOException {
        this.port = port;
        serverSocket = new ServerSocket(port);
        try{
            g = new SimpleGraph("" + port + ".db").load();
        } catch(Exception e){
            g = new SimpleGraph("" + port + ".db");
        }
    }

    public void start() throws IOException{
        clientSocket = serverSocket.accept();
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        String line;
        while ((line = in.readLine()) != null) {
            System.out.println(line);
            try{
                line = line.toLowerCase();
                String[] params = line.split(":");
                for (int i=0; i<params.length; i++){
                    if (params[i].equals("null")){
                        params[i] = null;
                    }
                }
                if (params[0].equals("commit")) {
                    if (params.length != 1)
                        throw new GraphException("No parameter required");
                    g.commit();
                    out.println("Committed");
                } else if (params[0].equals("add")) {
                    if (params.length != 4)
                        throw new GraphException("Exactly 3 parameter required");
                    g.add(params[1], params[2], params[3]);
                    out.println("Added");
                } else if (params[0].equals("map")) {
                    if (params.length != 4)
                        throw new GraphException("Exactly 3 parameter required with 2 being null");
                    out.println(g.map(params[1], params[2], params[3]));
                } else if (params[0].equals("delete")) {
                    if (params.length != 4)
                        throw new GraphException("Exactly 3 parameter required");
                    g.delete(params[1], params[2], params[3]);
                    out.println("Deleted");
                } else if (params[0].equals("update")) {
                    if (params.length != 6)
                        throw new GraphException("Exactly 5 parameter required");
                    g.update(params[1], params[2], params[3], params[4], params[5]);
                    out.println("Updated");
                } else if (params[0].equals("list")) {
                    if (params.length != 4)
                        throw new GraphException("Exactly 3 parameter required with 1 being null");
                    out.println(g.list(params[1], params[2], params[3]));
                } else if (params[0].equals("value")) {
                    if (params.length != 4)
                        throw new GraphException("Exactly 3 parameter required with 1 being null");
                    out.println(g.value(params[1], params[2], params[3]));
                } else if (params[0].equals("is")) {
                    if (params.length != 4)
                        throw new GraphException("Exactly 3 parameter required with none being null");
                    out.println(g.is(params[1], params[2], params[3]));
                } else if (params[0].equals("triples")) {
                    if (params.length != 4)
                        throw new GraphException("Exactly 3 parameter required");
                    out.println(g.triples(params[1], params[2], params[3]));
                } else if (params[0].equals("isSubject")) {
                    if (params.length != 2)
                        throw new GraphException("Exactly 1 parameter");
                    out.println(g.isSubject(params[1]));
                } else if (params[0].equals("isPredicate")) {
                    if (params.length != 2)
                        throw new GraphException("Exactly 1 parameter");
                    out.println(g.isPredicate(params[1]));
                } else if (params[0].equals("isObject")) {
                    if (params.length != 2)
                        throw new GraphException("Exactly 1 parameter");
                    out.println(g.isObject(params[1]));
                } else if (params[0].equals("query")) {
                    if (params.length != 4)
                        throw new GraphException("Exactly 3 parameters");
                    List<String[]> list = new ArrayList<>();
                    String[] strs = new String[3];
                    strs[0] = params[1];
                    strs[1] = params[2];
                    strs[2] = params[3];
                    list.add(strs);
                    out.println((Map<String, Set<String>>)g.query(list));
                }
            } catch(GraphException e){
                out.println(e);
            }
        }
    }

    public static void main(String args[]){
        try {
            Server s = new Server(1111);
            while(true){
                s.start();
            }
        } catch (IOException e){
            System.out.println(e);
        }
    }

}
package personalised.library.GraphDB;

import personalised.library.GraphDB.SimpleGraph;
import java.util.*;
import java.io.*;
import java.net.*;

public class Client {

    public static void main(String args[]) {
        try {
        	
            if (args.length != 2)
                throw new Exception("Expected exactly 2 arguments: <host> <port>");

            String intro = "Graph DB java starting at port " + args[1];
            System.out.println(intro);

            Socket socket = new Socket(args[0], Integer.parseInt(args[1]));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String resp;
            Scanner scanner = new Scanner(System.in);
            while(true) {
                System.out.print(">>> ");
                resp = scanner.next();
                if (resp.equals("quit"))
                    break;
                out.println(resp);
                System.out.println(in.readLine());
            }
        } catch(Exception e) {
            System.out.println(e.toString());
        }
    }

}
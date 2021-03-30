package server;

import java.io.IOException;

public class Main {
   static Server server;

   private Main() { // never instantiated
   }

   public static void main(String[] args) throws IOException {
      if(args.length > 1) {
         System.out.println("Bad Format: usage java <port-number>");
      }
      server = args.length == 1 ? new Server(Integer.parseInt(args[0])) : new Server(80);
      server.start();
   }
}
package server;

import java.io.IOException;

public class Main {
   static Server server;

   private Main() { // never instantiated
   }

   public static void main(String[] args) {
      if (args.length > 1) {
         System.err.println("Bad Format: usage java <port-number>");
         return;
      }
      try {
         server = args.length == 1 ? new Server(Integer.parseInt(args[0])) : new Server(80);
      } catch (IOException e) {
         System.err.println("Error creating the Server");
         e.printStackTrace();
      }

      if (server.setUp()) {
         server.start();
      } else {
         System.err.println("Could not setup Server to run; check that vhosts.conf file is present and well formatted");
      }
   }
}
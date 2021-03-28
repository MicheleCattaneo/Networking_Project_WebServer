package server;

import java.io.IOException;

public class Main {
   static Server server;

   private Main() { // never instantiated
   }

   public static void main(String[] args) throws IOException {
      server = new Server(80);
      server.start();
   }
}
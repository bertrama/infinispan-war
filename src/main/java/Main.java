
import java.util.Map;
import java.util.HashMap;
import java.security.ProtectionDomain;
import java.net.URL;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.webapp.WebAppContext;

public class Main
{
   private static String host           = "127.0.0.1";
   private static String config         = null;
   private static Integer httpPort      = 8080;
   private static Integer hotrodPort    = 11222;
   private static Integer memcachedPort = 11211;

   private static void parseArgs(String[] args) {
      // Pull in the system properties.
      if (System.getProperty("infinispan.host") != null) {
         host = System.getProperty("infinispan.host");
      }
      if (System.getProperty("infinispan.config") != null) {
         config = System.getProperty("infinispan.config");
      }
      if (System.getProperty("infinispan.http.port") != null) {
         httpPort = Integer.parseInt(System.getProperty("infinispan.http.port"));
      }
      if (System.getProperty("infinispan.hotrod.port") != null) {
         hotrodPort = Integer.parseInt(System.getProperty("infinispan.hotrod.port"));
      }
      if (System.getProperty("infinispan.memcached.port") != null) {
         memcachedPort = Integer.parseInt(System.getProperty("infinispan.memcached.port"));
      }

      // Override with command line arguments.
      for (int i = 0 ; i < args.length ; ++i) {
         if (args[i].startsWith("--httpPort=")) {
            httpPort = Integer.parseInt(args[i].substring(11, args[i].length()));
         }
         else if (args[i].startsWith("--memcachedPort=")) {
            memcachedPort = Integer.parseInt(args[i].substring(16, args[i].length()));
         }
         else if (args[i].startsWith("--hotrodPort=")) {
            hotrodPort = Integer.parseInt(args[i].substring(13, args[i].length()));
         }
         else if (args[i].startsWith("--host=")) {
            host = args[i].substring(7, args[i].length());
         }
         else if (args[i].startsWith("--config=")) {
            config = args[i].substring(9, args[i].length());
         }
         else {
            System.err.println("Run with java -jar infinispan.war [options]");
            System.err.println("Options:");
            System.err.println("  --host=IP");
            System.err.println("  --config=infinispan-config-file.xml");
            System.err.println("  --httpPort=#");
            System.err.println("  --hotrodPort=#");
            System.err.println("  --memcachedPort=#");
            System.err.println("");
            System.err.println("  Unrecognized argument: " + args[i]);
            System.exit(255);
         }
      }

      // Write back to system properties.
      System.setProperty("infinispan.host", host);
      if (config != null) {
        System.setProperty("infinispan.config", config);
      }
      System.setProperty("infinispan.http.port", httpPort.toString());
      System.setProperty("infinispan.hotrod.port", hotrodPort.toString());
      System.setProperty("infinispan.memcached.port", memcachedPort.toString());
   }

   public static void main(String[] args) throws Exception
   {
      parseArgs(args);
      Server server = new Server();

      if (httpPort > 0) {
         ServerConnector  httpConnector = new ServerConnector(server);
         httpConnector.setPort(httpPort);
         httpConnector.setHost(host);
         server.addConnector(httpConnector);
      }

      ProtectionDomain domain = Main.class.getProtectionDomain();
      URL location = domain.getCodeSource().getLocation();

      WebAppContext webapp = new WebAppContext();
      webapp.setContextPath("/");
      webapp.setWar(location.toExternalForm());
      server.setHandler(webapp);
      server.start();
      server.join();
   }
}

package edu.umich.lib.infinispan;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.infinispan.manager.EmbeddedCacheManager;

import org.infinispan.server.hotrod.HotRodServer;
import org.infinispan.server.hotrod.configuration.HotRodServerConfiguration;
import org.infinispan.server.hotrod.configuration.HotRodServerConfigurationBuilder;

public class HotRodListener extends Listener {

   private final static String CONFIGURATION = "edu.umich.lib.infinispan.HotRodListener.CONFIGURATION";
   private final static String SERVER        = "edu.umich.lib.infinispan.HotRodListener.SERVER";
   private final static String HOTROD_HOST   = "infinispan.hotrod.host";
   private final static String HOTROD_PORT   = "infinispan.hotrod.port";

   public static HotRodServerConfiguration getConfiguration(ServletContext ctx) {
      return (HotRodServerConfiguration) ctx.getAttribute(CONFIGURATION);
   }

   private static void setServer(ServletContext ctx, HotRodServer server) {
      ctx.setAttribute(SERVER, server);
   }

   private static HotRodServer getServer(ServletContext ctx) {
      return (HotRodServer) ctx.getAttribute(SERVER);
   }

   public static void setConfiguration(ServletContext ctx, HotRodServerConfiguration cfg) {
      ctx.setAttribute(CONFIGURATION, cfg);
   }

   public HotRodServerConfiguration createConfiguration (ServletContext ctx) {
      HotRodServerConfigurationBuilder builder = new HotRodServerConfigurationBuilder();
      String port = getParameter(ctx, HOTROD_PORT);
      if (port != null) {
        builder.port(Integer.parseInt(port));
      }
      String host = getParameter(ctx, HOTROD_HOST);
      if (host != null) {
        builder.host(host);
      }
      HotRodServerConfiguration config = builder.build();
      setConfiguration(ctx, config);
      return config;
   }

   private HotRodServer createServer(ServletContext ctx) {
      HotRodServer server = new HotRodServer();
      setServer(ctx, server);
      return server;
   }

   @Override
   public void contextInitialized(ServletContextEvent sce) {
      synchronized (sce) {
         ServletContext ctx = sce.getServletContext();

         HotRodServer server = getServer(ctx);
         if (server == null) {
            server = createServer(ctx);
         }
            
         EmbeddedCacheManager cm = getCacheManager(ctx);
         if (cm == null) {
             cm = createCacheManager(ctx);
         }

         HotRodServerConfiguration config = getConfiguration(ctx);
         if (config == null) {
            config = createConfiguration(ctx);
         }
         server.start(config, cm);
      }
   }

   @Override
   public void contextDestroyed(ServletContextEvent sce) {
      synchronized (sce) {
         HotRodServer server = getServer(sce.getServletContext());
         if (server != null) {
            server.stop();
         }
         super.contextDestroyed(sce);
      }
   }
}

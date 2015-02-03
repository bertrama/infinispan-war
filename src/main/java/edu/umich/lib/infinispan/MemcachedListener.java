package edu.umich.lib.infinispan;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.infinispan.manager.EmbeddedCacheManager;

import org.infinispan.rest.logging.JavaLog;

import org.infinispan.server.memcached.MemcachedServer;
import org.infinispan.server.memcached.configuration.MemcachedServerConfiguration;
import org.infinispan.server.memcached.configuration.MemcachedServerConfigurationBuilder;

import org.infinispan.util.logging.LogFactory;

public class MemcachedListener extends Listener {

   private final static JavaLog log = LogFactory.getLog(MemcachedListener.class, JavaLog.class);

   private final static String CONFIGURATION = "edu.umich.lib.infinispan.MemcachedListener.CONFIGURATION";
   private final static String SERVER        = "edu.umich.lib.infinispan.MemcachedListener.SERVER";

   public static MemcachedServerConfiguration getConfiguration(ServletContext ctx) {
      return (MemcachedServerConfiguration) ctx.getAttribute(CONFIGURATION);
   }

   private static void setServer(ServletContext ctx, MemcachedServer server) {
      ctx.setAttribute(SERVER, server);
   }

   private static MemcachedServer getServer(ServletContext ctx) {
      return (MemcachedServer) ctx.getAttribute(SERVER);
   }

   public static void setConfiguration(ServletContext ctx, MemcachedServerConfiguration cfg) {
      ctx.setAttribute(CONFIGURATION, cfg);
   }

   public MemcachedServerConfiguration createConfiguration (ServletContext ctx) {
      MemcachedServerConfigurationBuilder builder = new MemcachedServerConfigurationBuilder();
      String port = getParameter(ctx, "infinispan.config.memcached.port");
      if (port != null) {
        builder.port(Integer.parseInt(port));
      }
      String host = getParameter(ctx, "infinispan.config.memcached.host");
      if (host != null) {
        builder.host(host);
      }

      MemcachedServerConfiguration config = builder.build();
      setConfiguration(ctx, config);
      return config;
   }

   private MemcachedServer createServer(ServletContext ctx) {
      MemcachedServer server = new MemcachedServer();
      setServer(ctx, server);
      return server;
   }

   @Override
   public void contextInitialized(ServletContextEvent sce) {
      synchronized (sce) {
         log.info("Starting MemcachedListener");
         ServletContext ctx = sce.getServletContext();

         MemcachedServer server = getServer(ctx);
         if (server == null) {
            server = createServer(ctx);
         }
            
         EmbeddedCacheManager cm = getCacheManager(ctx);
         if (cm == null) {
             cm = createCacheManager(ctx);
         }

         MemcachedServerConfiguration config = getConfiguration(ctx);
         if (config == null) {
            config = createConfiguration(ctx);
         }
         server.start(config, cm);
      }
   }

   @Override
   public void contextDestroyed(ServletContextEvent sce) {
      synchronized (sce) {
         MemcachedServer server = getServer(sce.getServletContext());
         if (server != null) {
            server.stop();
         }
         super.contextDestroyed(sce);
      }
   }
}

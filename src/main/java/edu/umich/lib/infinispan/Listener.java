package edu.umich.lib.infinispan;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.rest.ManagerInstance;
import org.infinispan.rest.configuration.ExtendedHeaders;
import org.infinispan.rest.configuration.RestServerConfiguration;
import org.infinispan.rest.configuration.RestServerConfigurationBuilder;
import org.infinispan.rest.logging.JavaLog;
import org.infinispan.util.logging.LogFactory;

/**
 * Initializes cache manager for the REST server and sets it into the servlet context.
 *
 * @author Michal Linhard (mlinhard@redhat.com)
 * @since 5.2
 */
public class Listener implements ServletContextListener {

   /**
    * The name of an Infinispan configuration file to load
    */
   public static final String INFINISPAN_CONFIG = "infinispan.config";

   protected final static JavaLog log = LogFactory.getLog(Listener.class, JavaLog.class);

   // Attributes attached to the ServletContext
   public final static String CACHE_MANAGER = "edu.umich.lib.infinispan.Listener.CACHE_MANAGER";
   public final static String MANAGER_INSTANCE = "edu.umich.lib.infinispan.Listener.MANAGER_INSTANCE";

   private final static String DEFAULT_CONFIG = "/WEB-INF/config.xml";

   public static String getParameter(ServletContext ctx, String name) {
      String ret = System.getProperty(name);
      if (ret == null) {
         ret = ctx.getInitParameter(name);
      }
      return ret;
   }

   public static void setCacheManager(ServletContext ctx, EmbeddedCacheManager cacheManager) {
      ctx.setAttribute(CACHE_MANAGER, cacheManager);
      ctx.setAttribute(MANAGER_INSTANCE, new ManagerInstance(cacheManager));
   }

   public static EmbeddedCacheManager getCacheManager(ServletContext ctx) {
      return (EmbeddedCacheManager) ctx.getAttribute(CACHE_MANAGER);
   }

   public static ManagerInstance getManagerInstance(ServletContext ctx) {
      return (ManagerInstance) ctx.getAttribute(MANAGER_INSTANCE);
   }

   private static EmbeddedCacheManager loadDefaultConfig(ServletContext ctx) {
      EmbeddedCacheManager cm;
      try {
         cm = new DefaultCacheManager(ctx.getResourceAsStream(DEFAULT_CONFIG));
      } catch (IOException e) {
         log.errorReadingConfigurationFile(e, "war:" + DEFAULT_CONFIG);
         cm = new DefaultCacheManager();
      }
      return cm;
   }

   protected static EmbeddedCacheManager createCacheManager(ServletContext ctx) {
      EmbeddedCacheManager cm;
      String cfgFile = getParameter(ctx, INFINISPAN_CONFIG);
      if (cfgFile == null) {
         cm = loadDefaultConfig(ctx);
      } else {
         try {
            cm = new DefaultCacheManager(cfgFile);
         } catch (IOException e) {
            log.errorReadingConfigurationFile(e, cfgFile);
            cm = loadDefaultConfig(ctx);
         }
      }
      setCacheManager(ctx, cm);
      return cm;
   }

   @Override
   public void contextInitialized(ServletContextEvent sce) { }

   @Override
   public void contextDestroyed(ServletContextEvent sce) {
      synchronized (sce) {
         EmbeddedCacheManager cm = getCacheManager(sce.getServletContext());
         if (cm != null) {
            cm.stop();
         }
      }
   }

   private Class<?> loadClass(String name) throws Exception {
      return Thread.currentThread().getContextClassLoader().loadClass(name);
   }

}

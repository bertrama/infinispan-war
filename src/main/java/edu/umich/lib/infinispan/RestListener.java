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

/**
 * Initializes cache manager for the REST server and sets it into the servlet context.
 *
 * @author Michal Linhard (mlinhard@redhat.com)
 * @since 5.2
 */
public class RestListener extends Listener {

   /**
    * Whether to allow returning extended metadata headers
    */
   private static final String EXTENDED_HEADERS = "extended.headers";

   private final static String CONFIGURATION = "edu.umich.lib.infinispan.RestListener.CONFIGURATION"; 

   public static RestServerConfiguration getConfiguration(ServletContext ctx) {
      return (RestServerConfiguration) ctx.getAttribute(CONFIGURATION);
   }

   public static void setConfiguration(ServletContext ctx, RestServerConfiguration cfg) {
      ctx.setAttribute(CONFIGURATION, cfg);
   }

   private RestServerConfiguration createConfiguration(ServletContext ctx) {
      RestServerConfigurationBuilder builder = new RestServerConfigurationBuilder();
      String extendedHeaders = ctx.getInitParameter(EXTENDED_HEADERS);
      if (extendedHeaders != null) {
         builder.extendedHeaders(ExtendedHeaders.valueOf(extendedHeaders));
      }
      RestServerConfiguration config = builder.build();
      setConfiguration(ctx, config);
      return config;
   }


   @Override
   public void contextInitialized(ServletContextEvent sce) {
      synchronized (sce) {
         ServletContext ctx = sce.getServletContext();
         // Try to obtain an externally injected CacheManager
         EmbeddedCacheManager cm = getCacheManager(ctx);

         // If cache manager is null, create one for REST server's own usage
         if (cm == null) {
            cm = createCacheManager(ctx);
         }

        // REST Server configuration
        if (getConfiguration(ctx) == null) {
           createConfiguration(ctx);
        }

        // Start defined caches to avoid issues with lazily started caches
        for (String cacheName : cm.getCacheNames())
           cm.getCache(cacheName);

        // Finally, start default cache as well
        cm.getCache();
      }
   }

   private Class<?> loadClass(String name) throws Exception {
      return Thread.currentThread().getContextClassLoader().loadClass(name);
   }

}

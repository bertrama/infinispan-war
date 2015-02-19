# Infinispan server/war

This project packages an infinispan rest, hotrod, and memcached server into a .war file.
It's based on the infinispan-rest.war, but adds in listeners for hotrod and memcached.
This is primarily intended to run at a relatively small scale for development and testing.

## Building

`mvn package` should build the war file.  I used version 3.2.5.

## Running

The .war file should run in a standard servlet container like jetty, tomcat or jboss.
Can also be run on the command line using an embedded jetty container:

```bash
java -jar infinispan.war
```

## Configuring

The command-line launcher accepts a few options:

```bash
java -jar infinispan.war \
  --host=IP \
  --config=infinispan-config-file.xml \
  --ajpPort=# \
  --httpPort=# \
  --hotrodPort=# \
  --memcachedPort=#
```

It'll also listen to java system properties:

```bash
infinispan.config=ininispan-config-file.xml
infinispan.host=IP
infinispan.ajp.port=#
infinispan.http.port=#
infinispan.hotrod.port=#
infinispan.memcached.port=#
```

Or as context init parameters in `WEB-INF/web.xml`:

```xml
<web-app>
  ...
  <context-param>
    <param-name>infininspan.config</param-name>
    <param-value>infinispan-config-file.xml</param-value>
  <context-param>
  ...
</webapp>
```

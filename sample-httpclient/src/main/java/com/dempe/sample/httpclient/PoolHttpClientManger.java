package com.dempe.sample.httpclient;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: Dempe
 * Date: 2016/11/10
 * Time: 15:22
 * To change this template use File | Settings | File Templates.
 */
public class PoolHttpClientManger {

    private static final Logger LOG = LoggerFactory.getLogger(PoolHttpClientManger.class);

    private final String URL_PREFIX;
    private final int MaxTotal;
    private final int DefaultMaxPerRoute;
    private final int ConnectionRequestTimeout;
    private final int ConnectTimeout;
    private final int SocketTimeout;
    private final int CACHE_TIMEOUT;

    final CloseableHttpClient client;

    public PoolHttpClientManger() {
        Properties properties = new Properties();
        try {
            properties.load(getClass().getClassLoader().getResourceAsStream("httpclient.properties"));
        } catch (IOException e) {
            LOG.warn(e.toString(), e);
        }

        URL_PREFIX = properties.getProperty("URL_PREFIX");
        MaxTotal = Integer.parseInt(properties.getProperty("MaxTotal"));
        DefaultMaxPerRoute = Integer.parseInt(properties.getProperty("DefaultMaxPerRoute"));
        ConnectionRequestTimeout = Integer.parseInt(properties.getProperty("ConnectionRequestTimeout"));
        ConnectTimeout = Integer.parseInt(properties.getProperty("ConnectTimeout"));
        SocketTimeout = Integer.parseInt(properties.getProperty("SocketTimeout"));
        CACHE_TIMEOUT = Integer.parseInt(properties.getProperty("CACHE_TIMEOUT"));

        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(MaxTotal);
        cm.setDefaultMaxPerRoute(DefaultMaxPerRoute);
        RequestConfig config = RequestConfig.custom().setConnectionRequestTimeout(ConnectionRequestTimeout)
                .setConnectTimeout(ConnectTimeout).setSocketTimeout(SocketTimeout).build();
        client = HttpClients.custom().setConnectionManager(cm).setDefaultRequestConfig(config).build();

        new IdleConnectionMonitorThread(cm).start();
    }


    public static void main(String[] args) {

    }

    private static class IdleConnectionMonitorThread extends Thread {
        private final HttpClientConnectionManager connMgr;
        private volatile boolean shutdown;

        public IdleConnectionMonitorThread(HttpClientConnectionManager connMgr) {
            super("IdleConnectionMonitorThread");
            this.connMgr = connMgr;
        }

        @Override
        public void run() {
            try {
                while (!shutdown) {
                    synchronized (this) {
                        wait(60000);
                        try {
                            LOG.info("closeExpiredConnections");
                            connMgr.closeExpiredConnections();
                            LOG.info("closeIdleConnections");
                            connMgr.closeIdleConnections(30, TimeUnit.SECONDS);
                        } catch (Exception e) {
                            LOG.warn(e.toString(), e);
                        }
                    }
                }
            } catch (InterruptedException e) {
                LOG.warn(e.toString(), e);
            }
        }

        @SuppressWarnings("unused")
        public void shutdown() {
            shutdown = true;
            synchronized (this) {
                notifyAll();
            }
        }
    }

}

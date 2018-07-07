package com.bulingbuu.common.http;

import java.util.HashMap;

public class ClientConfig extends HashMap<String, Object> {

    public static final String SSL_VERSION = "ssl.version";
    public static final Object SSL_VERSION_SCHEMA = String.class;
    public static final String DEFAULT_SSL_VERSION = "TLS";

    public static final String MAX_RETRY_TIMES = "max.retry.times";
    public static final Object MAX_RETRY_TIMES_SCHEMA = Integer.class;
    public static final int DEFULT_MAX_RETRY_TIMES = 3;

    public static final String READ_TIMEOUT = "read.timeout";
    public static final Object READ_TIMEOUT_SCHEMA = Integer.class;
    public static final int DEFAULT_READ_TIMEOUT = 30 * 1000;

    public static final String CONNECTION_REQUEST_TIMEOUT = "connection.request.timeout";
    public static final Object CONNECTION_REQUEST_TIMEOUT_SCHEMA = Integer.class;
    public static final int DEFAULT_CONNECTION_REQUEST_TIMEOUT = 10 * 1000;

    public static final String CONNECTION_TIMEOUT = "connection.timeout";
    public static final Object CONNECTION_TIMEOUT_SCHEMA = Integer.class;
    public static final int DEFAULT_CONNECTION_TIMEOUT = 5 * 1000;

    public static final String SOCKET_TIMEOUT = "socket.timeout";
    public static final Object SOCKET_TIMEOUT_SCHEMA = Integer.class;
    public static final int DEFAULT_SOCKET_TIMEOUT = 10 * 1000;

    /**
     * Global APNs environment setting.
     * Setting to -1, if you want to use PushPayload Options.
     * Default value is -1.
     * Setting to 0, if you want to use global setting as development environment.
     * Setting to 1, if you want to use global setting as production environment.
     *
     */
    public static final String APNS_PRODUCTION = "apns.production";
    public static final Object APNS_PRODUCTION_SCHEMA = Integer.class;
    public static final int DEFAULT_APNS_PRODUCTION = -1;

    /**
     * Global time_to_live setting. Time unit is second.
     * Setting to -1, if you want to use PushPayload Options.
     * Default value is -1.
     * It will override PushPayload Options, while it is a positive integer value.
     */
    public static final String TIME_TO_LIVE = "time.to.live";
    public static final Object TIME_TO_LIVE_SCHEMA = Long.class;
    public static final long DEFAULT_TIME_TO_LIVE = -1;

    private static ClientConfig instance = new ClientConfig();

    private ClientConfig() {
        super(16);

        this.put(SSL_VERSION, DEFAULT_SSL_VERSION);
        this.put(MAX_RETRY_TIMES, DEFULT_MAX_RETRY_TIMES);
        this.put(READ_TIMEOUT, DEFAULT_READ_TIMEOUT);
        this.put(CONNECTION_REQUEST_TIMEOUT, DEFAULT_CONNECTION_REQUEST_TIMEOUT);
        this.put(CONNECTION_TIMEOUT, DEFAULT_CONNECTION_TIMEOUT);
        this.put(SOCKET_TIMEOUT, DEFAULT_SOCKET_TIMEOUT);

        this.put(APNS_PRODUCTION, DEFAULT_APNS_PRODUCTION);
        this.put(TIME_TO_LIVE, DEFAULT_TIME_TO_LIVE);

    }

    public static ClientConfig getInstance() {
        return instance;
    }


    public void setSSLVersion(String sslVer) {
        this.put(SSL_VERSION, sslVer);
    }

    public void setMaxRetryTimes(int maxRetryTimes) {
        this.put(MAX_RETRY_TIMES, maxRetryTimes);
    }

    public void setReadTimeout(int readTimeout) {
        this.put(READ_TIMEOUT, readTimeout);
    }

    public void setConnectionRequestTimeout(int timeout) {
        this.put(CONNECTION_REQUEST_TIMEOUT, timeout);
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.put(CONNECTION_TIMEOUT, connectionTimeout);
    }

    public void setSocketTimeout(int socketTimeout) {
        this.put(SOCKET_TIMEOUT, socketTimeout);
    }

    public String getSSLVersion() {
        return (String) this.get(SSL_VERSION);
    }

    public Integer getMaxRetryTimes() {
        return (Integer) this.get(MAX_RETRY_TIMES);
    }

    public Integer getReadTimeout() {
        return (Integer) this.get(READ_TIMEOUT);
    }

    public Integer getConnectionRequestTimeout() {
        return (Integer) this.get(CONNECTION_REQUEST_TIMEOUT);
    }

    public Integer getConnectionTimeout() {
        return (Integer) this.get(CONNECTION_TIMEOUT);
    }

    public Integer getSocketTimeout() {
        return (Integer) this.get(SOCKET_TIMEOUT);
    }

    public void setApnsProduction(boolean production) {
        if(production) {
            this.put(APNS_PRODUCTION, 1);
        } else {
            this.put(APNS_PRODUCTION, 0);
        }
    }

    public void setTimeToLive(long timeToLive) {
        this.put(TIME_TO_LIVE, timeToLive);
    }

    public void setGlobalPushSetting(boolean apnsProduction, long timeToLive) {
        setApnsProduction(apnsProduction);
        setTimeToLive(timeToLive);
    }
}

package com.bulingbuu.common.http.connection;

import com.bulingbuu.common.http.ClientConfig;
import com.bulingbuu.common.http.resp.APIConnectionException;
import com.bulingbuu.common.http.resp.APIRequestException;
import com.bulingbuu.common.http.resp.ResponseWrapper;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslContext;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.concurrent.CountDownLatch;

import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;


public class NettyHttpClient implements IHttpClient {

    private static Logger LOG = LoggerFactory.getLogger(NettyHttpClient.class);

    private String _authCode;
    private int _maxRetryTimes;
    private int _readTimeout;
    private Channel _channel;
    private Bootstrap b;
    private EventLoopGroup _workerGroup;
    private SslContext _sslCtx;
    private int _workerThreadNum;


    public NettyHttpClient(HttpProxy proxy, ClientConfig config) {
        _maxRetryTimes = config.getMaxRetryTimes();
        _readTimeout = config.getReadTimeout();
        String message = MessageFormat.format("Created instance with "
                        + "connectionTimeout {0}, readTimeout {1}, maxRetryTimes {2}, SSL Version {3}",
                config.getConnectionTimeout(), _readTimeout, _maxRetryTimes, config.getSSLVersion());
        LOG.debug(message);

//        try {
//            _sslCtx = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
//        } catch (SSLException e) {
//            e.printStackTrace();
//        }
        _workerGroup = new NioEventLoopGroup(_workerThreadNum);
        b = new Bootstrap(); // (1)
        b.group(_workerGroup); // (2)
        b.channel(NioSocketChannel.class); // (3)
        b.option(ChannelOption.SO_KEEPALIVE, true); // (4)

    }

    public void sendRequest(HttpMethod method, String content, URI uri, BaseCallback callback) {
        FullHttpRequest request;
        b = new Bootstrap();
        if (b.group() == null) {
            b.group(_workerGroup);
        }
        b.channel(NioSocketChannel.class);
        b.option(ChannelOption.SO_KEEPALIVE, true);
        b.handler(new NettyClientInitializer(_sslCtx, callback, null));
        String scheme = uri.getScheme() == null ? "http" : uri.getScheme();
        int port = uri.getPort();
        if (port == -1) {
            if ("http".equalsIgnoreCase(scheme)) {
                port = 80;
            } else if ("https".equalsIgnoreCase(scheme)) {
                port = 443;
            }
        }
        _channel = b.connect(uri.getHost(), port).syncUninterruptibly().channel();
        if (null != content) {
            ByteBuf byteBuf = Unpooled.copiedBuffer(content.getBytes(CharsetUtil.UTF_8));
            request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, method, uri.getRawPath(), byteBuf);
            request.headers().set(HttpHeaderNames.CONTENT_LENGTH, (long) byteBuf.readableBytes());
        } else {
            request = new DefaultFullHttpRequest(HTTP_1_1, method, uri.getRawPath());
        }
        request.headers().set(HttpHeaderNames.HOST, uri.getHost());
        request.headers().set(HttpHeaderNames.AUTHORIZATION, _authCode);
        request.headers().set("Content-Type", "application/json;charset=utf-8");

        LOG.info("Sending request. " + request);
        LOG.info("Send body: " + content);
        _channel.writeAndFlush(request);
        try {
            _channel.closeFuture().sync();
            _workerGroup.shutdownGracefully();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Override
    public ResponseWrapper sendGet(String url) throws APIConnectionException, APIRequestException {
        return sendGet(url, null);
    }

    public ResponseWrapper sendGet(String url, String content) throws APIConnectionException, APIRequestException {
        ResponseWrapper wrapper = new ResponseWrapper();
        try {
            return sendHttpRequest(HttpMethod.GET, url, content);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return wrapper;
    }

    @Override
    public ResponseWrapper sendPut(String url, String content) throws APIConnectionException, APIRequestException {
        ResponseWrapper wrapper = new ResponseWrapper();
        try {
            return sendHttpRequest(HttpMethod.PUT, url, content);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return wrapper;
    }

    @Override
    public ResponseWrapper sendPost(String url, String content) throws APIConnectionException, APIRequestException {
        ResponseWrapper wrapper = new ResponseWrapper();
        try {
            return sendHttpRequest(HttpMethod.POST, url, content);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return wrapper;
    }

    @Override
    public ResponseWrapper sendDelete(String url) throws APIConnectionException, APIRequestException {
        return sendDelete(url, null);
    }

    public ResponseWrapper sendDelete(String url, String content) throws APIConnectionException, APIRequestException {
        ResponseWrapper wrapper = new ResponseWrapper();
        try {
            return sendHttpRequest(HttpMethod.DELETE, url, content);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return wrapper;
    }


    private ResponseWrapper sendHttpRequest(HttpMethod method, String url, String body) throws APIConnectionException,
            APIRequestException, URISyntaxException {
        CountDownLatch latch = new CountDownLatch(1);
        NettyClientInitializer initializer = new NettyClientInitializer(_sslCtx, null, latch);
        b.handler(initializer);
        ResponseWrapper wrapper = new ResponseWrapper();
        URI uri = new URI(url);
        String scheme = uri.getScheme() == null ? "http" : uri.getScheme();
        String host = uri.getHost() == null ? "127.0.0.1" : uri.getHost();
        int port = uri.getPort();
        if (port == -1) {
            if ("http".equalsIgnoreCase(scheme)) {
                port = 80;
            } else if ("https".equalsIgnoreCase(scheme)) {
                port = 443;
            }
        }

        try {
            ChannelFuture connect = b.connect(host, port);
            _channel = connect.sync().channel();
            FullHttpRequest request;
            if (null != body) {
                ByteBuf byteBuf = Unpooled.copiedBuffer(body.getBytes(CharsetUtil.UTF_8));
                request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, method, uri.getRawPath(), byteBuf);
                request.headers().set(HttpHeaderNames.CONTENT_LENGTH, (long) byteBuf.readableBytes());
            } else {
                request = new DefaultFullHttpRequest(HTTP_1_1, method, uri.getRawPath());
            }
            request.headers().set(HttpHeaderNames.HOST, uri.getHost());
            request.headers().set("Content-Type", "application/json;charset=utf-8");
            connect.awaitUninterruptibly();
            LOG.info("Sending request. " + request);
            LOG.info("Send body: " + body);
            _channel.writeAndFlush(request);
            latch.await();
            wrapper = initializer.getResponse();
            int status = wrapper.responseCode;
            String responseContent = wrapper.responseContent;
            if (status >= 200 && status < 300) {
                LOG.debug("Succeed to get response OK - responseCode:" + status);
                LOG.debug("Response Content - " + responseContent);

            } else if (status >= 300 && status < 400) {
                LOG.warn("Normal response but unexpected - responseCode:" + status + ", responseContent:" + responseContent);

            } else {
                LOG.warn("Got error response - responseCode:" + status + ", responseContent:" + responseContent);

                switch (status) {
                    case 400:
                        LOG.error("Your request params is invalid. Please check them according to error message.");
                        wrapper.setErrorObject();
                        break;
                    case 401:
                        LOG.error("Authentication failed! Please check authentication params according to docs.");
                        wrapper.setErrorObject();
                        break;
                    case 403:
                        LOG.error("Request is forbidden! Maybe your appkey is listed in blacklist or your params is invalid.");
                        wrapper.setErrorObject();
                        break;
                    case 404:
                        LOG.error("Request page is not found! Maybe your params is invalid.");
                        wrapper.setErrorObject();
                        break;
                    case 410:
                        LOG.error("Request resource is no longer in service. Please according to notice on official website.");
                        wrapper.setErrorObject();
                    case 429:
                        LOG.error("Too many requests! Please review your appkey's request quota.");
                        wrapper.setErrorObject();
                        break;
                    case 500:
                    case 502:
                    case 503:
                    case 504:
                        LOG.error("Seems encountered server error. Maybe JPush is in maintenance? Please retry later.");
                        break;
                    default:
                        LOG.error("Unexpected response.");
                }
                throw new APIRequestException(wrapper);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return wrapper;
    }

    public void send(ByteBuf body, HttpMethod method, URI uri) {
        String scheme = uri.getScheme() == null ? "http" : uri.getScheme();
        String host = uri.getHost() == null ? "127.0.0.1" : uri.getHost();
        int port = uri.getPort();
        if (port == -1) {
            if ("http".equalsIgnoreCase(scheme)) {
                port = 80;
            } else if ("https".equalsIgnoreCase(scheme)) {
                port = 443;
            }
        }
        _channel = b.connect(host, port).syncUninterruptibly().channel();
        HttpRequest request;
        if (null != body) {
            request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, method, uri.getRawPath(), body);
            request.headers().set(HttpHeaderNames.CONTENT_LENGTH, (long) body.readableBytes());
        } else {
            request = new DefaultFullHttpRequest(HTTP_1_1, method, uri.getRawPath());
        }
        request.headers().set(HttpHeaderNames.HOST, uri.getHost());
        request.headers().set(HttpHeaderNames.AUTHORIZATION, _authCode);
        request.headers().set("Content-Type", "application/json;charset=utf-8");
        LOG.info("Sending request. " + request);
        LOG.info("Send body: " + body);
        _channel.writeAndFlush(request);
    }

    public int get_workerThreadNum() {
        return _workerThreadNum;
    }

    public void set_workerThreadNum(int _workerThreadNum) {
        this._workerThreadNum = _workerThreadNum;
    }

    public void close() {
        if (null != _channel) {
            _channel.closeFuture().syncUninterruptibly();
            _workerGroup.shutdownGracefully();
            _channel = null;
            _workerGroup = null;
        }
        System.out.println("Finished request(s)");
    }

    public interface BaseCallback {
        public void onSucceed(ResponseWrapper wrapper);
    }
}

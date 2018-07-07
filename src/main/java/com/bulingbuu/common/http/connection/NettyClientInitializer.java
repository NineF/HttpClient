package com.bulingbuu.common.http.connection;

import com.bulingbuu.common.http.resp.ResponseWrapper;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.ssl.SslContext;

import java.util.concurrent.CountDownLatch;

public class NettyClientInitializer extends ChannelInitializer<SocketChannel> {

    private SslContext _sslCtx;
    private NettyHttpClient.BaseCallback _callback;
    private CountDownLatch _latch;
    private HttpResponseHandler _handler;

    public NettyClientInitializer(SslContext sslContext, NettyHttpClient.BaseCallback callback, CountDownLatch latch) {
        this._sslCtx = sslContext;
        this._callback = callback;
        this._latch = latch;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        this._handler = new HttpResponseHandler(_callback, _latch);
        if(_sslCtx!=null) {
            socketChannel.pipeline().addLast(_sslCtx.newHandler(socketChannel.alloc()), new HttpClientCodec(), _handler);
        }else {
            socketChannel.pipeline().addLast(new HttpRequestDecoder());
            socketChannel.pipeline().addLast(new HttpResponseEncoder());
            socketChannel.pipeline().addLast(_handler);
        }
    }

    public void resetLatch(CountDownLatch latch) {
        _handler.resetLatch(latch);
    }

    public ResponseWrapper getResponse() {
        return _handler.getResponse();
    }
}


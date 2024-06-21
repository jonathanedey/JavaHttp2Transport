package com.jedey.ReactorNettyHttp2Transport;

import reactor.netty.http.HttpProtocol;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.HttpClient.RequestSender;
import reactor.netty.http.Http2SslContextSpec;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.LowLevelHttpRequest;

import io.netty.handler.codec.http.HttpMethod;

public class ReactorNettyHttp2Transport extends HttpTransport{
    public final HttpClient http2Client;

    public ReactorNettyHttp2Transport() {
        
        this.http2Client = HttpClient.create()
            .protocol(HttpProtocol.H2)
            .secure(sslContextSpec -> sslContextSpec.sslContext(Http2SslContextSpec.forClient()));
        System.out.println("Cold");
        this.http2Client.warmup().block();
        System.out.println("Warm");
    }

    @Override
    protected LowLevelHttpRequest buildRequest(String method, String url) {
        RequestSender requestSender = http2Client.request(HttpMethod.valueOf(method)).uri(url);
        return new ReactorNettyHttp2Request(requestSender);
    }
}
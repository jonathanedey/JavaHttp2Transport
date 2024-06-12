package com.jedey.ReactorNettyHttp2Transport;

import reactor.netty.http.HttpProtocol;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.HttpClient.RequestSender;

import java.security.cert.CertificateException;

import javax.net.ssl.SSLException;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.LowLevelHttpRequest;

import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http2.Http2SecurityUtil;
import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.handler.ssl.ApplicationProtocolConfig.Protocol;
import io.netty.handler.ssl.ApplicationProtocolConfig.SelectedListenerFailureBehavior;
import io.netty.handler.ssl.ApplicationProtocolConfig.SelectorFailureBehavior;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.ssl.SupportedCipherSuiteFilter;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.ssl.util.SelfSignedCertificate;

public class ReactorNettyHttp2Transport extends HttpTransport{
    public final HttpClient http2Client;

    public ReactorNettyHttp2Transport() {
        this.http2Client = HttpClient.create()
            .protocol(HttpProtocol.H2)
            .secure((sslContextSpec) -> {
                try {
                    sslContextSpec.sslContext(createSSLContext(false));
                } catch (SSLException | CertificateException e) {
                    e.printStackTrace();
                }
            });
    }

    @Override
    protected LowLevelHttpRequest buildRequest(String method, String url) {
        RequestSender requestSender = http2Client.request(HttpMethod.valueOf(method)).uri(url);
        return new ReactorNettyHttp2Request(http2Client, requestSender);
    }

    public static SslContext createSSLContext(boolean isServer) throws SSLException, CertificateException {

        SslContext sslCtx;

        SelfSignedCertificate ssc = new SelfSignedCertificate();

        if (isServer) {
            sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey())
                .sslProvider(SslProvider.JDK)
                .ciphers(Http2SecurityUtil.CIPHERS, SupportedCipherSuiteFilter.INSTANCE)
                .applicationProtocolConfig(new ApplicationProtocolConfig(Protocol.ALPN,
                    SelectorFailureBehavior.NO_ADVERTISE,
                    SelectedListenerFailureBehavior.ACCEPT, ApplicationProtocolNames.HTTP_2, ApplicationProtocolNames.HTTP_1_1))
                .build();
        } else {
            sslCtx = SslContextBuilder.forClient()
                .sslProvider(SslProvider.JDK)
                .ciphers(Http2SecurityUtil.CIPHERS, SupportedCipherSuiteFilter.INSTANCE)
                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                .applicationProtocolConfig(new ApplicationProtocolConfig(Protocol.ALPN,
                    SelectorFailureBehavior.NO_ADVERTISE,
                    SelectedListenerFailureBehavior.ACCEPT, ApplicationProtocolNames.HTTP_2))
                .build();
        }
        return sslCtx;
    }
}
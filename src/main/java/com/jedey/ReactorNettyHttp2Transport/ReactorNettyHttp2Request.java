package com.jedey.ReactorNettyHttp2Transport;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.http.LowLevelHttpResponse;

import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClientResponse;
import reactor.util.function.Tuple2;
import reactor.netty.http.client.HttpClient.RequestSender;

public class ReactorNettyHttp2Request extends LowLevelHttpRequest {
    private RequestSender requestSender;
    private HttpHeaders headers;

    ReactorNettyHttp2Request(RequestSender requestSender) {
        this.requestSender = requestSender;
        this.headers = new DefaultHttpHeaders();
    }

    @Override
    public void addHeader(String name, String value) {
        headers.add(name, value);
    }

    @SuppressWarnings("deprecation")
    @Override
    public LowLevelHttpResponse execute() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        getStreamingContent().writeTo(baos);
        byte[] bytes = baos.toByteArray();

        try {
            CompletableFuture<Tuple2<HttpClientResponse,InputStream>> responseFuture = requestSender.send((request, outbound) -> {
                request.headers(headers);
                return outbound.sendByteArray(Mono.just(bytes)).then();
            }).responseSingle((res, contextBytes) -> {
                return Mono.zip(Mono.just(res), contextBytes.asInputStream());
            }).toFuture();

            final Tuple2<HttpClientResponse, InputStream> responseTuple = responseFuture.get();
            final HttpClientResponse response = responseTuple.getT1();
            final InputStream stream = responseTuple.getT2();
            return new ReactorNettyHttp2Response(response, stream);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new IOException("Error making request", e);
        }
    }
}

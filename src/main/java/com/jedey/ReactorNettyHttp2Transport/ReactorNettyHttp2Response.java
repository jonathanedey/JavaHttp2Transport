package com.jedey.ReactorNettyHttp2Transport;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map.Entry;

import com.google.api.client.http.LowLevelHttpResponse;

import io.netty.handler.codec.http.HttpHeaderNames;
import reactor.netty.http.client.HttpClientResponse;

public class ReactorNettyHttp2Response extends LowLevelHttpResponse {
    
    private final HttpClientResponse response;
    private final List<Entry<String, String>> allHeaders;
    private final InputStream stream;

    ReactorNettyHttp2Response(HttpClientResponse response, InputStream stream) {
        this.response = response;
        this.stream = stream;
        this.allHeaders = response.responseHeaders().entries();
    }

    @Override
    public InputStream getContent() throws IOException {
        return stream;
    }

    @Override
    public String getContentEncoding() throws IOException {
        return response.responseHeaders().get(HttpHeaderNames.CONTENT_ENCODING);
    }

    @Override
    public long getContentLength() throws IOException {
        return Long.parseLong(response.responseHeaders().get(HttpHeaderNames.CONTENT_LENGTH));
    }

    @Override
    public String getContentType() throws IOException {
        return response.responseHeaders().get(HttpHeaderNames.CONTENT_TYPE);
    }

    @Override
    public String getStatusLine() throws IOException {
        return response.status().toString();
    }

    @Override
    public int getStatusCode() throws IOException {
        return response.status().code();
    }

    @Override
    public String getReasonPhrase() throws IOException {
        return response.status().reasonPhrase();
    }

    @Override
    public int getHeaderCount() throws IOException {
        return allHeaders.size();
    }

    @Override
    public String getHeaderName(int index) throws IOException {
        return allHeaders.get(index).getKey();
    }

    @Override
    public String getHeaderValue(int index) throws IOException {
        return allHeaders.get(index).getValue();
    }
}

package com.jedey.SendEachBenchmark;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.BenchmarkParams;

import com.google.api.client.http.apache.v2.ApacheHttpTransport;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.jedey.ApacheHttp2Transport.ApacheHttp2Transport;
import com.jedey.ReactorNettyHttp2Transport.ReactorNettyHttp2Transport;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("deprecation")
@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
@Fork(1)
@Warmup(iterations = 1)
@Measurement(iterations = 5)
public class JMHSendEachBenchmark {
    List<Message> messages;
    FirebaseApp app;
    int message_count = 500;


    public FirebaseApp setup_admin_default() throws IOException {
        FileInputStream serviceAccount = new FileInputStream("src/main/resources/cert.json");

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

        return FirebaseApp.initializeApp(options);
    }

    public FirebaseApp setup_admin_apache_http1() throws IOException {
        FileInputStream serviceAccount = new FileInputStream("src/main/resources/cert.json");

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setHttpTransport(new ApacheHttpTransport())
                .build();

        return FirebaseApp.initializeApp(options);
    }

    public FirebaseApp setup_admin_apache_http2() throws IOException {
        FileInputStream serviceAccount = new FileInputStream("src/main/resources/cert.json");

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setHttpTransport(new ApacheHttp2Transport())
                .build();

        return FirebaseApp.initializeApp(options);
    }

    @Setup()
    public void setup(BenchmarkParams benchmarkParams) throws IOException {
        FileInputStream serviceAccount = new FileInputStream("src/main/resources/cert.json");
        FirebaseOptions.Builder options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccount));

        messages = new ArrayList<>(message_count);
        for (int i = 0; i < message_count; i++) {
            Message message = Message.builder()
                .setTopic(String.format("foo-bar-%d", i % 10))
                .build();
                messages.add(message);
        }

        String benchmarkName = benchmarkParams.getBenchmark();
        System.out.println("Name: " + benchmarkName);
        if (benchmarkName == "benchmark_send_each_apache_http2_default") {
            options.setHttpTransport(new ApacheHttp2Transport());
        } else if (benchmarkName == "benchmark_send_each_apache_http2_custom"){
            options.setHttpTransport(new ApacheHttp2Transport(true));
        } else if (benchmarkName == "benchmark_send_each_apache_http1"){
            options.setHttpTransport(new ApacheHttpTransport());
        }else if (benchmarkName == "benchmark_send_all_apache_http1"){
            options.setHttpTransport(new ApacheHttpTransport());
        } else if (benchmarkName == "benchmark_send_all_netty_http2") {
            options.setHttpTransport(new ReactorNettyHttp2Transport());
        }
        app = FirebaseApp.initializeApp(options.build());
    }

    @TearDown()
    public void tearDown(){
        app.delete();
    }

    // Benchmarks
    // @Benchmark
    // public void benchmark_send_each_apache_http2_default() throws FirebaseMessagingException {
    //     System.out.println("benchmark_send_each_apache_http2_default");

    //     BatchResponse response = FirebaseMessaging.getInstance().sendEach(messages, true);
    //     System.out.println("Dry Run Response: " + response.getSuccessCount());
    // }

    // @Benchmark
    // public void benchmark_send_each_apache_http2_custom() throws FirebaseMessagingException {
    //     System.out.println("benchmark_send_each_apache_http2_custom");

    //     BatchResponse response = FirebaseMessaging.getInstance().sendEach(messages, true);
    //     System.out.println("Dry Run Response: " + response.getSuccessCount());
    // }

    // @Benchmark
    // public void benchmark_send_each_apache_http1() throws FirebaseMessagingException {
    //     System.out.println("benchmark_send_each_apache_http1");

    //     BatchResponse response = FirebaseMessaging.getInstance().sendEach(messages, true);
    //     System.out.println("Dry Run Response: " + response.getSuccessCount());
    // }

    // @Benchmark
    // public void benchmark_send_all_apache_http1() throws FirebaseMessagingException {
    //     System.out.println("benchmark_send_all_apache_http1");

    //     BatchResponse response = FirebaseMessaging.getInstance().sendAll(messages, true);
    //     System.out.println("Dry Run Response: " + response.getSuccessCount());
    // }

    // @Benchmark
    // public void benchmark_send_each_default() throws FirebaseMessagingException {
    //     System.out.println("benchmark_send_each_apache_http1");

    //     BatchResponse response = FirebaseMessaging.getInstance().sendEach(messages, true);
    //     System.out.println("Dry Run Response: " + response.getSuccessCount());
    // }

    // @Benchmark
    // public void benchmark_send_all_default() throws FirebaseMessagingException {
    //     System.out.println("benchmark_send_all_apache_http1");

    //     BatchResponse response = FirebaseMessaging.getInstance().sendAll(messages, true);
    //     System.out.println("Dry Run Response: " + response.getSuccessCount());
    // }

    @Benchmark
    public void benchmark_send_all_netty_http2() throws FirebaseMessagingException {
        System.out.println("benchmark_send_each_netty_http2");

        BatchResponse response = FirebaseMessaging.getInstance().sendEach(messages, true);
        System.out.println("Dry Run Response: " + response.getSuccessCount());
    }
}

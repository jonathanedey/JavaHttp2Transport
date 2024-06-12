package com.jedey;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.google.api.client.http.apache.v2.ApacheHttpTransport;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.jedey.ApacheHttp2Transport.ApacheHttp2Transport;
import com.jedey.ReactorNettyHttp2Transport.ReactorNettyHttp2Transport;
import com.google.firebase.messaging.*;

import java.util.ArrayList;
import java.util.List;

public class MyApp {

    public static FirebaseApp setup_admin_default() throws IOException {
        FileInputStream serviceAccount = new FileInputStream("src/main/resources/cert.json");
        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();
        return FirebaseApp.initializeApp(options);
    }

    public static FirebaseApp setup_admin_apache_http1() throws IOException {
        FileInputStream serviceAccount = new FileInputStream("src/main/resources/cert.json");

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setHttpTransport(new ApacheHttpTransport())
                .build();

        return FirebaseApp.initializeApp(options);
    }

    public static FirebaseApp setup_admin_apache_http2() throws IOException {
        FileInputStream serviceAccount = new FileInputStream("src/main/resources/cert.json");

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setHttpTransport(new ApacheHttp2Transport())
                .build();

        return FirebaseApp.initializeApp(options);
    }

    public static FirebaseApp setup_admin_netty_http2() throws IOException {
        FileInputStream serviceAccount = new FileInputStream("src/main/resources/cert.json");

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setHttpTransport(new ReactorNettyHttp2Transport())
                .build();

        return FirebaseApp.initializeApp(options);
    }

    public static List<Message> get_messages(int message_count) {
        List<Message> messages = new ArrayList<>(message_count);
        for (int i = 0; i < message_count; i++) {
            Message message = Message.builder()
                .setTopic(String.format("foo-bar-%d", i % 10))
                .build();
                messages.add(message);
        }
        return messages;
    }


    public static void benchmark_send_each(List<Message> messages, int numRequests, FirebaseApp app) throws FileNotFoundException, IOException, FirebaseMessagingException {
        System.out.println("\nsendEach()");
        for (int i = 0; i < 1; i++) {
            FirebaseMessaging.getInstance().sendEach(messages, true);
        }

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < numRequests; i++) {
            BatchResponse response = FirebaseMessaging.getInstance().sendEach(messages, true);
            System.out.println("Dry Run Response: " + response.getSuccessCount());
        }

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        double averageTime = (double) totalTime / numRequests;
    
        System.out.println("Total time: " + totalTime + " ms");
        System.out.println("Average time per request: " + averageTime + " ms");

        // app.delete();
    }

    @SuppressWarnings("deprecation")
    public static void benchmark_send_all(List<Message> messages, int numRequests, FirebaseApp app) throws FileNotFoundException, IOException, FirebaseMessagingException {
        System.out.println("\nsendAll()");
        for (int i = 0; i < numRequests; i++) {
            FirebaseMessaging.getInstance().sendEach(messages, true);
        }

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < numRequests; i++) {
            BatchResponse response = FirebaseMessaging.getInstance().sendAll(messages, true);
            System.out.println("Dry Run Response: " + response.getSuccessCount());
        }

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        double averageTime = (double) totalTime / numRequests;

        System.out.println("Total time: " + totalTime + " ms");
        System.out.println("Average time per request: " + averageTime + " ms");

        // app.delete();
    }

    public static void main(String[] args) throws FirebaseMessagingException, IOException{

        List<Message> messages = get_messages(500);
        int numRequests = 1; // Number of time to loop
        FirebaseApp app;

        app = setup_admin_netty_http2();
        benchmark_send_each(messages, numRequests, app);
        app.delete();

        // System.out.println("\n\nHTTP1");
        // app = setup_admin_apache_http1();
        // // benchmark_send_all(messages, numRequests, app);
        // benchmark_send_each(messages, numRequests, app);
        // app.delete();

        // System.out.println("\n\nHTTP2");
        // app = setup_admin_apache_http2();
        // // benchmark_send_all(messages, numRequests, app);
        // benchmark_send_each(messages, numRequests, app);
        // app.delete();


        // System.out.println("\n\nHTTP1");
        // app = setup_admin_apache_http1();
        // benchmark_send_each(messages, numRequests, app);
        // // benchmark_send_all(messages, numRequests, app);
        // app.delete();


        // // System.out.println("\n\nHTTP2");
        // app = setup_admin_apache_http2();
        // // benchmark_send_all(messages, numRequests, app);
        // benchmark_send_each(messages, numRequests, app);
        // app.delete();
    }
}
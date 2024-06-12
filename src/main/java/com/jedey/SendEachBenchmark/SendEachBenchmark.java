package com.jedey.SendEachBenchmark;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.jedey.ApacheHttp2Transport.ApacheHttp2Transport;

public class SendEachBenchmark {
    private static int message_count = 500;
    private static List<Message> messages;
    private static FirebaseApp app;

    public static void get_messages() {
        messages = new ArrayList<>(message_count);
        for (int i = 0; i < message_count; i++) {
            Message message = Message.builder()
                .setTopic(String.format("foo-bar-%d", i % 10))
                .build();
                messages.add(message);
        }
    }

    public static void setup_admin() throws IOException {
        FileInputStream serviceAccount = new FileInputStream("src/main/resources/cert.json");

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setHttpTransport(new ApacheHttp2Transport())
                .build();

        app = FirebaseApp.initializeApp(options);
    }

    public static BatchResponse executeRequest() throws FirebaseMessagingException, InterruptedException, ExecutionException {
        BatchResponse response = FirebaseMessaging.getInstance(app).sendEach(messages, true);
        System.out.println("Successfully sent message count: " + response.getSuccessCount());
        return response;
    }

    public static BatchResponse executeRequestAsync() throws FirebaseMessagingException, InterruptedException, ExecutionException {
        ApiFuture<BatchResponse> responseFuture = FirebaseMessaging.getInstance(app).sendEachAsync(messages, true);
        BatchResponse response = responseFuture.get();
        System.out.println("Successfully sent message count: " + response.getSuccessCount());
        return response;
    }

    public static void main(String[] args) throws FirebaseMessagingException, IOException, InterruptedException, ExecutionException{
        int numRequests = 5; // Number of requests to execute
        get_messages();
        setup_admin();
        long startTime = System.currentTimeMillis();
    
        for (int i = 0; i < numRequests; i++) {
            executeRequestAsync();
        }
    
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        double averageTime = (double) totalTime / numRequests;
    
        System.out.println("Total time: " + totalTime + " ms");
        System.out.println("Average time per request: " + averageTime + " ms");
    }
}

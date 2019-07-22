package com.siyicai.grpc.greeting.client;

import com.proto.calculator.CalculatorServiceGrpc;
import com.proto.greet.*;
import io.grpc.*;
import io.grpc.stub.StreamObserver;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class GreetingClient {

    public void run () {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext() //to avoid ssl, not recommended for production
                .build();

        //doUnaryCall(channel);
        //doServerStreamingCall(channel);
        //doClientStreamingCall(channel);
        //doBiDiStreamingCall(channel);

        doUnaryCallWithDeadline(channel);

        System.out.println("Shutting down channel");
        channel.shutdown();
    }
    public static void main(String[] args) {
        System.out.println("Hello, I am a gRPC client");

        System.out.println("Creating a stub");

        GreetingClient main = new GreetingClient();

        main.run();

        //GreetServiceGrpc.GreetServiceFutureStub greetClient = GreetServiceGrpc.newFutureStub(channel);

    }

    private static void doServerStreamingCall(ManagedChannel channel) {
        //Create Stub       //create a greet service client (blocking - synchronous)
        GreetServiceGrpc.GreetServiceBlockingStub greetClient = GreetServiceGrpc.newBlockingStub(channel);

        //Server Streaming
        GreetManyTimesRequest greetManyTimesRequest = GreetManyTimesRequest.newBuilder()
                .setGreeting(Greeting.newBuilder()
                        .setFirstName("Siyi")
                        .build())
                .build();

        greetClient.greetManyTimes(greetManyTimesRequest)
                .forEachRemaining(greetManyTimesResponse -> {
                    System.out.println(greetManyTimesResponse.getResult());
                });
    }

    private static void doUnaryCall(ManagedChannel channel) {
        //create a stub      //create a greet service client (blocking - synchronous)
        GreetServiceGrpc.GreetServiceBlockingStub greetClient = GreetServiceGrpc.newBlockingStub(channel);
        //create a protocol buffer message
        Greeting greeting = Greeting.newBuilder()
                .setFirstName("Siyi")
                .setLastName("Cai")
                .build();

        //do the same for the GreetRequst
        GreetRequest greetRequest = GreetRequest.newBuilder()
                .setGreeting(greeting)
                .build();

        //call rpc and get a GreetResponse
        GreetResponse response = greetClient.greet(greetRequest);

        System.out.println(response.getResult());
    }

    private static void doClientStreamingCall(ManagedChannel channel)  {
        //create an asynchronous client
        GreetServiceGrpc.GreetServiceStub asyncClient = GreetServiceGrpc.newStub(channel);

        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<LongGreetRequest> requestObserver =  asyncClient.longGreet(new StreamObserver<LongGreetResponse>() {
            @Override
            public void onNext(LongGreetResponse value) {
                //we get a response from a server
                System.out.println("Received a response from server");
                System.out.println(value.getResult());
                //onNext will only be called once
            }

            @Override
            public void onError(Throwable t) {
                //we get an error from the server

            }

            @Override
            public void onCompleted() {
                //the server is done sending us data
                System.out.println("Server completes sending us something");
                //onComplete will be called right after onNext()
                latch.countDown();
            }
        });

        //Stream message #1
        System.out.println("Sending message 1");
        requestObserver.onNext(LongGreetRequest.newBuilder()
                .setGreeting(Greeting.newBuilder()
                        .setFirstName("Siyi")
                        .setLastName("Cai")
                        .build())
                .build());

        //Stream message #2
        System.out.println("Sending message 2");
        requestObserver.onNext(LongGreetRequest.newBuilder()
                .setGreeting(Greeting.newBuilder()
                        .setFirstName("Woha")
                        .build())
                .build());

        //Stream message #3
        System.out.println("Sending message 3");
        requestObserver.onNext(LongGreetRequest.newBuilder()
                .setGreeting(Greeting.newBuilder()
                        .setFirstName("Amber")
                        .build())
                .build());

        //We are telling the server that the server is done sending data
        requestObserver.onCompleted();

        try {
            latch.await(3L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void doBiDiStreamingCall(ManagedChannel channel){
        //create an asynchronous client
        GreetServiceGrpc.GreetServiceStub asyncClient = GreetServiceGrpc.newStub(channel);

        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<GreetEveryoneRequest> requestObserver = asyncClient.greetEveryone(new StreamObserver<GreetEveryoneResponse>() {
            @Override
            public void onNext(GreetEveryoneResponse value) {
                System.out.println("Response from server: "+value.getResult());
            }

            @Override
            public void onError(Throwable t) {
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                System.out.println("Server is done sending the data");
                latch.countDown();
            }
        });

        Arrays.asList("foo", "bar", "sth").forEach(

                name -> {
                    System.out.println("Sending: " + name);
                    requestObserver.onNext(GreetEveryoneRequest.newBuilder()
                        .setGreeting(Greeting.newBuilder()
                                .setFirstName(name).build())
                        .build());


                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
        );

        requestObserver.onCompleted();

        try {
            latch.await(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void doUnaryCallWithDeadline (ManagedChannel channel) {
        GreetServiceGrpc.GreetServiceBlockingStub blockingStub = GreetServiceGrpc.newBlockingStub(channel);

        // first call 500 ms deadline
        try {
            System.out.println("Sening a request with a deadline of 3000 ms");
            GreetWithDeadlineResponse response = blockingStub.withDeadline(Deadline.after(3000, TimeUnit.MILLISECONDS)).greetWithDeadline(GreetWithDeadlineRequest
                    .newBuilder()
                    .setGreeting(Greeting.newBuilder().setFirstName("Foo").setLastName("Bar").build())
                    .build());
            System.out.println(response.getResult());
        } catch (StatusRuntimeException e) {
            if(e.getStatus() == Status.DEADLINE_EXCEEDED){
                System.out.println("Deadline has been exceeded, we don't want to respond");
            } else {
                e.printStackTrace();
            }
        }

        // first call 100 ms deadline
        try {
            System.out.println("Sening a request with a deadline of 100 ms");
            GreetWithDeadlineResponse response = blockingStub.withDeadline(Deadline.after(100, TimeUnit.MILLISECONDS)).greetWithDeadline(GreetWithDeadlineRequest
                    .newBuilder()
                    .setGreeting(Greeting.newBuilder().setFirstName("Foo").setLastName("Bar").build())
                    .build());
            System.out.println(response.getResult());
        } catch (StatusRuntimeException e) {
            if(e.getStatus() == Status.DEADLINE_EXCEEDED){
                System.out.println("Deadline has been exceeded, we don't want to respond");
            } else {
                e.printStackTrace();
            }
        }
    }



}

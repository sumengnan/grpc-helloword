package com.example.demo;

import com.google.common.util.concurrent.ListenableFuture;
import demo.HelloWordServiceGrpc;
import demo.HellowordService;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.Iterator;
import java.util.concurrent.ExecutionException;

public class Client {
    private static final ManagedChannel channel;
    private static final HellowordService.HelloRequest helloRequest ;

    static {
        //构建一条信道用与发送数据
        channel = ManagedChannelBuilder.forAddress("localhost", 8083).usePlaintext().build();
        //构建请求体
        helloRequest = HellowordService.HelloRequest.newBuilder().setMessage("你好服务器！").build();
    }
    public static void main(String[] args) throws Exception {
        //1、普通rpc调用，不支持流的形式
        simpleSayHello();
        System.out.println("===================");
        //2、客户端流式调用
        clientStreamSayHello();
        System.out.println("===================");
        //3、服务端流式调用
        serverStreamSayHello();
        System.out.println("===================");
        //4、双向流式调用
        streamSayHello();
        System.out.println("===================");
    }

    /**
     * 普通rpc调用
     */
    private static void simpleSayHello() throws InterruptedException, ExecutionException {
        System.out.println("simpleSayHello普通grpc调用");
        HelloWordServiceGrpc.HelloWordServiceBlockingStub blockingStub = HelloWordServiceGrpc.newBlockingStub(channel);
        //远程调用，并返回结果
        HellowordService.HelloResponse response = blockingStub.simpleSayHello(helloRequest);
        System.out.println("simpleSayHello收到服务器反馈：" + response);
    }
    /**
     * 客户端流式调用
     */
    private static void clientStreamSayHello(){
        System.out.println("clientStreamSayHello客户端流式调用开始");
        //异步存根
        HelloWordServiceGrpc.HelloWordServiceStub stub = HelloWordServiceGrpc.newStub(channel);
        //远程调用，并返回结果
        StreamObserver<HellowordService.HelloResponse> helloRequestObserver = new StreamObserver<HellowordService.HelloResponse>() {
            public void onNext(HellowordService.HelloResponse helloResponse) {
                System.out.println("客户端流式拿到服务端返回值");
                System.out.println(helloResponse);
            }

            public void onError(Throwable throwable) {
                System.out.println(throwable);
            }

            public void onCompleted() {
                System.out.println("客户端流式服务器完成");
            }
        };
        StreamObserver<HellowordService.HelloRequest> streamObserver = stub.clientStreamSayHello(helloRequestObserver);
        System.out.println("发送");
        streamObserver.onNext(helloRequest);
        System.out.println("发送");
        streamObserver.onNext(helloRequest);
        System.out.println("客户端关闭");
        streamObserver.onCompleted();
        System.out.println("clientStreamSayHello客户端流式调用结束");
    }
    /**
     * 服务端流式调用
     */
    private static void serverStreamSayHello(){
        System.out.println("serverStreamSayHello服务端流式调用开始");
        HelloWordServiceGrpc.HelloWordServiceBlockingStub blockingStub = HelloWordServiceGrpc.newBlockingStub(channel);
        Iterator<HellowordService.HelloResponse> responseIterator = blockingStub.serverStreamSayHello(helloRequest);
        System.out.println("收到服务端消息如下：");
        while (responseIterator.hasNext()){
            HellowordService.HelloResponse response = responseIterator.next();
            System.out.println(response);
        }
        System.out.println("serverStreamSayHello服务端流式调用结束");
    }

    /**
     * 双向流式调用(和客户端流式调用类似)
     */
    private static void streamSayHello(){
        System.out.println("streamSayHello双向流式调用开始");
        //异步存根
        HelloWordServiceGrpc.HelloWordServiceStub stub = HelloWordServiceGrpc.newStub(channel);
        //远程调用，并返回结果
        StreamObserver<HellowordService.HelloResponse> helloRequestObserver = new StreamObserver<HellowordService.HelloResponse>() {
            public void onNext(HellowordService.HelloResponse helloResponse) {
                System.out.println("双向流式拿到服务端返回值");
                System.out.println(helloResponse);
            }
            public void onError(Throwable throwable) {
                System.out.println(throwable);
            }
            public void onCompleted() {
                System.out.println("双向流式服务器完成");
            }
        };
        StreamObserver<HellowordService.HelloRequest> streamObserver = stub.streamSayHello(helloRequestObserver);
        System.out.println("发送");
        streamObserver.onNext(helloRequest);
        System.out.println("发送");
        streamObserver.onNext(helloRequest);
        System.out.println("发送");
        streamObserver.onNext(helloRequest);
        System.out.println("客户端关闭");
        streamObserver.onCompleted();

        System.out.println("streamSayHello双向流式调用结束");
    }
}

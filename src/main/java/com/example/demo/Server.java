package com.example.demo;

import demo.HelloWordServiceGrpc;
import demo.HellowordService;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.util.ArrayList;
import java.util.List;

public class Server {

    public static void main(String[] args) throws Exception {
        //构建一个rpc的server，监听8083端口，配置处理类
        io.grpc.Server server = ServerBuilder.forPort(8083).addService(new HelloWordServiceImpl()).build();
        //启动服务
        server.start();
        System.out.println("服务端启动");
        server.awaitTermination();
    }
}

/**
 * 实现grpc定义的接口，处理业务
 */
class HelloWordServiceImpl extends HelloWordServiceGrpc.HelloWordServiceImplBase {
    /**
     * 简单rpc调用
     */
    @Override
    public void simpleSayHello(HellowordService.HelloRequest request, StreamObserver<HellowordService.HelloResponse> responseObserver) {
        System.out.println("简单rpc调用，收到客户端：");
        System.out.println(request);
        //响应客户端
        HellowordService.HelloResponse response = HellowordService.HelloResponse.newBuilder().setIsOk(true).build();
        //注意，因为是非流式。所以只能返回一次结果（调用onNext），
        responseObserver.onNext(response);
        //响应完成操作
        responseObserver.onCompleted();
    }
    /**
     * 客户端流式 RPC
     */
    @Override
    public StreamObserver<HellowordService.HelloRequest> clientStreamSayHello(final StreamObserver<HellowordService.HelloResponse> responseObserver) {
        //使用list存储收到的客户端信息，或者直接边接收边处理
        final List<HellowordService.HelloRequest> list = new ArrayList();

        return new StreamObserver<HellowordService.HelloRequest>() {
            public void onNext(HellowordService.HelloRequest helloRequest) {
                System.out.println("客户端流式拿到客户端返回值");
                System.out.println(helloRequest);
                list.add(helloRequest);
            }
            public void onError(Throwable throwable) {

            }
            public void onCompleted() {
                System.out.println("客户端流式客户端完成,共收到消息："+list.size());
                //响应客户端
                HellowordService.HelloResponse response = HellowordService.HelloResponse.newBuilder().setIsOk(true).setMessage("收到你（客户端）的消息").build();
                responseObserver.onNext(response);
                //注意，因为是客户端流式，不是服务器端流式。所以只能返回一次结果（调用onNext），
                // 否则报错Cancelling the stream with status Status{code=INTERNAL, description=Too many
                responseObserver.onCompleted();
            }
        };
    }
    /**
     * 服务端流式 RPC
     */
    @Override
    public void serverStreamSayHello(HellowordService.HelloRequest request, StreamObserver<HellowordService.HelloResponse> responseObserver) {
        System.out.println("服务端流式rpc调用，收到客户端：");
        System.out.println(request);
        //响应客户端
        HellowordService.HelloResponse response = HellowordService.HelloResponse.newBuilder().setIsOk(true).build();
        System.out.println("服务端流式发送给客户端");
        responseObserver.onNext(response);
        System.out.println("服务端流式发送给客户端");
        responseObserver.onNext(response);
        System.out.println("服务端流式发送给客户端");
        responseObserver.onNext(response);
        //响应完成操作
        responseObserver.onCompleted();
    }
    /**
     * 双向流式 RPC
     */
    @Override
    public StreamObserver<HellowordService.HelloRequest> streamSayHello(final StreamObserver<HellowordService.HelloResponse> responseObserver) {
       return new StreamObserver<HellowordService.HelloRequest>() {
            public void onNext(HellowordService.HelloRequest helloRequest) {
                System.out.println("双向流式拿到客户端返回值");
                System.out.println(helloRequest);
                //响应客户端
                HellowordService.HelloResponse response = HellowordService.HelloResponse.newBuilder().setIsOk(true).setMessage("收到你（客户端）的消息").build();
                responseObserver.onNext(response);
            }
            public void onError(Throwable throwable) {

            }
            public void onCompleted() {
                System.out.println("双向流式客户端完成");
            }
        };
    }
}

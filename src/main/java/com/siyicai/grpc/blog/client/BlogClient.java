package com.siyicai.grpc.blog.client;

import com.proto.blog.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class BlogClient {
    public void run() {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext() //to avoid ssl, not recommended for production
                .build();
        BlogServiceGrpc.BlogServiceBlockingStub blogClient = BlogServiceGrpc.newBlockingStub(channel);
        Blog blog = Blog.newBuilder()
                .setAuthorId("Siyi")
                .setTitle("New Blog")
                .setContent("hello world, this is my new blog.")
                .build();
        CreateBlogResponse createResponse = blogClient.createBlog(
                CreateBlogRequest.newBuilder().setBlog(blog).build()
        );

        System.out.println("Received create blog response");
        System.out.println(createResponse.toString());

        System.out.println("Reading blog");
        String blogId = createResponse.getBlog().getId();
        ReadBlogResponse readBlogResponse = blogClient.readBlog(ReadBlogRequest.newBuilder()
                .setBlogId(blogId)
                .build());

        System.out.println(readBlogResponse.toString());

        //trigger a not found error
//        System.out.println("Reading blog with non existing id...");
//        ReadBlogResponse readBlogResponseNotFound = blogClient.readBlog(ReadBlogRequest.newBuilder()
//                .setBlogId("5d1e22526df80f52072ea449")
//                .build());

        Blog newBlog = Blog.newBuilder()
                .setId(blogId)
                .setAuthorId("Changed Author")
                .setTitle("New Blog (Updated)")
                .setContent("hello world, this is my new blog. I've added more conent")
                .build();


        System.out.println("Updating blog...");
        UpdateBlogResponse updateBlogResponse = blogClient.updateBlog(UpdateBlogRequest.newBuilder()
                .setBlog(newBlog).build());

        System.out.println("Updated blog");
        System.out.println(updateBlogResponse.toString());

        System.out.println("Deleting blog...");
        DeleteBlogResponse deleteBlogResponse = blogClient.deleteBlog(DeleteBlogRequest.newBuilder().setBlogId(blogId).build());

        System.out.println("Deleted Blog");

//        System.out.println("Reading blog");
//        ReadBlogResponse readBlogResponseAfterDeletion = blogClient.readBlog(ReadBlogRequest.newBuilder()
//                .setBlogId(blogId)
//                .build());

        System.out.println("Listing blog");
        blogClient.listBlog(ListBlogRequest.newBuilder().build()).forEachRemaining(
                listBlogResponse -> System.out.println(listBlogResponse.getBlog().toString())
        );
        System.out.println("Shutting down channel");
        channel.shutdown();
    }

    public static void main(String[] args) {
        System.out.println("Hello, I am a gRPC client");

        System.out.println("Creating a stub");

        BlogClient main = new BlogClient();

        main.run();

    }
}

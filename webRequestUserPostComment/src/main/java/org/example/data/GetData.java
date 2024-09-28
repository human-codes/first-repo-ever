package org.example.data;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.SneakyThrows;
import org.example.entity.Comment;
import org.example.entity.Post;
import org.example.entity.User;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class GetData {
    @SneakyThrows
    public static List<User> getDataUser(){
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://jsonplaceholder.typicode.com/users"))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        Gson gson = new Gson();
        Type userListType = new TypeToken<List<User>>(){}.getType();
        List<User> users = gson.fromJson(response.body(), userListType);
        return users;
    }
    @SneakyThrows
    public static List<Post> getDataPost(){
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://jsonplaceholder.typicode.com/posts"))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        Gson gson = new Gson();
        Type userListType = new TypeToken<List<Post>>(){}.getType();
        List<Post> posts = gson.fromJson(response.body(), userListType);
        return posts;
    }
    @SneakyThrows
    public static List<Comment> getDataComment(){
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://jsonplaceholder.typicode.com/comments"))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        Gson gson = new Gson();
        Type userListType = new TypeToken<List<Comment>>(){}.getType();
        List<Comment> comments = gson.fromJson(response.body(), userListType);
        return comments;
    }
}

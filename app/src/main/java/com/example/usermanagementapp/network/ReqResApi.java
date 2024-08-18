package com.example.usermanagementapp.network;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.DELETE;
import retrofit2.http.Path;
import retrofit2.http.PUT;

import retrofit2.http.Query;
import com.example.usermanagementapp.data.User;

public interface ReqResApi {
    @GET("api/users")
    Call<UsersResponse> getUsers(@Query("page") int page);

    @POST("api/users/new")
    Call<UserResponse> createUser(@Body UserRequest userRequest);

    @PUT("api/users/{id}")
    Call<UserResponse> updateUser(@Path("id") int userId, @Body UserRequest userRequest);

    @DELETE("api/users/{id}")
    Call<Void> deleteUser(@Path("id") int userId);


}
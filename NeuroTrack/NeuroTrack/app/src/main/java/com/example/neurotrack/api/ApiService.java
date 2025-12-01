package com.example.neurotrack.api;

import com.example.neurotrack.models.ChildSummary;
import com.example.neurotrack.models.CreateUserRequest;
import com.example.neurotrack.models.LoginRequest;
import com.example.neurotrack.models.LoginResponse;
import com.example.neurotrack.models.TaskInstance;
import com.example.neurotrack.models.TaskTemplate;
import com.example.neurotrack.models.Theme;
import com.example.neurotrack.models.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiService {


    @POST("api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

    @POST("users/guardian")
    Call<User> register(@Body CreateUserRequest request);

    @GET("users/guardian/{guardianId}/children")
    Call<List<ChildSummary>> getGuardianChildren(@Path("guardianId") Long guardianId);

    @GET("api/themes")
    Call<List<Theme>> getThemes();

    @GET("api/themes/{id}")
    Call<Theme> getTheme(@Path("id") Long id);


    @GET("api/task-templates")
    Call<List<TaskTemplate>> getTaskTemplates();

    @GET("api/task-templates/{id}")
    Call<TaskTemplate> getTaskTemplate(@Path("id") Long id);


    @GET("api/task-instances/child/{childId}")
    Call<List<TaskInstance>> getChildTasks(
            @Path("childId") Long childId,
            @Header("Authorization") String token
    );

    @POST("api/task-instances")
    Call<TaskInstance> createTaskInstance(
            @Body TaskInstance taskInstance,
            @Header("Authorization") String token
    );

    @PUT("api/task-instances/{id}/complete")
    Call<TaskInstance> completeTask(
            @Path("id") Long id,
            @Header("Authorization") String token
    );


    @GET("api/users/me")
    Call<User> getCurrentUser(@Header("Authorization") String token);
}


package com.example.neurotrack.api;

import com.example.neurotrack.models.ChildSummary;
import com.example.neurotrack.models.CreateChildRequest;
import com.example.neurotrack.models.CreateTaskInstanceRequest;
import com.example.neurotrack.models.CreateUserRequest;
import com.example.neurotrack.models.LoginRequest;
import com.example.neurotrack.models.LoginResponse;
import com.example.neurotrack.models.RewardCatalog;
import com.example.neurotrack.models.TaskInstance;
import com.example.neurotrack.models.TaskTemplate;
import com.example.neurotrack.models.Theme;
import com.example.neurotrack.models.UpdateChildRequest;
import com.example.neurotrack.models.User;
import com.example.neurotrack.models.AssignRewardRequest;
import com.example.neurotrack.models.ChildReward;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
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

    @GET("users/{userId}")
    Call<User> getUserById(@Path("userId") Long userId);

    @POST("users/guardian/{guardianId}/child")
    Call<User> createChild(
            @Path("guardianId") Long guardianId,
            @Body CreateChildRequest request
    );

    @GET("api/themes")
    Call<List<Theme>> getThemes();

    @GET("api/themes/{id}")
    Call<Theme> getTheme(@Path("id") Long id);


    @GET("api/task-templates")
    Call<List<TaskTemplate>> getTaskTemplates();

    @GET("api/task-templates/{id}")
    Call<TaskTemplate> getTaskTemplate(@Path("id") Long id);


    @GET("api/task-instances/today/{childUserId}")
    Call<List<TaskInstance>> getChildTasks(
            @Path("childUserId") Long childUserId,
            @Header("Authorization") String token
    );

    @GET("api/task-instances/child/{childUserId}")
    Call<List<TaskInstance>> getAllChildTasks(
            @Path("childUserId") Long childUserId,
            @Header("Authorization") String token
    );

    @POST("api/task-instances")
    Call<TaskInstance> createTaskInstance(
            @Body CreateTaskInstanceRequest request,
            @Header("Authorization") String token
    );

    @POST("api/task-instances/{id}/complete")
    Call<TaskInstance> completeTask(
            @Path("id") Long id,
            @Header("Authorization") String token
    );

    @DELETE("api/task-instances/{taskInstanceId}")
    Call<Void> deleteTaskInstance(
            @Path("taskInstanceId") Long taskInstanceId,
            @Header("Authorization") String token
    );

    @PUT("api/task-instances/{taskInstanceId}")
    Call<TaskInstance> updateTaskInstance(
            @Path("taskInstanceId") Long taskInstanceId,
            @Body CreateTaskInstanceRequest request,
            @Header("Authorization") String token
    );


    @GET("api/users/me")
    Call<User> getCurrentUser(@Header("Authorization") String token);

    @DELETE("users/{userId}")
    Call<Void> deleteUser(@Path("userId") Long userId);

    @PUT("users/{userId}")
    Call<User> updateUser(
            @Path("userId") Long userId,
            @Body UpdateChildRequest request
    );


    @GET("api/catalog/rewards/guardian/{guardianUserId}")
    Call<List<RewardCatalog>> getGuardianRewards(
            @Path("guardianUserId") Long guardianUserId
    );

    @POST("api/catalog/rewards")
    Call<RewardCatalog> createReward(
            @Body RewardCatalog reward
    );

    @PUT("api/catalog/rewards/{rewardId}")
    Call<RewardCatalog> updateReward(
            @Path("rewardId") Long rewardId,
            @Body RewardCatalog reward
    );

    @DELETE("api/catalog/rewards/{rewardId}")
    Call<Void> deleteReward(
            @Path("rewardId") Long rewardId
    );


    @GET("api/points/child/{childId}/total")
    Call<Integer> getChildPoints(
            @Path("childId") Long childId
    );


    @POST("api/catalog/rewards/redeem")
    Call<ChildReward> redeemReward(
            @Body AssignRewardRequest request
    );

    @GET("api/rewards/child/{childUserId}")
    Call<List<ChildReward>> getChildRewards(
            @Path("childUserId") Long childUserId
    );
}


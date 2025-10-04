package com.example.todo.network;

import com.example.todo.model.List;
import com.example.todo.model.Tag;
import com.example.todo.model.Task;
import com.example.todo.model.User;
import com.example.todo.model.dto.*;
import com.example.todo.model.response.ApiResponse;
import com.example.todo.model.vo.UserLoginVO;
import com.example.todo.model.vo.UserRefreshTokenVO;

import retrofit2.Call;
import retrofit2.http.*;

/**
 * API服务接口
 * 定义所有网络请求方法
 */
public interface ApiService {

    // ==================== 用户相关 ====================

    /**
     * 用户登录接口
     * @param userLoginDTO 登录请求数据
     * @return 登录响应结果
     */
    @POST("/users/login")
    Call<ApiResponse<UserLoginVO>> login(@Body UserLoginDTO userLoginDTO);

    /**
     * 用户注册接口
     * @param userRegisterDTO 注册请求数据
     * @return 注册响应结果
     */
    @POST("/users/register")
    Call<ApiResponse<Void>> register(@Body UserRegisterDTO userRegisterDTO);

    /**
     * 用户刷新令牌接口
     * @param refreshTokenDTO 刷新令牌请求数据
     * @return 刷新令牌响应结果
     */
    @POST("/users/refresh-token")
    Call<ApiResponse<UserRefreshTokenVO>> refreshToken(@Body UserRefreshTokenDTO refreshTokenDTO);

    /**
     * 获取当前用户信息
     * @return 用户信息
     */
    @GET("/users/profile")
    Call<ApiResponse<User>> getCurrentUser();

    /**
     * 更新用户信息
     * @param userProfileUpdateDTO 更新用户信息DTO
     * @return 更新结果
     */
    @PUT("/users/profile")
    Call<ApiResponse<Void>> updateUser(@Body UserProfileUpdateDTO userProfileUpdateDTO);

    /**
     * 修改密码
     * @param userChangePasswordDTO 修改密码DTO
     * @return 修改结果
     */
    @PUT("/users/profile/password")
    Call<ApiResponse<Void>> changePassword(@Body UserChangePasswordDTO userChangePasswordDTO);

    /**
     * 用户退出登录
     * @return 退出结果
     */
    @POST("/users/logout")
    Call<ApiResponse<Void>> logout();

    /**
     * 校验用户名是否可用
     * @param username 用户名
     * @return 是否可用
     */
    @GET("/users/check-username")
    Call<ApiResponse<Boolean>> checkUsername(@Query("username") String username);

    /**
     * 校验邮箱是否可用
     * @param email 邮箱
     * @return 是否可用
     */
    @GET("/users/check-email")
    Call<ApiResponse<Boolean>> checkEmail(@Query("email") String email);

    // ==================== 分类相关 ====================

    /**
     * 创建分类
     * @param createListDTO 创建分类DTO
     * @return 创建结果
     */
    @POST("/lists")
    Call<ApiResponse<List>> createList(@Body CreateListDTO createListDTO);

    /**
     * 获取分类列表
     * @return 分类列表
     */
    @GET("/lists")
    Call<ApiResponse<java.util.List<List>>> getLists();

    /**
     * 获取分类详情
     * @param id 分类ID
     * @return 分类详情
     */
    @GET("/lists/{id}")
    Call<ApiResponse<List>> getList(@Path("id") int id);

    /**
     * 更新分类
     * @param id 分类ID
     * @param updateListDTO 更新分类DTO
     * @return 更新结果
     */
    @PUT("/lists/{id}")
    Call<ApiResponse<Void>> updateList(@Path("id") int id, @Body UpdateListDTO updateListDTO);

    /**
     * 删除分类
     * @param id 分类ID
     * @return 删除结果
     */
    @DELETE("/lists/{id}")
    Call<ApiResponse<Void>> deleteList(@Path("id") int id);

    // ==================== 标签相关 ====================

    /**
     * 创建标签
     * @param createTagDTO 创建标签DTO
     * @return 创建结果
     */
    @POST("/tags")
    Call<ApiResponse<Tag>> createTag(@Body CreateTagDTO createTagDTO);

    /**
     * 获取标签列表
     * @return 标签列表
     */
    @GET("/tags")
    Call<ApiResponse<java.util.List<Tag>>> getTags();

    /**
     * 获取标签详情
     * @param id 标签ID
     * @return 标签详情
     */
    @GET("/tags/{id}")
    Call<ApiResponse<Tag>> getTag(@Path("id") int id);

    /**
     * 更新标签
     * @param id 标签ID
     * @param updateTagDTO 更新标签DTO
     * @return 更新结果
     */
    @PUT("/tags/{id}")
    Call<ApiResponse<Void>> updateTag(@Path("id") int id, @Body UpdateTagDTO updateTagDTO);

    /**
     * 删除标签
     * @param id 标签ID
     * @return 删除结果
     */
    @DELETE("/tags/{id}")
    Call<ApiResponse<Void>> deleteTag(@Path("id") int id);

    // ==================== 任务相关 ====================

    /**
     * 创建任务
     * @param createTaskDTO 创建任务DTO
     * @return 创建结果
     */
    @POST("/tasks")
    Call<ApiResponse<Task>> createTask(@Body CreateTaskDTO createTaskDTO);

    /**
     * 获取任务列表
     * @param status 任务状态
     * @param priority 优先级
     * @param listId 分类ID
     * @param tagId 标签ID
     * @param keyword 关键词
     * @return 任务列表
     */
    @GET("/tasks")
    Call<ApiResponse<java.util.List<Task>>> getTasks(
            @Query("status") String status,
            @Query("priority") String priority,
            @Query("listId") Integer listId,
            @Query("tagId") Integer tagId,
            @Query("keyword") String keyword
    );

    /**
     * 获取任务详情
     * @param id 任务ID
     * @return 任务详情
     */
    @GET("/tasks/{id}")
    Call<ApiResponse<Task>> getTask(@Path("id") int id);

    /**
     * 更新任务
     * @param id 任务ID
     * @param updateTaskDTO 更新任务DTO
     * @return 更新结果
     */
    @PUT("/tasks/{id}")
    Call<ApiResponse<Void>> updateTask(@Path("id") int id, @Body UpdateTaskDTO updateTaskDTO);

    /**
     * 更新任务状态
     * @param id 任务ID
     * @param updateTaskStatusDTO 更新任务状态DTO
     * @return 更新结果
     */
    @PATCH("/tasks/{id}")
    Call<ApiResponse<Void>> updateTaskStatus(@Path("id") int id, @Body UpdateTaskStatusDTO updateTaskStatusDTO);

    /**
     * 删除任务
     * @param id 任务ID
     * @return 删除结果
     */
    @DELETE("/tasks/{id}")
    Call<ApiResponse<Void>> deleteTask(@Path("id") int id);

    // ==================== 文件上传 ====================

    /**
     * 上传文件
     * @param file 文件
     * @return 上传结果
     */
//    @Multipart
//    @POST("/files/upload")
//    Call<ApiResponse<FileUploadResponse>> uploadFile(@Part MultipartBody.Part file);
}
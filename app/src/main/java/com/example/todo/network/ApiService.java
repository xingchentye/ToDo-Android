package com.example.todo.network;

import com.example.todo.model.dto.UserLoginDTO;
import com.example.todo.model.dto.UserRegisterDTO;
import com.example.todo.model.response.ApiResponse;
import com.example.todo.model.vo.UserLoginVO;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * API服务接口
 * 定义所有网络请求方法
 */
public interface ApiService {

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
}
package com.example.todo.network;

import com.example.todo.model.dto.UserLoginDTO;
import com.example.todo.model.response.ApiResponse;
import com.example.todo.model.vo.UserLoginVO;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

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
}
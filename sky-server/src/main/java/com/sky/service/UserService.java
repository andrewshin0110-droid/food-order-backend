package com.sky.service;

import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;

public interface UserService {

    /**
     * 用戶登入
     * @param userLoginDTO
     * @return
     */
    User login(UserLoginDTO userLoginDTO);
}

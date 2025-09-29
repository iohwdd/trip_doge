package com.tripdog.service;

import com.tripdog.model.dto.UserLoginDTO;
import com.tripdog.model.dto.UserRegisterDTO;
import com.tripdog.model.entity.UserDO;
import com.tripdog.model.vo.UserInfoVO;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 根据邮箱查询用户
     */
    UserDO selectByEmail(String email);

    /**
     * 检查邮箱是否已存在
     */
    boolean existsByEmail(String email);

    /**
     * 创建用户
     */
    boolean createUser(UserDO user);

    /**
     * 根据ID查询用户
     */
    UserDO selectById(Long id);

    /**
     * 更新用户信息
     */
    boolean updateUser(UserDO user);

    /**
     * 用户注册（包含验证码校验）
     */
    UserInfoVO register(UserRegisterDTO registerDTO);

    /**
     * 用户登录
     */
    UserInfoVO login(UserLoginDTO loginDTO);

}

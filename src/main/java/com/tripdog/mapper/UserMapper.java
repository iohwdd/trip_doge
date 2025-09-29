package com.tripdog.mapper;

import com.tripdog.model.entity.UserDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户表 Mapper 接口
 */
@Mapper
public interface UserMapper {

    /**
     * 插入用户
     */
    int insert(UserDO user);

    /**
     * 根据ID删除用户
     */
    int deleteById(Long id);

    /**
     * 更新用户
     */
    int updateById(UserDO user);

    /**
     * 根据ID查询用户
     */
    UserDO selectById(Long id);

    /**
     * 根据邮箱查询用户
     */
    UserDO selectByEmail(@Param("email") String email);

    /**
     * 检查邮箱是否存在
     */
    int countByEmail(@Param("email") String email);
}

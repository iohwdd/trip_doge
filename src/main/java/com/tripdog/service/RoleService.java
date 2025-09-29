package com.tripdog.service;

import java.util.List;

import com.tripdog.model.vo.RoleInfoVO;
import com.tripdog.model.vo.RoleDetailVO;

/**
 * 角色服务接口
 *
 * @author: iohw
 * @date: 2025/9/24 20:31
 */
public interface RoleService {

    /**
     * 获取活跃角色列表
     *
     * @return 角色信息列表
     */
    List<RoleInfoVO> getRoleInfoList();

    /**
     * 根据角色代码获取角色详情
     *
     * @param code 角色代码
     * @return 角色详情
     */
    RoleDetailVO getRoleDetailByCode(String code);

    /**
     * 根据角色ID获取角色详情
     *
     * @param roleId 角色ID
     * @return 角色详情
     */
    RoleDetailVO getRoleDetailById(Long roleId);

    /**
     * 获取角色的系统提示词
     *
     * @param roleId 角色ID
     * @return 系统提示词
     */
    String getSystemPrompt(Long roleId);
}

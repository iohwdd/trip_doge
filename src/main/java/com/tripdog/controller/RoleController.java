package com.tripdog.controller;

import com.tripdog.common.ErrorCode;
import com.tripdog.common.Result;
import com.tripdog.mapper.ConversationMapper;
import com.tripdog.model.entity.ConversationDO;
import com.tripdog.model.vo.RoleInfoVO;
import com.tripdog.model.vo.RoleDetailVO;
import com.tripdog.model.vo.UserInfoVO;
import com.tripdog.service.ConversationService;
import com.tripdog.service.RoleService;
import com.tripdog.service.impl.UserSessionService;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.http.Method;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色控制器
 */
@Tag(name = "角色管理", description = "AI角色列表和对话管理相关接口")
@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RoleController {
    @Value("${minio.bucket-name}")
    private String bucketName;
    private final MinioClient minioClient;
    private final RoleService roleService;
    private final ConversationService conversationService;
    private final ConversationMapper conversationMapper;
    private final UserSessionService userSessionService;


    /**
     * 获取用户与角色对话列表
     */
    @Operation(summary = "获取角色对话列表", description = "获取用户与所有AI角色的对话列表，包含角色信息和会话ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "成功获取角色列表"),
            @ApiResponse(responseCode = "10105", description = "用户未登录")
    })
    @PostMapping("/list")
    public Result<List<RoleInfoVO>> getActiveRoles() {
        // 从用户会话服务获取当前登录用户信息
        UserInfoVO userInfo = userSessionService.getCurrentUser();
        if(userInfo == null) {
            return Result.error(ErrorCode.USER_NOT_LOGIN);
        }
        // 检查所有角色是否已创建好对话
        List<RoleInfoVO> roleInfoList = roleService.getRoleInfoList();
        roleInfoList.forEach(roleInfoVO -> {
            ConversationDO conversation = conversationService.findConversationByUserAndRole(
                userInfo.getId(), roleInfoVO.getId());
            if(conversation == null) {
                conversation = conversationService.getOrCreateConversation(userInfo.getId(), roleInfoVO.getId());
            }
            roleInfoVO.setConversationId(conversation.getConversationId());
            // 转化头像url
            String url;
            try{
                url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET)
                        .bucket(bucketName)
                        .object(roleInfoVO.getAvatarUrl())
                        .expiry(60 * 60)
                        .build()
                );
                roleInfoVO.setAvatarUrl(url);
            }catch (Exception e){
                throw new RuntimeException(ErrorCode.NO_FOUND_FILE.getMessage());
            }
        });

        return Result.success(roleInfoList);
    }

    /**
     * 获取角色详情
     *
     * @param roleId 角色ID
     * @return 角色详情信息
     */
    @Operation(summary = "获取角色详情", description = "根据角色ID获取指定AI角色的详细信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功获取角色详情"),
        @ApiResponse(responseCode = "10202", description = "角色不存在")
    })
    @PostMapping("/{roleId}/detail")
    public Result<RoleDetailVO> getRoleDetail(@PathVariable Long roleId) {
        RoleDetailVO roleDetail = roleService.getRoleDetailById(roleId);
        if (roleDetail == null) {
            return Result.error(ErrorCode.NOT_FOUND_ERROR, "角色不存在");
        }
        return Result.success(roleDetail);
    }

}

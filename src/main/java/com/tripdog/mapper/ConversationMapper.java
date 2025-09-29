package com.tripdog.mapper;

import com.tripdog.model.entity.ConversationDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户与角色对话会话表 Mapper 接口
 */
@Mapper
public interface ConversationMapper {

    /**
     * 插入会话
     */
    int insert(ConversationDO conversation);

    /**
     * 根据ID删除会话
     */
    int deleteById(String id);

    /**
     * 更新会话
     */
    int updateById(ConversationDO conversation);

    /**
     * 根据ID查询会话
     */
    ConversationDO selectById(String id);

    /**
     * 根据conversationId查询会话
     */
    ConversationDO selectByConversationId(String conversationId);

    /**
     * 动态条件查询会话列表
     */
    List<ConversationDO> selectConversationList(ConversationDO conversation);
}

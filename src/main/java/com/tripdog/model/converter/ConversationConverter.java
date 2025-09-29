package com.tripdog.model.converter;

import com.tripdog.model.entity.ConversationDO;
import com.tripdog.model.entity.ChatHistoryDO;
import com.tripdog.model.vo.ConversationVO;
import com.tripdog.model.vo.ChatHistoryVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 会话和聊天历史映射器
 */
@Mapper
public interface ConversationConverter {

    ConversationConverter INSTANCE = Mappers.getMapper(ConversationConverter.class);

    /**
     * ConversationDO转ConversationVO
     */
    @Mapping(target = "roleName", ignore = true)
    @Mapping(target = "roleAvatarUrl", ignore = true)
    @Mapping(target = "createTime", source = "createdAt")
    @Mapping(target = "updateTime", source = "updatedAt")
    ConversationVO toConversationVO(ConversationDO conversationDO);

    /**
     * ConversationDO列表转ConversationVO列表
     */
    List<ConversationVO> toConversationVOList(List<ConversationDO> conversationDOList);

    /**
     * ChatHistoryDO转ChatHistoryVO
     */
    @Mapping(target = "roleId", ignore = true)
    @Mapping(target = "messageType", source = "role")
    @Mapping(target = "inputTokens", ignore = true)
    @Mapping(target = "outputTokens", ignore = true)
    @Mapping(target = "createTime", source = "createdAt")
    ChatHistoryVO toChatHistoryVO(ChatHistoryDO chatHistoryDO);

    /**
     * ChatHistoryDO列表转ChatHistoryVO列表
     */
    List<ChatHistoryVO> toChatHistoryVOList(List<ChatHistoryDO> chatHistoryDOList);
}

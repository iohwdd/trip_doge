package com.tripdog.model.converter;

import com.tripdog.model.entity.RoleDO;
import com.tripdog.model.vo.RoleInfoVO;
import com.tripdog.model.vo.RoleDetailVO;
import com.tripdog.common.utils.RoleConfigParser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.AfterMapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 角色映射器
 */
@Mapper
public interface RoleConverter {

    RoleConverter INSTANCE = Mappers.getMapper(RoleConverter.class);

    /**
     * RoleDO转RoleInfoVO
     */
    @Mapping(target = "conversationId", ignore = true)
    RoleInfoVO toRoleInfoVO(RoleDO roleDO);

    /**
     * RoleDO列表转RoleInfoVO列表
     */
    List<RoleInfoVO> toRoleInfoVOList(List<RoleDO> roleDOList);

    /**
     * RoleDO转RoleDetailVO（不包含解析后的配置）
     */
    @Mapping(target = "personality", ignore = true)
    @Mapping(target = "specialties", ignore = true)
    RoleDetailVO toRoleDetailVO(RoleDO roleDO);

    /**
     * 转换后处理，解析配置字段
     */
    @AfterMapping
    default void afterMapping(RoleDO roleDO, @MappingTarget RoleDetailVO detailVO) {
        // 解析AI设置
        String aiSetting = roleDO.getAiSetting();
        // if (aiSetting != null) {
        //     detailVO.setSystemPrompt(RoleConfigParser.extractSystemPrompt(aiSetting));
        //     detailVO.setTemperature(RoleConfigParser.extractTemperature(aiSetting));
        //     detailVO.setMaxTokens(RoleConfigParser.extractMaxTokens(aiSetting));
        //     detailVO.setTopP(RoleConfigParser.extractTopP(aiSetting));
        // }

        // 解析角色设定
        String roleSetting = roleDO.getRoleSetting();
        if (roleSetting != null) {
            detailVO.setPersonality(RoleConfigParser.extractPersonality(roleSetting));
            detailVO.setSpecialties(RoleConfigParser.extractSpecialties(roleSetting));
            // detailVO.setCommunicationStyle(RoleConfigParser.extractCommunicationStyle(roleSetting));
            // detailVO.setEmoji(RoleConfigParser.extractEmoji(roleSetting));
            // detailVO.setCatchphrases(RoleConfigParser.extractCatchphrases(roleSetting));
        }
    }
}

package com.tripdog.mapper;

import com.tripdog.model.entity.IntimacyFactorsDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 亲密度影响因素记录表 Mapper 接口
 */
@Mapper
public interface IntimacyFactorsMapper {

    /**
     * 插入亲密度因素记录
     */
    int insert(IntimacyFactorsDO intimacyFactors);

    /**
     * 根据ID删除亲密度因素记录
     */
    int deleteById(Long id);

    /**
     * 更新亲密度因素记录
     */
    int updateById(IntimacyFactorsDO intimacyFactors);

    /**
     * 根据ID查询亲密度因素记录
     */
    IntimacyFactorsDO selectById(Long id);

    /**
     * 动态条件查询亲密度因素记录
     */
    List<IntimacyFactorsDO> selectIntimacyFactorsList(IntimacyFactorsDO intimacyFactors);

    /**
     * 计算会话当前总亲密度
     */
    Integer calculateCurrentIntimacy(@Param("conversationId") Long conversationId);

    /**
     * 根据会话ID删除所有亲密度因素记录
     */
    int deleteByConversationId(@Param("conversationId") Long conversationId);
}

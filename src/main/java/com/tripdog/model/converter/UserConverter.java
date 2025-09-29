package com.tripdog.model.converter;

import com.tripdog.model.dto.UserRegisterDTO;
import com.tripdog.model.entity.UserDO;
import com.tripdog.model.vo.UserInfoVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * 用户对象转换器
 */
@Mapper
public interface UserConverter {

    UserConverter INSTANCE = Mappers.getMapper(UserConverter.class);

    /**
     * UserDO转UserInfoVO
     */
    UserInfoVO toUserInfoVO(UserDO userDO);

    /**
     * UserRegisterDTO转UserDO
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "avatarUrl", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    UserDO toUserDO(UserRegisterDTO userRegisterDTO);

}

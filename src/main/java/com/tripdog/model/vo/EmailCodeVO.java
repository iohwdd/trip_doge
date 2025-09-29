package com.tripdog.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 邮箱验证码返回VO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailCodeVO {

    /**
     * 验证码
     */
    private String code;

}

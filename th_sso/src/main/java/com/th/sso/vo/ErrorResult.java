package com.th.sso.vo;

import lombok.Builder;
import lombok.Data;

/*
 * @author Lise
 * @date 2021年05月20日 21:35
 * @program: tanhua
 * @description:
 */
@Data
@Builder
public class ErrorResult {

    // 错误状态码和错误信息
    private String errCode;
    private String errMessage;


}

package com.th.common.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * @author Lise
 * @date 2021年05月20日 20:03
 * @program: tanhua
 * @description:
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class User extends BasePojo{

    private Long id;

    private String mobile;

    private String password;

}

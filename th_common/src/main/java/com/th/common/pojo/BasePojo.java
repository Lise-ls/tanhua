package com.th.common.pojo;


import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.util.Date;

/*
 * @author Lise
 * @date 2021年05月20日 19:54
 * @program: tanhua
 * @description:
 */

@Data
public abstract class BasePojo {

    // Mybatis 自动填充时间
    @TableField(fill = FieldFill.INSERT)
    // 创建时间
    private Date created;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    // 更新时间字段
    private Date updated;
}

package com.th.sso.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.util.Date;

/*
 * @author Lise
 * @date 2021年05月20日 20:18
 * @program: tanhua
 * @description:  对自动填充字段的处理
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {


    @Override
    public void insertFill(MetaObject metaObject) {
        // 得到新增字段
        Object created = getFieldValByName("created", metaObject);
        //判断,如果新增时，创建时间为空，则赋值。
        if (created==null){
            setFieldValByName("created",new Date(),metaObject);
        }

        // 得到更新字段
        Object updated = getFieldValByName("updated",metaObject);
        if (updated==null){
            setFieldValByName("updated",new Date(), metaObject);
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        setFieldValByName("updated", new Date(),metaObject);
    }
}

package com.th.dubbo.serverIsImpl.pojo;

import lombok.Data;
import org.bson.types.ObjectId;

import java.io.Serializable;

/*
 * @author Lise
 * @date 2021年05月26日 16:41
 * @program: th
 * @description:
 */
@Data
public class Users implements Serializable {

    private static final long serialVersionUID = 6003135946820874230L;

    private ObjectId id;
    private Long userId; //用户id
    private Long friendId; //好友id
    private Long date; //时间

}

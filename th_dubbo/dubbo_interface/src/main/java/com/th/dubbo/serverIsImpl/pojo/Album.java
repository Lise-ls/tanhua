package com.th.dubbo.serverIsImpl.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

/*
 * @author Lise
 * @date 2021年05月26日 18:13
 * @program: th
 * @description:  相册表，用于存储自己发布的数据，每一个用户一张表进行存储
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "quanzi_album_{userId}")
public class Album implements Serializable {

    private static final long serialVersionUID = 432183095092216817L;

    @Id
    private ObjectId id; //主键id

    private ObjectId publishId; //发布id
    private Long created; //发布时间
}

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
 * @date 2021年05月26日 18:14
 * @program: th
 * @description:  时间线表，用于存储发布的数据，每一个用户一张表进行存储
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "quanzi_time_line_{userId}")
public class TimeLine implements Serializable {

    private static final long serialVersionUID = 9096178416317502524L;

    @Id
    private ObjectId id;
    private Long userId; // 好友id
    private ObjectId publishId; //发布id
    private Long date; //发布的时间1

}

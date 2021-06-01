package com.th.dubbo.serverIsImpl.service;

import cn.hutool.core.collection.CollUtil;
import com.th.dubbo.serverIsImpl.pojo.TimeLine;
import com.th.dubbo.serverIsImpl.pojo.Users;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/*
 * @author Lise
 * @date 2021年05月27日 19:18
 * @program: tanhua
 * @description:
 */
@Service
@Slf4j
public class TimeLineService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Async //异步执行，原理：底层开一个线程去执行该方法
    public CompletableFuture<String> saveTimeLine(Long userId, ObjectId publishId) {
        // 写入好友时间线表


        try {
            // 查询好友列表
            // 通过传入的 用户id 查询 tanhua_users表  返回对应的friendid
            Query query = Query.query(Criteria.where("userId").is(userId));
            List<Users> usersList = this.mongoTemplate.find(query, Users.class);
            //对返回的用户对应的好友id集合，进行是否为空判断。
            if (CollUtil.isEmpty(usersList)){
                // 不为空  说明有好友 返回成功
                return CompletableFuture.completedFuture("ok");
            }

            // 依次写入好友的时间线表中
            for (Users users : usersList) {
                TimeLine timeLine=new TimeLine();
                timeLine.setId(ObjectId.get());
                timeLine.setDate(System.currentTimeMillis());
                timeLine.setUserId(userId);
                timeLine.setPublishId(publishId);
                // 动态写入数据  获取好友id 做拼接
                this.mongoTemplate.save(timeLine,"quanzi_time_line_"+ users.getFriendId());
            }
        } catch (Exception e) {
            log.error("写入好友时间线表失败~ userId = " + userId + ", publishId = " + publishId, e);
            //TODO 事务回滚问题
            return CompletableFuture.completedFuture("error");
        }

        return CompletableFuture.completedFuture("ok");
    }
}

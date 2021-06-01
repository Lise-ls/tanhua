package com.th.dubbo.serverIsImpl.api;

import com.alibaba.dubbo.config.annotation.Service;
import com.th.dubbo.serverIsImpl.pojo.RecommendUser;
import com.th.dubbo.serverIsImpl.vo.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

/*
 * @author Lise
 * @date 2021年05月23日 21:30
 * @program: tanhua
 * @description:
 */

@Service(version = "1.0.0")  // 申明dubbo服务
public class RecommendUserApiImpl implements RecommendUserApi{

    // 因需查询mongo 所以 注入 MongoTemplate
    @Autowired
    private MongoTemplate mongoTemplate;


    // 实现 查询一位得分最高的推荐用户，用来完成今日佳人功能
    @Override
    public RecommendUser queryWithMaxScore(Long userId) {

        //查询得分最高的用户，按照得分倒序排序
        Query query = Query.query(Criteria.where("toUserId").is(userId))
                .with(Sort.by(Sort.Order.desc("score"))).limit(1);

        // mongo 客户端调用方法进行查询
        RecommendUser recommendUser = this.mongoTemplate.findOne(query, RecommendUser.class);

        System.out.println("recommendUser = " + recommendUser);

        // 返回查询结果
        return recommendUser;

    }



    //  实现 按照得分倒序，分页查询推荐列表功能实现
    @Override
    public PageInfo<RecommendUser> queryPageInfo(Long userId, Integer pageNum, Integer pageSize) {

        // 分页并排序参数
        PageRequest pageRequest = PageRequest.of((pageNum - 1), pageSize, Sort.by(Sort.Order.desc("score")));

        // 查询参数
        Query query = Query.query(Criteria.where("toUserId").is(userId)).with(pageRequest);

        // 查询mongo 集合
        List<RecommendUser> recommendUserList = this.mongoTemplate.find(query, RecommendUser.class);

        PageInfo pageInfo = new PageInfo<>(0,pageNum, pageSize, recommendUserList);

        System.out.println("pageInfo = " + pageInfo);

        return pageInfo;
    }
}

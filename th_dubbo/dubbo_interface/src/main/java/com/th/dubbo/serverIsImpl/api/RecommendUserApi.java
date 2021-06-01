package com.th.dubbo.serverIsImpl.api;

import com.th.dubbo.serverIsImpl.pojo.RecommendUser;
import com.th.dubbo.serverIsImpl.vo.PageInfo;

public interface RecommendUserApi {


    /**
     * 查询一位得分最高的推荐用户，用来完成今日佳人功能
     *
     * @param userId
     * @return
     */
    RecommendUser queryWithMaxScore(Long userId);

    /**
     * 按照得分倒序，分页查询推荐列表功能实现
     *
     * @param userId
     * @param pageNum
     * @param pageSize
     * @return
     */
    PageInfo<RecommendUser> queryPageInfo(Long userId, Integer pageNum, Integer pageSize);
}

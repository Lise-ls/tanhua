package com.th.server.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.th.common.mapper.UserInfoMapper;
import com.th.common.pojo.UserInfo;
import com.th.common.utils.AppJwtUtil;
import com.th.dubbo.serverIsImpl.api.RecommendUserApi;
import com.th.dubbo.serverIsImpl.pojo.RecommendUser;
import com.th.dubbo.serverIsImpl.vo.PageInfo;
import com.th.server.vo.PageResult;
import com.th.server.vo.RecommendUserQueryParam;
import com.th.server.vo.TodayBest;
import io.jsonwebtoken.Claims;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/*
 * @author Lise
 * @date 2021年05月23日 22:41
 * @program: tanhua
 * @description:
 */

@Service
public class TodayBestService {


    // 采用dubbo包下的进行远程调接口
    @Reference(version = "1.0.0")
    private RecommendUserApi recommendUserApi;

    @Value("${th.sso.default.user}")
    public String defaultUserId;

    @Resource
    private UserInfoMapper userInfoMapper;





   /*
      根据用户id查询推荐的今日佳人信息
        @param token 令牌
        @return
    */
    public TodayBest queryTodayBest(String token){

        Claims claims = AppJwtUtil.getClaimsBody(token);

        int tks = AppJwtUtil.verifyToken(claims);
        // 判断token 是否有效
        if (tks ==1 || tks==2){
            // token 失效 返回null
            return null;
        }
        if (tks ==-1 || tks ==0){
            // token 有效

            // 查询推荐用户
            Integer userId =(Integer)claims.get("id");
            // 远程调用  获取今日佳人方法
            TodayBest todayBest =queryWithMaxScore(Long.valueOf(userId));



            // 判断 返回的佳人信息是否为空
            if (todayBest == null){
                // 为空的情况 设置默认缘分
                todayBest=new TodayBest();

                todayBest.setId(Long.valueOf(defaultUserId));
                todayBest.setFateValue(80L);

                return todayBest;

            }

            // 不为空 将佳人信息补全
            UserInfo userInfo = userInfoMapper.selectOne(Wrappers.<UserInfo>lambdaUpdate().eq(UserInfo::getUserId, todayBest.getId()));

            // 判断信息是否为空
            if (userInfo==null){
                return null;
            }

            // 获取userinfo 信息，补全

            todayBest.setAvatar(userInfo.getLogo());
            todayBest.setId(userInfo.getId());
            todayBest.setNickname(userInfo.getNickName());
            todayBest.setGender(userInfo.getSex().getValue()==1 ? "man" : "woman");
            todayBest.setTags(StringUtils.split(userInfo.getTags()+","));
            todayBest.setAge(userInfo.getAge());
            return todayBest;

        }
        return null;
    }


    // RPC远程调用 获取佳人信息
    private TodayBest queryWithMaxScore(Long userId) {

        // 远程调用接口

        RecommendUser recommendUser =this.recommendUserApi.queryWithMaxScore(userId);

        System.out.println("recommendUser ... th_server = " + this.recommendUserApi);


        //判断佳人信息
        if (recommendUser == null){
            return null;
        }

        TodayBest todayBest=new TodayBest();
        // 将获取的佳人id 设置至 佳人详细信息
        todayBest.setId(recommendUser.getUserId());
        // 缘分获取
        double score = Math.floor(recommendUser.getScore());
        // 设置缘分
        todayBest.setFateValue(Double.valueOf(score).longValue());

        // 返回佳人详细信息
        return todayBest;
    }





    // 推荐列表
    public PageResult queryRecommendation(String token, RecommendUserQueryParam queryParam) {

        Claims claims = AppJwtUtil.getClaimsBody(token);

        int  Vtoken= AppJwtUtil.verifyToken(claims);

        // token 失效
        if (Vtoken == 1 || Vtoken ==2){
            return null;
        }

        // token 有效
        if (Vtoken == -1 || Vtoken ==0){

            // 获取当前页 和 每页显示数
            Integer page = queryParam.getPage();
            Integer pagesize = queryParam.getPagesize();

            //数据封装  目的是返回
            PageResult pageResult=new PageResult();
            pageResult.setPage(page);
            pageResult.setPagesize(pagesize);


            // 根据用户id 进行推荐
            Integer userId  =(Integer) claims.get("id");

            // RPC调用  可获取到远程推荐列表集合
            PageInfo<RecommendUser> recommendUserPageInfo = recommendUserApi.queryPageInfo(Long.valueOf(userId + ""), page, pagesize);


            // 获取推荐列表
            List<RecommendUser> records = recommendUserPageInfo.getRecords();

            //判断 推荐列表是否为空
            if (CollectionUtils.isEmpty(records)){
                // 如果不为空 返回推荐列表
                return pageResult;
                }

            // 集合不为空 将数据进行封装
            Set<Long> userIds = new HashSet<>();
            for (RecommendUser record : records) {
                userIds.add(record.getUserId());
            }

            // 根据条件查询 推荐列表集合
            LambdaQueryWrapper<UserInfo> querwrapper = Wrappers.lambdaQuery();

           //5.1 根据推荐人id去查询
            querwrapper.in(UserInfo::getUserId,userIds);
            /*
            //5.2 根据性别查询
            if(StringUtils.isNotEmpty(queryParam.getGender())){
                querwrapper.eq(UserInfo::getSex,StringUtils.equals(queryParam.getGender(),"man")?1:2);
            }
            //5.3 根据年龄查询,查小于等于
            if(queryParam.getAge()!=null){
                querwrapper.le(UserInfo::getAge,queryParam.getAge());
            }
            //5.4 根据城市查询
            if(StringUtils.isNotEmpty(queryParam.getCity())){
                querwrapper.eq(UserInfo::getCity,queryParam.getCity());
            }
            //5.5 根据学历查询
            if(StringUtils.isNotEmpty(queryParam.getEducation())){
                querwrapper.eq(UserInfo::getEdu,queryParam.getEducation());
            }*/

            // 查询推荐列表 得到集合
            List<UserInfo> userInfos = userInfoMapper.selectList(querwrapper);
            //判断集合是否为空
            if (CollectionUtils.isEmpty(userInfos)){
                // 为空返回空数据
                return pageResult;
            }

            // 如果集合不为null 封装数据
            List<TodayBest> Tolist=new ArrayList<>();

            // 遍历userInfos 推荐列表集合  封装数据
            TodayBest todayBest=new TodayBest();
            for (UserInfo userInfo : userInfos) {
                todayBest.setId(userInfo.getUserId());
                todayBest.setAvatar(userInfo.getLogo());
                todayBest.setNickname(userInfo.getNickName());
                todayBest.setTags(StringUtils.split(userInfo.getTags(), ','));
                todayBest.setGender(userInfo.getSex().getValue() == 1 ? "man" : "woman");
                todayBest.setAge(userInfo.getAge());

                //缘分值
                for (RecommendUser record : records) {
                    if(record.getUserId().longValue() == userInfo.getUserId().longValue()){
                        double score = Math.floor(record.getScore());//取整,98.2 -> 98
                        todayBest.setFateValue(Double.valueOf(score).longValue());
                        break;
                    }
                }
                // 将所有数据添加至 List 集合
                Tolist.add(todayBest);
            }

            //按照缘分值进行倒序排序
            Collections.sort(Tolist, (o1, o2) -> new Long(o2.getFateValue() - o1.getFateValue()).intValue());

            // 封装PageInfo数据
            pageResult.setItems(Tolist);
            return pageResult;
        }
        return null;
    }
}

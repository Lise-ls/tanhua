package com.th.server.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.dubbo.config.annotation.Reference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.th.common.mapper.UserInfoMapper;
import com.th.common.pojo.User;
import com.th.common.pojo.UserInfo;
import com.th.common.service.PicUploadService;
import com.th.common.utils.AppJwtUtil;
import com.th.common.utils.RelativeDateFormat;
import com.th.common.utils.UserThreadLocal;
import com.th.common.vo.PicUploadResult;
import com.th.dubbo.serverIsImpl.api.QuanZiApi;
import com.th.dubbo.serverIsImpl.pojo.Comment;
import com.th.dubbo.serverIsImpl.pojo.Publish;
import com.th.dubbo.serverIsImpl.vo.PageInfo;
import com.th.server.vo.CommentVo;
import com.th.server.vo.PageResult;
import com.th.server.vo.QuanZiVo;
import io.jsonwebtoken.Claims;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.multipart.MultipartFile;


import javax.annotation.Resource;
import java.util.*;

/*
 * @author Lise
 * @date 2021年05月26日 20:29
 * @program: th
 * @description:  圈子好友圈  推荐圈  点赞喜欢
 */

@Service
public class QuanZiService {

    @Reference(version = "1.0.0")
    private QuanZiApi quanZiApi;

    @Resource
    private UserInfoMapper userInfoMapper;

    @Autowired
    private PicUploadService picUploadService;



    /**
     * 好友圈子
     * @param page
     * @param pageSize
     * @param token
     * @return
     */
    public PageResult queryPublishList(Integer page, Integer pageSize, String token) {

        //封装数据
        PageResult pageResult = new PageResult();
        pageResult.setPage(page);
        pageResult.setPagesize(pageSize);

        //校验token
        Claims claims = AppJwtUtil.getClaimsBody(token);
        int count = AppJwtUtil.verifyToken(claims);
        if(count==1 || count==2){
            //表示过期了
            return pageResult;
        }

        if (count==-1 || count==0){
            //有效期内容
            PageInfo<Publish> pageInfo = quanZiApi.queryPublishList(Long.parseLong(claims.get("id")+""), page, pageSize);

            List<Publish> records = pageInfo.getRecords();
            if (CollUtil.isEmpty(records)){
                return pageResult;
            }

            //获取数据，并封装list集合中
            List<QuanZiVo> quanZiVoList = new ArrayList<>();
            records.forEach(publish -> {
                QuanZiVo quanZiVo = new QuanZiVo();
                quanZiVo.setId(publish.getId().toHexString());
                quanZiVo.setTextContent(publish.getText());
                quanZiVo.setImageContent(publish.getMedias().toArray(new String[]{}));
                quanZiVo.setUserId(publish.getUserId());
                quanZiVo.setCreateDate(RelativeDateFormat.format(new Date(publish.getCreated())));

                quanZiVoList.add(quanZiVo);

            });


            //查询用户信息
            List<Object> userIds = CollUtil.getFieldValues(records, "userId");

            LambdaQueryWrapper<UserInfo> querywrapper= Wrappers.<UserInfo>lambdaQuery().in(UserInfo::getUserId,userIds);
            List<UserInfo> userInfoList = userInfoMapper.selectList(querywrapper);

            //找到对应的用户信息
            for (QuanZiVo quanZiVo : quanZiVoList) {
                for (UserInfo userInfo : userInfoList) {
                    if(quanZiVo.getUserId().longValue() == userInfo.getUserId().longValue()){
                        this.fillUserInfoToQuanZiVo(userInfo, quanZiVo);
                        break;
                    }
                }
            }

            pageResult.setItems(quanZiVoList);
            return pageResult;
    }
            return null;
}


    /**
     * 填充用户信息
     * @param userInfo 用户详情
     * @param quanZiVo vo返回实体
     */
    private void fillUserInfoToQuanZiVo(UserInfo userInfo, QuanZiVo quanZiVo) {
        BeanUtil.copyProperties(userInfo, quanZiVo, "id");
        quanZiVo.setGender(userInfo.getSex().name().toLowerCase());
        quanZiVo.setTags(StringUtils.split(userInfo.getTags(), ','));

        //当前用户
        User user = UserThreadLocal.get();

        quanZiVo.setCommentCount(0); //TODO 评论数
        quanZiVo.setDistance("1.2公里"); //TODO 距离


        quanZiVo.setHasLiked(this.quanZiApi.queryUserIsLike(user.getId(), quanZiVo.getId()) ? 1 : 0); //是否点赞（1是，0否）
        quanZiVo.setLikeCount(Convert.toInt(this.quanZiApi.queryLikeCount(quanZiVo.getId()))); //点赞数


        quanZiVo.setHasLoved(this.quanZiApi.queryUserIsLove(user.getId(), quanZiVo.getId()) ? 1 : 0); //是否喜欢（1是，0否）
        quanZiVo.setLoveCount(Convert.toInt(this.quanZiApi.queryLoveCount(quanZiVo.getId()))); //喜欢数
        }


    /**
     * 发布动态
     * @param textContent
     * @param location
     * @param latitude
     * @param longitude
     * @param multipartFile
     * @param token
     * @return
     */
    public String savePublish(String textContent,
                              String location,
                              String latitude,
                              String longitude,
                              MultipartFile[] multipartFile,
                              String token) {

        //查询当前的登录信息
        User user = UserThreadLocal.get();
        System.out.println(".... 本地线程获取 user = " + user);

       /*  UserThreadLocal  获取不到的情况，采用下面原始的方式

        Claims claims = AppJwtUtil.getClaimsBody(token);
        int tks = AppJwtUtil.verifyToken(claims);
        // 查询推荐用户
        Integer userId =(Integer)claims.get("id");
        //publish.setUserId(Long.valueOf(userId+""));
        */

        Publish publish = new Publish();

        publish.setUserId(user.getId());
        publish.setText(textContent);
        publish.setLocationName(location);
        publish.setLatitude(latitude);
        publish.setLongitude(longitude);
        publish.setSeeType(1);

        List<String> picUrls = new ArrayList<>();
        //图片上传
        for (MultipartFile file : multipartFile) {
            PicUploadResult picUploadResult = this.picUploadService.upload(file);
            picUrls.add(picUploadResult.getName());
        }

        publish.setMedias(picUrls);
        return this.quanZiApi.savePublish(publish);

    }


    /**
     * 推荐圈子
     * @param page
     * @param pageSize
     * @return
     */
    public PageResult queryRecommendPublishList(Integer page, Integer pageSize) {

        //分析：通过dubbo中的服务查询系统推荐动态
        //通过mysql查询用户的信息，回写到结果对象中（QuanZiVo）

        PageResult pageResult = new PageResult();
        pageResult.setPage(page);
        pageResult.setPagesize(pageSize);

        //直接从ThreadLocal中获取对象
        User user = UserThreadLocal.get();

        //通过dubbo查询数据
        PageInfo<Publish> pageInfo = this.quanZiApi.queryRecommendPublishList(user.getId(), page, pageSize);
        System.out.println("quanZiApi = " + quanZiApi);

        List<Publish> records = pageInfo.getRecords();
        if (CollUtil.isEmpty(records)) {
            return pageResult;
        }

        pageResult.setItems(this.fillQuanZiVo(records));
        return pageResult;
    }


    /**
     * 根据查询到的publish集合填充QuanZiVo对象
     * @param records
     * @return
     */
    private List<QuanZiVo> fillQuanZiVo(List<Publish> records) {

        List<QuanZiVo> quanZiVoList = new ArrayList<>();
        records.forEach(publish -> {
            QuanZiVo quanZiVo = new QuanZiVo();
            quanZiVo.setId(publish.getId().toHexString());
            quanZiVo.setTextContent(publish.getText());
            quanZiVo.setImageContent(publish.getMedias().toArray(new String[]{}));
            quanZiVo.setUserId(publish.getUserId());
            quanZiVo.setCreateDate(RelativeDateFormat.format(new Date(publish.getCreated())));

            quanZiVoList.add(quanZiVo);
        });

        //查询用户信息
        List<Object> userIds = CollUtil.getFieldValues(records, "userId");

        LambdaQueryWrapper<UserInfo> querywrapper=
                Wrappers.<UserInfo>lambdaQuery().in(UserInfo::getUserId,userIds);

        List<UserInfo> userInfoList = this.userInfoMapper.selectList(querywrapper);
        for (QuanZiVo quanZiVo : quanZiVoList) {
            //找到对应的用户信息
            for (UserInfo userInfo : userInfoList) {
                if(quanZiVo.getUserId().longValue() == userInfo.getUserId().longValue()){
                    this.fillUserInfoToQuanZiVo(userInfo, quanZiVo);
                    break;
                }
            }
        }

        return quanZiVoList;
    }




    // ------------ 以下点赞 -------------

    /**
     * 点赞
     * @param publishId
     * @return
     */
    public Long likeComment(String publishId) {

        User user = UserThreadLocal.get();
        //点赞
        Boolean result = this.quanZiApi.likeComment(user.getId(), publishId);
        if(result){
            //查询点赞数
            return this.quanZiApi.queryLikeCount(publishId);
        }
        return null;
    }


    /**
     * 取消点赞
     * @param publishId
     * @return
     */
    public Long disLikeComment(String publishId) {

        User user = UserThreadLocal.get();
        //取消点赞
        Boolean result = this.quanZiApi.disLikeComment(user.getId(), publishId);
        if(result){
            //查询点赞数
            return this.quanZiApi.queryLikeCount(publishId);
        }
        return null;
    }



    // ------------ 以下喜欢 -------------

    /**
     * 喜欢
     * @param publishId
     * @return
     */
    public Long loveComment(String publishId) {

        User user = UserThreadLocal.get();
        //喜欢
        Boolean result = this.quanZiApi.loveComment(user.getId(), publishId);
        if(result){
            //查询喜欢数
            return this.quanZiApi.queryLoveCount(publishId);
        }
        return null;
    }


    /**
     * 取消喜欢
     * @param publishId
     * @return
     */
    public Long disLoveComment(String publishId) {
        User user = UserThreadLocal.get();
        //取消喜欢
        Boolean result = this.quanZiApi.disLoveComment(user.getId(), publishId);
        if(result){
            //查询喜欢数
            return this.quanZiApi.queryLoveCount(publishId);
        }
        return null;
    }


    /**
     * 查询单条动态
     * @param publishId
     * @return
     */
    public QuanZiVo queryById(String publishId) {

        Publish publish = this.quanZiApi.queryPublishById(publishId);
        if (publish == null) {
            return null;
        }
        return this.fillQuanZiVo(Arrays.asList(publish)).get(0);
    }




    /**
     * 查询评论列表
     * @param publishId
     * @param page
     * @param pageSize
     * @return
     */
    public PageResult queryCommentList(String publishId, Integer page, Integer pageSize) {

        PageResult pageResult = new PageResult();
        pageResult.setPage(page);
        pageResult.setPagesize(pageSize);

        User user = UserThreadLocal.get();

        //查询评论列表数据
        PageInfo<Comment> pageInfo = this.quanZiApi.queryCommentList(publishId, page, pageSize);
        List<Comment> records = pageInfo.getRecords();
        if(CollUtil.isEmpty(records)){
            return pageResult;
        }

        //查询用户信息
        List<Object> userIdList = CollUtil.getFieldValues(records, "userId");
        //自定义方法
        List<UserInfo> userInfoList = this.queryUserInfoByUserIdList(userIdList);

        List<CommentVo> result = new ArrayList<>();
        for (Comment record : records) {
            CommentVo commentVo = new CommentVo();
            commentVo.setContent(record.getContent());
            commentVo.setId(record.getId().toHexString());
            commentVo.setCreateDate(DateUtil.format(new Date(record.getCreated()), "HH:mm"));
            //是否点赞
            commentVo.setHasLiked(this.quanZiApi.queryUserIsLike(user.getId(), commentVo.getId()) ? 1 : 0);
            //点赞数
            commentVo.setLikeCount(Convert.toInt(this.quanZiApi.queryLikeCount(commentVo.getId())));

            for (UserInfo userInfo : userInfoList) {
                if(ObjectUtil.equals(record.getUserId(), userInfo.getUserId())){

                    commentVo.setAvatar(userInfo.getLogo());
                    commentVo.setNickname(userInfo.getNickName());

                    break;
                }
            }

            result.add(commentVo);
        }

        pageResult.setItems(result);

        return pageResult;
    }


    /**
     * 查询评论列表  -- 辅助
     * @param userIds
     * @return
     */
    private List<UserInfo> queryUserInfoByUserIdList(List<Object> userIds) {

        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("user_id",userIds);
        List<UserInfo> userInfos = userInfoMapper.selectList(queryWrapper);
        return userInfos;

    }


    /**
     * 发表评论
     * @param publishId
     * @param content
     * @return
     */
    public Boolean saveComments(String publishId, String content) {

        User user = UserThreadLocal.get();
        return this.quanZiApi.saveComment(user.getId(), publishId, content);
    }
}

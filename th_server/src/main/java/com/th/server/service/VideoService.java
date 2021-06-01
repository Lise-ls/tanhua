package com.th.server.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.dubbo.config.annotation.Reference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.tobato.fastdfs.domain.conn.FdfsWebServer;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.th.common.mapper.UserInfoMapper;
import com.th.common.pojo.User;
import com.th.common.pojo.UserInfo;
import com.th.common.service.PicUploadService;
import com.th.common.utils.UserThreadLocal;
import com.th.common.vo.PicUploadResult;
import com.th.dubbo.serverIsImpl.api.QuanZiApi;
import com.th.dubbo.serverIsImpl.api.VideoApi;
import com.th.dubbo.serverIsImpl.pojo.Video;
import com.th.dubbo.serverIsImpl.vo.PageInfo;
import com.th.server.vo.PageResult;
import com.th.server.vo.VideoVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/*
 * @author Lise
 * @date 2021年05月30日 21:50
 * @program: tanhua
 */

@Service
@Slf4j
public class VideoService {

    @Autowired
    private PicUploadService picUploadService;

    @Autowired
    protected FastFileStorageClient storageClient;

    @Autowired
    private FdfsWebServer fdfsWebServer;

    @Resource
    private UserInfoMapper userInfoMapper;

    @Autowired
    private QuanZiService quanZiService;


    @Reference(version = "1.0.0")
    private QuanZiApi quanZiApi;

    @Reference(version = "1.0.0")
    private VideoApi videoApi;




    /**
     * 发布视频
     * @param picFile
     * @param videoFile
     * @return
     */
    public Boolean saveVideo(MultipartFile picFile, MultipartFile videoFile) {

        User user = UserThreadLocal.get();

        Video video = new Video();
        video.setUserId(user.getId());
        video.setSeeType(1); //默认公开
        try {
            //上传封面图片
            PicUploadResult picUploadResult = this.picUploadService.upload(picFile);
            video.setPicUrl(picUploadResult.getName()); //图片路径

            //上传视频
            StorePath storePath = storageClient.uploadFile(videoFile.getInputStream(),
                    videoFile.getSize(),
                    StrUtil.subAfter(videoFile.getOriginalFilename(), '.', true),
                    null);

            //设置视频url
            video.setVideoUrl(fdfsWebServer.getWebServerUrl() + storePath.getFullPath());

            String videoId = this.videoApi.saveVideo(video);
            return StrUtil.isNotEmpty(videoId);
        } catch (Exception e) {
            log.error("发布小视频失败！file = " + picFile.getOriginalFilename() , e);
        }

        return false;
    }



    /**
     * 查询小视频列表
     * @param page
     * @param pageSize
     * @return
     */
    public PageResult queryVideoList(Integer page, Integer pageSize) {

        User user = UserThreadLocal.get();

        PageResult pageResult = new PageResult();
        pageResult.setPage(page);
        pageResult.setPagesize(pageSize);

        PageInfo<Video> pageInfo = this.videoApi.queryVideoList(user.getId(), page, pageSize);
        List<Video> records = pageInfo.getRecords();

        if(CollUtil.isEmpty(records)){
            return pageResult;
        }


        //查询用户信息
        List<Object> userIds = CollUtil.getFieldValues(records, "userId");

        //通过Userinfomapper 查询用户信息
        List<UserInfo> userInfoList = this.queryUserInfoByUserIdList(userIds);

        List<VideoVo> videoVoList = new ArrayList<>();
        for (Video record : records) {
            VideoVo videoVo = new VideoVo();

            videoVo.setUserId(record.getUserId());
            videoVo.setCover(record.getPicUrl());
            videoVo.setVideoUrl(record.getVideoUrl());
            videoVo.setId(record.getId().toHexString());
            videoVo.setSignature("木奥比"); //TODO 签名

            videoVo.setCommentCount(Convert.toInt(this.quanZiApi.queryCommentCount(videoVo.getId()))); //评论数
            videoVo.setHasFocus(this.videoApi.isFollowUser(user.getId(), videoVo.getUserId()) ? 1 : 0); //是否关注


            videoVo.setHasLiked(this.quanZiApi.queryUserIsLike(user.getId(), videoVo.getId()) ? 1 : 0); //是否点赞（1是，0否）
            videoVo.setLikeCount(Convert.toInt(this.quanZiApi.queryLikeCount(videoVo.getId())));//点赞数


            //填充用户信息
            for (UserInfo userInfo : userInfoList) {
                if (ObjectUtil.equals(videoVo.getUserId(), userInfo.getUserId())) {
                    videoVo.setNickname(userInfo.getNickName());
                    videoVo.setAvatar(userInfo.getLogo());
                    break;
                }
            }

            videoVoList.add(videoVo);
    }
        pageResult.setItems(videoVoList);
        return pageResult;
}


    // 根据用户id 查询用户信息
    private List<UserInfo> queryUserInfoByUserIdList(List<Object> userIds) {

        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("user_id",userIds);
        List<UserInfo> userInfos = userInfoMapper.selectList(queryWrapper);
        return userInfos;
    }



    /**
     * 视频点赞
     * @param videoId 视频id
     * @return
     */
    public Long likeComment(String videoId) {

        User user = UserThreadLocal.get();
        Boolean result = this.quanZiApi.likeComment(user.getId(), videoId);
        if (result) {
            return this.quanZiApi.queryLikeCount(videoId);
        }
        return null;
    }


    /**
     * 取消点赞
     * @param videoId
     * @return
     */
    public Long disLikeComment(String videoId) {

        User user = UserThreadLocal.get();
        Boolean result = this.quanZiApi.disLikeComment(user.getId(), videoId);
        if (result) {
            return this.quanZiApi.queryLikeCount(videoId);
        }
        return null;
    }


    /**
     * 评论列表
     * @param videoId
     * @param page
     * @param pageSize
     * @return
     */
    public PageResult queryCommentList(String videoId, Integer page, Integer pageSize) {
        return this.quanZiService.queryCommentList(videoId, page, pageSize);
    }


    /**
     * 提交评论
     * @param videoId
     * @param content
     * @return
     */
    public Boolean saveComment(String videoId, String content) {
        return this.quanZiService.saveComments(videoId, content);
    }


    /**
     * 关注用户
     * @param userId
     * @return
     */
    public Boolean followUser(Long userId) {

        User user = UserThreadLocal.get();
        return this.videoApi.followUser(user.getId(), userId);
    }

    /**
     * 取消关注
     * @param userId
     * @return
     */
    public Boolean disFollowUser(Long userId) {

        User user = UserThreadLocal.get();
        return this.videoApi.disFollowUser(user.getId(), userId);
    }
}


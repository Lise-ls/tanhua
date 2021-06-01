package com.th.server.controller;

/*
 * @author Lise
 * @date 2021年05月26日 20:25
 * @program: th
 * @description: 圈子好友圈  推荐圈  点赞喜欢
 */

import cn.hutool.core.util.StrUtil;
import com.th.server.service.QuanZiService;
import com.th.server.vo.PageResult;
import com.th.server.vo.QuanZiVo;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.Collections;

@RestController
@RequestMapping("movements")
public class QuanZiController {

    @Resource
    private QuanZiService quanZiService;



    /**
     * 查询好友圈子
     * @param page
     * @param pageSize
     * @param token
     * @return
     */
    @GetMapping
    public PageResult queryPublishList(@RequestParam(value = "page", defaultValue = "1") Integer page,
                                       @RequestParam(value = "pagesize", defaultValue = "10") Integer pageSize,
                                       @RequestHeader("Authorization") String token) {

        return this.quanZiService.queryPublishList(page, pageSize, token);

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
    @PostMapping
    public ResponseEntity<Void> savePublish(@RequestParam("textContent") String textContent,
                                            @RequestParam(value = "location", required = false) String location,
                                            @RequestParam(value = "latitude", required = false) String latitude,
                                            @RequestParam(value = "longitude", required = false) String longitude,
                                            @RequestParam(value = "imageContent", required = false) MultipartFile[] multipartFile,
                                            @RequestHeader("Authorization") String token) {
        try {
            String publishId = this.quanZiService.savePublish(textContent, location, latitude, longitude, multipartFile,token);
            if (StrUtil.isNotEmpty(publishId)) {
                return ResponseEntity.ok(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }


    /**
     * 推荐动态
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("recommend")
    public PageResult queryRecommendPublishList(@RequestParam(value = "page", defaultValue = "1") Integer page,
                                                @RequestParam(value = "pagesize", defaultValue = "10") Integer pageSize) {

        return this.quanZiService.queryRecommendPublishList(page, pageSize);
    }





    // -------------- 以下点赞 ---------------

    /**
     * 点赞
     * @param publishId
     * @return
     */
    @GetMapping("/{id}/like")
    public ResponseEntity<Long> likeComment(@PathVariable("id") String publishId) {
        try {
            Long likeCount = this.quanZiService.likeComment(publishId);
            if (likeCount != null) {
                return ResponseEntity.ok(likeCount);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * 取消点赞
     * @param publishId
     * @return
     */
    @GetMapping("/{id}/dislike")
    public ResponseEntity<Long> disLikeComment(@PathVariable("id") String publishId) {
        try {
            Long likeCount = this.quanZiService.disLikeComment(publishId);
            if (null != likeCount) {
                return ResponseEntity.ok(likeCount);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }


    /**
     * 喜欢
     * @param publishId
     * @return
     */
    @GetMapping("/{id}/love")
    public ResponseEntity<Long> loveComment(@PathVariable("id") String publishId) {
        try {
            Long loveCount = this.quanZiService.loveComment(publishId);
            if (null != loveCount) {
                return ResponseEntity.ok(loveCount);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }


    /**
     * 取消喜欢
     * @param publishId
     * @return
     */
    @GetMapping("/{id}/unlove")
    public ResponseEntity<Long> disLoveComment(@PathVariable("id") String publishId) {
        try {
            Long loveCount = this.quanZiService.disLoveComment(publishId);
            if (null != loveCount) {
                return ResponseEntity.ok(loveCount);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }


    /**
     * 查询单条动态信息
     * @param publishId
     * @return
     */
    @GetMapping("/{id}")
    public ResponseEntity<QuanZiVo> queryById(@PathVariable("id") String publishId) {
        try {
            QuanZiVo movements = this.quanZiService.queryById(publishId);
            if (null != movements) {
                return ResponseEntity.ok(movements);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }


    /**
     * TODO：谁看过我
     * @return
     */
    @GetMapping("visitors")
    public ResponseEntity<Object> queryVisitors() {
        return ResponseEntity.ok(Collections.EMPTY_LIST);
    }
}

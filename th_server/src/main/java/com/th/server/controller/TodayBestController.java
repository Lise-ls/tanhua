package com.th.server.controller;

/*
 * @author Lise
 * @date 2021年05月23日 22:34
 * @program: tanhua
 * @description:
 */

import com.th.common.utils.Cache;
import com.th.server.service.TodayBestService;
import com.th.server.vo.PageResult;
import com.th.server.vo.RecommendUserQueryParam;
import com.th.server.vo.TodayBest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("tanhua")
@Slf4j
public class TodayBestController {


        @Resource
        private TodayBestService todayBestService;


        // 今日佳人
        @GetMapping("/todayBest")
        public ResponseEntity<TodayBest> responseEntity(@RequestHeader("Authorization") String token){

            // 调用 queryTodayBest 查询  如果查询到的 佳人信息不为空， 返回成功。
            TodayBest todayBest = todayBestService.queryTodayBest(token);

            try {
                if (null !=todayBest){
                    return ResponseEntity.ok(todayBest);
                }
            } catch (Exception e) {
                log.error("无法查询出佳人信息 ... " +token,e);
            }

            // 无法查询发返回空
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }



        // 推荐朋友
        @GetMapping("/recommendation")
        @Cache(time = "30")
        public ResponseEntity<PageResult>  queryRecommendation(@RequestHeader("Authorization") String token,
                                                               RecommendUserQueryParam queryParam){

            PageResult pageResult= this.todayBestService.queryRecommendation(token,queryParam);


            try {
                if (pageResult  != null){
                    return ResponseEntity.ok(pageResult);
                }
            } catch (Exception e) {
                log.error("推荐朋友列表查询错误 ... " +token,e);
            }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
}

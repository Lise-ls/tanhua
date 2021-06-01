package com.th.dubbo.serverIsImpl.api;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.PageUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.dubbo.config.annotation.Service;
import com.th.common.enums.CommentType;
import com.th.dubbo.serverIsImpl.enums.IdType;
import com.th.dubbo.serverIsImpl.pojo.*;
import com.th.dubbo.serverIsImpl.service.TimeLineService;
import com.th.dubbo.serverIsImpl.service.idAuto;
import com.th.dubbo.serverIsImpl.vo.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.List;

/*
 * @author Lise
 * @date 2021年05月26日 18:18
 * @program: tanhua
 * @description:
 */
@Service(version = "1.0.0")
@Slf4j
public class QuanZiApiImpl implements QuanZiApi{

    // mongo 客户端引入
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private idAuto idAuto;

    @Autowired
    private TimeLineService timeLineService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;


    @Autowired
    private VideoApi videoApi;


    // 好友圈子实现
    @Override
    public PageInfo<Publish> queryPublishList(Long userId, Integer page, Integer pageSize) {

        // 查询好友圈 发布内容 因为发布表是同一个 数据量很大  所以考虑分页
        PageInfo<Publish> pageInfo = new PageInfo<Publish>();
        pageInfo.setPageNum(page);
        pageInfo.setPageSize(pageSize);

        // 根据 时间线表的 创建时间进行排序
        // Pageable  分页工具
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Order.desc("data")));

        // Query 自带 查询分页数据
        Query query = new Query().with(pageable);


        // 查询时间表内容
        List<TimeLine> timeLines = this.mongoTemplate.find(query, TimeLine.class, "quanzi_time_line_" + userId);

        // 判断 时间线内容是否为空
        if (CollUtil.isEmpty(timeLines)){
            return pageInfo;
        }


        // timeLine
        //获取时间线列表中的发布id的列表
        List<Object> publishId = CollUtil.getFieldValues(timeLines, "publishId");


        // Publish
        //根据动态id查询动态列表 且根据 created 排序
        Query querypublish = Query.query(Criteria.where("id").in(publishId)).with(Sort.by(Sort.Order.desc("created")));

        List<Publish> publishes = this.mongoTemplate.find(querypublish, Publish.class);
        pageInfo.setRecords(publishes);

        return pageInfo;
    }




    // 发布动态
    @Override
    public String savePublish(Publish publish) {

        // 逻辑判断  对Publish校验
        if (!ObjectUtil.isAllNotEmpty(publish.getUserId(),publish.getText())){
            // 发布失败
            return null;
        }

        // 设置主键id
        publish.setId(ObjectId.get());

        try {
            // 设置自增长的pid
            publish.setPid(this.idAuto.created(IdType.PUBLISH));
            publish.setCreated(System.currentTimeMillis());

            //写入到publish表中
            this.mongoTemplate.save(publish);

            // 写入相册
            Album album=new Album();
            album.setId(ObjectId.get());
            album.setCreated(System.currentTimeMillis());
            album.setPublishId(publish.getId());

            this.mongoTemplate.save(album,"quanzi_album_" +publish.getUserId());

            //写入好友的时间线表（异步写入）
            this.timeLineService.saveTimeLine(publish.getUserId(), publish.getId());
        } catch (Exception e) {
            //TODO 需要做事务的回滚，Mongodb的单节点服务，不支持事务，对于回滚我们暂时不实现了
            log.error("发布动态失败~ publish = " + publish, e);
        }

        return publish.getId().toHexString();
    }




    // 推荐列表查询
    @Override
    public PageInfo<Publish> queryRecommendPublishList(Long userId, Integer page, Integer pageSize) {

        PageInfo<Publish> pageInfo = new PageInfo<>();
        pageInfo.setPageNum(page);
        pageInfo.setPageSize(pageSize);


        // 查询推荐结果数据
        String key = "QUANZI_PUBLISH_RECOMMEND_" + userId;
        String data = this.redisTemplate.opsForValue().get(key);
        if (StrUtil.isEmpty(data)) {
            return pageInfo;
        }



        //查询到的pid进行分页处理
        List<String> pids = StrUtil.split(data, ',');
        //计算分页
        //[0, 10]
        int[] startEnd = PageUtil.transToStartEnd(page - 1, pageSize);
        int startIndex = startEnd[0]; //开始
        int endIndex = Math.min(startEnd[1], pids.size()); //结束

        List<Long> pidLongList = new ArrayList<>();
        for (int i = startIndex; i < endIndex; i++) {
            pidLongList.add(Long.valueOf(pids.get(i)));
        }

        if (CollUtil.isEmpty(pidLongList)) {
            //没有查询到数据
            return pageInfo;
        }

        //根据pid查询publish
        Query query = Query.query(Criteria.where("pid").in(pidLongList))
                .with(Sort.by(Sort.Order.desc("created")));
        List<Publish> publishList = this.mongoTemplate.find(query, Publish.class);
        if (CollUtil.isEmpty(publishList)) {
            //没有查询到数据
            return pageInfo;
        }

        pageInfo.setRecords(publishList);
        return pageInfo;
    }



    // -----------  点赞和喜欢业务  -------------------


    // 根据id 查询动态
    @Override
    public Publish queryPublishById(String id) {
        return this.mongoTemplate.findById(new ObjectId(id), Publish.class);
    }


    /**
     * 发表评论
     * @param userId
     * @param publishId
     * @param commentType
     * @param content
     * @return
     */
    private Boolean saveComment(Long userId, String publishId,
                                CommentType commentType, String content) {
        try {
            Comment comment = new Comment();
            comment.setId(ObjectId.get());
            comment.setUserId(userId);
            comment.setPublishId(new ObjectId(publishId));
            // 评论类型
            comment.setCommentType(commentType.getType());
            // 内容
            comment.setContent(content);
            comment.setCreated(System.currentTimeMillis());

            Publish publish = this.queryPublishById(publishId);
            if (ObjectUtil.isNotEmpty(publish)) {
                comment.setPublishUserId(publish.getUserId());
            } else {
                //查询评论
                Comment myComment = this.queryCommentById(publishId);
                if (ObjectUtil.isNotEmpty(myComment)) {
                    comment.setPublishUserId(myComment.getUserId());
                } else {
                    //查询小视频
                    Video video = this.videoApi.queryVideoById(publishId);
                    if (ObjectUtil.isNotEmpty(video)) {
                        comment.setPublishUserId(video.getUserId());
                    } else {
                        // 其他情况，直接返回
                        return false;
                    }
                }
            }

            //TODO 其他评论对象，暂不处理
            comment.setPublishUserId(publish.getUserId());
            this.mongoTemplate.save(comment);
            return true;

        } catch (Exception e) {
            log.error("保存Comment出错~ userId = " + userId + ", publishId = " + publishId + ", commentType = " + commentType, e);
        }

        return false;
    }

    // ----------------   有点问题,已修改。
    /**
     * 查询评论
     * @param publishId
     * @return
     */
    private Comment queryCommentById(String publishId) {

        Query query = new Query(Criteria
                .where("publishId").is(new ObjectId(publishId))
                .and("commentType").is(CommentType.COMMENT.getType()));
        //查询评论列表
        Comment comment = this.mongoTemplate.findOne(query, Comment.class);
        //打印
        System.out.println("comment = " + comment);
        return comment;
    }






    /**
     * 删除评论数据
     * @param userId
     * @param publishId
     * @param commentType
     * @return
     */
    private Boolean removeComment(Long userId, String publishId, CommentType commentType) {
        Query query = Query.query(Criteria.where("userId").is(userId)
                .and("publishId").is(new ObjectId(publishId))
                .and("commentType").is(commentType.getType())
        );
        return this.mongoTemplate.remove(query, Comment.class).getDeletedCount() > 0;
    }


    /**
     * 查询数量
     * @param publishId
     * @param commentType
     * @return
     */
    private Long queryCommentCount(String publishId, CommentType commentType) {
        Query query = Query.query(Criteria.where("publishId").is(new ObjectId(publishId))
                .and("commentType").is(commentType.getType())
        );
        return this.mongoTemplate.count(query, Comment.class);
    }



    //评论数据存储在Redis中key的前缀
    private static final String COMMENT_REDIS_KEY_PREFIX = "QUANZI_COMMENT_";

    //用户是否点赞的前缀
    private static final String COMMENT_USER_LIEK_REDIS_KEY_PREFIX = "USER_LIKE_";

    //用户是否喜欢的前缀
    private static final String COMMENT_USER_LOVE_REDIS_KEY_PREFIX = "USER_LOVE_";


    // 点赞
    @Override
    public Boolean likeComment(Long userId, String publishId) {
        //判断该用户是否已经点赞，如果已经点赞就直接返回
        if (this.queryUserIsLike(userId, publishId)) {
            return false;
        }
        //保存Comment数据
        Boolean result = this.saveComment(userId, publishId, CommentType.LIKE, null);
        if (!result) {
            return false;
        }

        //修改redis中的点赞数以及是否点赞
        //修改点赞数
        String redisKey = this.getCommentRedisKeyPrefix(publishId);
        String hashKey = CommentType.LIKE.toString();
        this.redisTemplate.opsForHash().increment(redisKey, hashKey, 1);

        //用户是否点赞
        String userHashKey = this.getCommentUserLikeRedisKeyPrefix(userId);
        this.redisTemplate.opsForHash().put(redisKey, userHashKey, "1");

        return true;
    }

    private String getCommentRedisKeyPrefix(String publishId) {
        return COMMENT_REDIS_KEY_PREFIX + publishId;
    }

    private String getCommentUserLikeRedisKeyPrefix(Long userId) {
        return COMMENT_USER_LIEK_REDIS_KEY_PREFIX + userId;
    }


    // 取消点赞
    @Override
    public Boolean disLikeComment(Long userId, String publishId) {
        //判断用户是否已经点赞，如果没有点赞就返回
        if (!this.queryUserIsLike(userId, publishId)) {
            return false;
        }

        //删除评论数据
        Boolean result = this.removeComment(userId, publishId, CommentType.LIKE);
        if (!result) {
            return false;
        }
        //修改Redis中的数据
        //修改点赞数
        String redisKey = this.getCommentRedisKeyPrefix(publishId);
        String hashKey = CommentType.LIKE.toString();
        this.redisTemplate.opsForHash().increment(redisKey, hashKey, -1);

        //用户是否点赞
        String userHashKey = this.getCommentUserLikeRedisKeyPrefix(userId);
        this.redisTemplate.opsForHash().delete(redisKey, userHashKey);

        return true;
    }


    // 查询点赞数
    @Override
    public Long queryLikeCount(String publishId) {
        //从Redis中命中查询，如果命中直接返回即可
        String redisKey = this.getCommentRedisKeyPrefix(publishId);
        String hashKey = CommentType.LIKE.toString();
        Object data = this.redisTemplate.opsForHash().get(redisKey, hashKey);
        if (ObjectUtil.isNotEmpty(data)) {
            return Convert.toLong(data);
        }

        //查询Mongodb
        Long count = this.queryCommentCount(publishId, CommentType.LIKE);
        //写入Redis中
        this.redisTemplate.opsForHash().put(redisKey, hashKey, String.valueOf(count));

        return count;
    }


    // 查询用户是否点赞该动态
    @Override
    public Boolean queryUserIsLike(Long userId, String publishId) {
        //从redis中查询数据
        String redisKey = this.getCommentRedisKeyPrefix(publishId);
        String userHashKey = this.getCommentUserLikeRedisKeyPrefix(userId);
        Object data = this.redisTemplate.opsForHash().get(redisKey, userHashKey);
        if (ObjectUtil.isNotEmpty(data)) {
            return StrUtil.equals(Convert.toStr(data), "1");
        }

        //查询Mongodb，确定是否已经点赞
        Query query = Query.query(Criteria.where("publishId").is(new ObjectId(publishId))
                .and("userId").is(userId)
                .and("commentType").is(CommentType.LIKE)
        );
        long count = this.mongoTemplate.count(query, Comment.class);
        if(count == 0){
            return false;
        }

        //写入到redis中
        this.redisTemplate.opsForHash().put(redisKey, userHashKey, "1");

        return true;
    }


    // 喜欢
    @Override
    public Boolean loveComment(Long userId, String publishId) {
        //查询该用户是否已经喜欢
        if (this.queryUserIsLove(userId, publishId)) {
            return false;
        }

        //喜欢
        boolean result = this.saveComment(userId, publishId, CommentType.LOVE, null);
        if (!result) {
            return false;
        }

        //喜欢成功后，修改Redis中的总的喜欢数
        String redisKey = this.getCommentRedisKeyPrefix(publishId);
        String hashKey = CommentType.LOVE.toString();
        this.redisTemplate.opsForHash().increment(redisKey, hashKey, 1);

        //标记用户已经喜欢
        hashKey = this.getCommentUserLoveRedisKey(userId);
        this.redisTemplate.opsForHash().put(redisKey, hashKey, "1");

        return true;
    }


    private String getCommentUserLoveRedisKey(Long userId) {
        return COMMENT_USER_LOVE_REDIS_KEY_PREFIX + userId;
    }



    // 取消喜欢
    @Override
    public Boolean disLoveComment(Long userId, String publishId) {
        if (!this.queryUserIsLove(userId, publishId)) {
            //如果用户没有喜欢，就直接返回
            return false;
        }

        boolean result = this.removeComment(userId, publishId, CommentType.LOVE);
        if (!result) {
            //删除失败
            return false;
        }

        //删除redis中的记录
        String redisKey = this.getCommentRedisKeyPrefix(publishId);
        String hashKey = this.getCommentUserLoveRedisKey(userId);
        this.redisTemplate.opsForHash().delete(redisKey, hashKey);
        this.redisTemplate.opsForHash().increment(redisKey, CommentType.LOVE.toString(), -1);

        return true;
    }


    // 查询喜欢数
    @Override
    public Long queryLoveCount(String publishId) {
        //首先从redis中命中，如果命中的话就返回，没有命中就查询Mongodb
        String redisKey = this.getCommentRedisKeyPrefix(publishId);
        String hashKey = CommentType.LOVE.toString();
        Object value = this.redisTemplate.opsForHash().get(redisKey, hashKey);
        if (ObjectUtil.isNotEmpty(value)) {
            return Convert.toLong(value);
        }
        //查询count
        Long count = this.queryCommentCount(publishId, CommentType.LOVE);

        //存储到redis中
        this.redisTemplate.opsForHash().put(redisKey, hashKey, String.valueOf(count));

        return count;
    }



    // 查询用户是否喜欢该动态
    @Override
    public Boolean queryUserIsLove(Long userId, String publishId) {

        String redisKey = this.getCommentRedisKeyPrefix(publishId);
        String hashKey = this.getCommentUserLoveRedisKey(userId);
        Object value = this.redisTemplate.opsForHash().get(redisKey, hashKey);
        if (ObjectUtil.isNotEmpty(value)) {
            return StrUtil.equals(Convert.toStr(value), "1");
        }
        //查询mongodb
        Query query = Query.query(Criteria.where("publishId")
                .is(new ObjectId(publishId))
                .and("userId").is(userId)
                .and("commentType").is(CommentType.LOVE.getType()));
        long count = this.mongoTemplate.count(query, Comment.class);
        if (count == 0) {
            return false;
        }

        //标记用户已经喜欢
        this.redisTemplate.opsForHash().put(redisKey, hashKey, "1");

        return true;
    }




    //  ------------------  以下评论

    /**
     * 查询评论列表
     * @param publishId
     * @param page
     * @param pageSize
     * @return
     */
    @Override
    public PageInfo<Comment> queryCommentList(String publishId, Integer page, Integer pageSize) {
        PageRequest pageRequest = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Order.asc("created")));

        Query query = new Query(Criteria
                .where("publishId").is(new ObjectId(publishId))
                .and("commentType").is(CommentType.COMMENT.getType())).with(pageRequest);

        //查询评论列表
        List<Comment> commentList = this.mongoTemplate.find(query, Comment.class);

        PageInfo<Comment> pageInfo = new PageInfo<>();
        pageInfo.setPageNum(page);
        pageInfo.setPageSize(pageSize);
        pageInfo.setRecords(commentList);
        return pageInfo;
    }


    /**
     * 发表评论
     * @param userId
     * @param publishId
     * @param content
     * @return
     */
    @Override
    public Boolean saveComment(Long userId, String publishId, String content) {
        return this.saveComment(userId, publishId, CommentType.COMMENT, content);
    }


    /**
     * 查询评论数
     * @param publishId
     * @return
     */
    @Override
    public Long queryCommentCount(String publishId) {
        return this.queryCommentCount(publishId, CommentType.COMMENT);
    }

}

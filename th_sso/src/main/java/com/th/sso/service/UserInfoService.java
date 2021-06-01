package com.th.sso.service;

/*
 * @author Lise
 * @date 2021年05月22日 13:33
 * @program: tanhua
 * @description:
 */


import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.th.common.enums.SexEnum;
import com.th.common.mapper.UserInfoMapper;
import com.th.common.pojo.UserInfo;
import com.th.common.service.PicUploadService;
import com.th.common.utils.AppJwtUtil;
import com.th.common.vo.PicUploadResult;
import io.jsonwebtoken.Claims;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.util.Map;

@Service
public class UserInfoService  extends ServiceImpl<UserInfoMapper, UserInfo> {


    @Autowired
    private FaceEngineService faceEngineService;

    @Autowired
    private PicUploadService picUploadService;

    // 注册资料完善
    public Boolean saveUserInfo(Map<String, String> param, String token) {

        //1。检查token是否有效
        Claims claims = AppJwtUtil.getClaimsBody(token);
        int count = AppJwtUtil.verifyToken(claims);
        if(count==1 || count==2){
            //表示过期了
            return false;
        }if(count==-1 || count==0){
            //有效期
            UserInfo userInfo=new UserInfo();
            //参数判断
            if(param!=null && param.size()>0){
                //获取用户id
                Integer id = (Integer) claims.get("id");
                userInfo.setUserId(Long.valueOf(id));
                userInfo.setSex(StringUtils.equalsIgnoreCase(param.get("gender"),"man")? SexEnum.MAN:SexEnum.WOMAN);
                userInfo.setNickName(param.get("nickname"));
                userInfo.setBirthday(param.get("birthday"));
                userInfo.setCity(param.get("city"));

                //新增执行 ，save方法是Iservice中的方法
                boolean flag = save(userInfo);
                return flag;

            }
        }
        return false;
    }



    // 头像检验和上传
    public Boolean saveUserLogo(MultipartFile file, String token) {

        //1。检查token是否有效
        Claims claims = AppJwtUtil.getClaimsBody(token);
        int count = AppJwtUtil.verifyToken(claims);
        if(count==1 || count==2){
            //表示过期了
            return false;
        }if(count==-1 || count==0){
            //有效期,
            // 检测图片是否是人像
            try {
                boolean imgFlag = faceEngineService.checkIsPortrait(file.getBytes());
                if(!imgFlag){
                    return false;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            // 然后上传文件到文件服务器
            PicUploadResult result = this.picUploadService.upload(file);
            //如果为空，则表示上传失败
            if(StringUtils.isEmpty(result.getName())){
                return false;
            }
            //否则，把头像保存到用户信息表中
            UserInfo userInfo=new UserInfo();
            userInfo.setLogo(result.getName());
            //update方法也是使用的IService接口中的方法
            boolean flag = update(Wrappers.<UserInfo>lambdaUpdate().
                    set(UserInfo::getLogo, result.getName()).
                    eq(UserInfo::getUserId, claims.get("id")));
            return flag;
        }
        return false;
    }
}

package com.th.server.interceptor;

import cn.hutool.core.util.StrUtil;
import com.th.common.pojo.User;
import com.th.common.utils.AppJwtUtil;
import com.th.common.utils.NoAuthorization;
import com.th.common.utils.UserThreadLocal;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/*
 * @author Lise
 * @date 2021年05月26日 22:51
 * @program: th
 * @description:
 */
@Component
public class UserTokenInterceptor implements HandlerInterceptor {


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //校验handler是否是HandlerMethod
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        //判断是否包含@NoAuthorization注解，如果包含，直接放行
        if (((HandlerMethod) handler).hasMethodAnnotation(NoAuthorization.class)) {
            return true;
        }

        //从请求头中获取token
        String token = request.getHeader("Authorization");
        if(StrUtil.isNotEmpty(token)){
            Claims claims = AppJwtUtil.getClaimsBody(token);
            int count = AppJwtUtil.verifyToken(claims);
            if(count==-1 || count==0){
                //token有效
                User user=new User();
                user.setId(Long.parseLong(claims.get("id")+""));
                //将User对象放入到ThreadLocal中
                UserThreadLocal.set(user);
                return true;
            }
        }

        //token无效，响应状态为401
        response.setStatus(401); //无权限
        return false;
    }


    /**
     * 完成之后执行
     * @param request
     * @param response
     * @param handler
     * @param ex
     * @throws Exception
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserThreadLocal.remove();
    }
}

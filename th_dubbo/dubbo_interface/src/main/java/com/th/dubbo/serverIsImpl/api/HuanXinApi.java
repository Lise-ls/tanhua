package com.th.dubbo.serverIsImpl.api;


import com.th.dubbo.serverIsImpl.pojo.HuanXinUser;

/**
 * 与环信平台集成的相关操作
 */
public interface HuanXinApi {

    /**
     * 获取环信token（获取管理员权限）
     * 参见：http://docs-im.easemob.com/im/server/ready/user#%E8%8E%B7%E5%8F%96%E7%AE%A1%E7%90%86%E5%91%98%E6%9D%83%E9%99%90
     *
     * @return
     */
    String getToken();


    /**
     * @param userId
     * @return
     */

    Boolean register(Long userId);

    /**
     * 根据用户Id询环信账户信息
     * @param userId
     * @return
     */
    HuanXinUser queryHuanXinUser(Long userId);
}

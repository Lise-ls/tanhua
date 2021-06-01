package com.th.common.enums;

import com.baomidou.mybatisplus.core.enums.IEnum;

public enum SexEnum implements IEnum<Integer> {

    MAN(1,"男"),
    WOMAN(2,"女"),
    UNKNOWN(3,"未知");

    // 声明两个变量 value desc 分别表示状态和对应的值
    private int value;
    private String desc;

    // 带参构造
    SexEnum(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    // 得到value
    @Override
    public Integer getValue() {
        return this.value;
    }

    // 返回状态对应的性别
    @Override
    public String toString() {
       return this.desc;
    }
}

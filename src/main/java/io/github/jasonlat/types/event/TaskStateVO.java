package io.github.jasonlat.types.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: jia-qiang ljq1024.cc
 * @desc: 任务状态值对象
 * @Date: 2024-06-20-16:59
 */
@Getter
@AllArgsConstructor
public enum TaskStateVO {

    create("create", "创建"),
    complete("complete", "发送完成"),
    fail("fail", "发送失败"),
    ;

    private final String code;
    private final String desc;

}
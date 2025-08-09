package io.github.jasonlat.types.event;

import io.github.jasonlat.types.snow.SnowflakeIdGenerator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @Author: jia-qiang ljq1024.cc
 * @desc: 基础事件 -》 mq事件等
 * @Date: 2024-04-24-19:32
 */
@Data
public abstract class BaseEvent<T> {

    @Resource
    protected SnowflakeIdGenerator snowflakeIdGenerator;

    public abstract EventMessage<T> buildEventMessage(T data);

    public abstract String topic();

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class EventMessage<T> {
        private String id;
        private Date timestamp;
        private T data;
    }
}

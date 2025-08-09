package io.github.jasonlat.infrastructure.event;

import com.alibaba.fastjson.JSON;
import io.github.jasonlat.types.event.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@AllArgsConstructor
public class EventPublisher {

    private RabbitTemplate rabbitTemplate;

    public void publish(String topic, BaseEvent.EventMessage<?> eventMessage) {
        try {
            String messageJson = JSON.toJSONString(eventMessage);
            rabbitTemplate.convertAndSend(topic, messageJson);
            log.info("发送MQ消息 topic:{} message:{}", topic, messageJson);
        } catch (Exception e) {
            log.error("发送MQ消息失败 topic:{} message:{}", topic, JSON.toJSONString(eventMessage), e);
            throw e;
        }
    }

    public void publish(String topic, String eventMessageJSON){
        try {
            rabbitTemplate.convertAndSend(topic, eventMessageJSON);
            log.info("发送MQ消息- topic:{} message:{}", topic, eventMessageJSON);
        } catch (Exception e) {
            log.error("发送MQ消息失败- topic:{} message:{}", topic, eventMessageJSON, e);
            throw e;
        }
    }

}
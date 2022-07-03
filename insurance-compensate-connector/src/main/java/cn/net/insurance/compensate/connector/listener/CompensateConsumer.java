package cn.net.insurance.compensate.connector.listener;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RocketMQMessageListener(topic = "${rocket.topic.insurance-compensate}",consumerGroup = "${rocket.group.insurance-compensate}")
public class CompensateConsumer implements RocketMQListener<String> {


    @Override
    public void onMessage(String message) {
        log.info("[RocketMq]开始消费: {}", message);
        log.info("执行业务操作......");
    }
}

package com.mahesh.notificationservice.redis;

import com.mahesh.notificationservice.dto.EventRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class IdempotencyService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String PREFIX = "idempotency:";
    private static final Long TTL_HOURS = 24L;

    public Boolean isAlreadyProcessed(EventRequest eventRequest){
        String key = PREFIX + eventRequest.getOrderId() + ":" + eventRequest.getOrderStatus();
        return redisTemplate.hasKey(key);
    }

    public void markAsProcessed(EventRequest eventRequest){
        String key = PREFIX + eventRequest.getOrderId() + ":" + eventRequest.getOrderStatus();
        redisTemplate.opsForValue()
                .set(key, "Processed", TTL_HOURS, TimeUnit.HOURS);

        log.info("Marked event processed in redis for userId : {} | key : {}", eventRequest.getUserId(), key);
    }

}





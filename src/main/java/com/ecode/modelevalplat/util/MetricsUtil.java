package com.ecode.modelevalplat.util;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MetricsUtil {

    private final MeterRegistry meterRegistry;

    /**
     * 记录成功操作的耗时
     * @param operation 操作名称
     * @param duration 耗时（毫秒）
     */
    public void recordSuccess(String operation, long duration) {
        try {
            Timer.Sample sample = Timer.start(meterRegistry);
            sample.stop(Timer.builder("operation.duration")
                    .tag("operation", operation)
                    .tag("result", "success")
                    .register(meterRegistry));
        } catch (Exception e) {
            log.warn("记录监控指标失败", e);
        }
    }

    /**
     * 记录失败操作的耗时
     * @param operation 操作名称
     * @param duration 耗时（毫秒）
     */
    public void recordFailure(String operation, long duration) {
        try {
            Timer.Sample sample = Timer.start(meterRegistry);
            sample.stop(Timer.builder("operation.duration")
                    .tag("operation", operation)
                    .tag("result", "failure")
                    .register(meterRegistry));
        } catch (Exception e) {
            log.warn("记录监控指标失败", e);
        }
    }

    /**
     * 记录计数指标
     * @param name 指标名称
     * @param tags 标签
     */
    public void incrementCounter(String name, String... tags) {
        try {
            meterRegistry.counter(name, tags).increment();
        } catch (Exception e) {
            log.warn("增加计数器失败", e);
        }
    }
}
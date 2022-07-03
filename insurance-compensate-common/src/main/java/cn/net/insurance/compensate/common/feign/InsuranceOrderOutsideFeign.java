package cn.net.insurance.compensate.common.feign;

import cn.net.insurance.core.base.model.RespResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@Component
@FeignClient(name = "insurance-compensate-outside-connector")
public interface InsuranceOrderOutsideFeign {
    /**
     * 同步理赔状态
     *
     * @param params
     * @return
     */
    @PostMapping(value = "/outside/invokerOrder/order/syncStatus")
    RespResult<Void> syncStatus(@RequestBody Map params);
}

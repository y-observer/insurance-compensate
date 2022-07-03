package cn.net.insurance.compensate.common.feign;

import cn.net.insurance.core.base.model.RespResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@Component
@FeignClient(name = "insurance-compensate-connector")
public interface InsuranceCompensateFeign {
    /**
     * 查询字典
     *
     * @param params
     * @return
     */
    @PostMapping(value = "/compensate/dictionary/list-page")
    RespResult listPage(@RequestBody Map params);
}

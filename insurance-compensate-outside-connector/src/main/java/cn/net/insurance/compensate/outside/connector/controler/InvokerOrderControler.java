package cn.net.insurance.compensate.outside.connector.controler;

import cn.net.insurance.core.base.model.RespResult;
import com.alibaba.nacos.common.utils.JacksonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/invokerOrder")
public class InvokerOrderControler {


    /**
     *
     * @param params
     * @return
     */
    @PostMapping("/order/syncStatus")
    public RespResult<Void> syncStatus(@RequestBody Map params) {
        System.out.println("[feign]调用理赔服务...." + JacksonUtils.toJson(params));
        //RespResult<Void> respResult = ServiceExchangeUtils.httpPostJson2ServiceOnMap(ServerUrlConstants.ENABLE_IDENTITY, params, Void.class);
        return null;
    }
}

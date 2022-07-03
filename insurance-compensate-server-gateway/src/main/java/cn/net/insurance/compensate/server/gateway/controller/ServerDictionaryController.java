package cn.net.insurance.compensate.server.gateway.controller;

import cn.net.insurance.compensate.common.feign.InsuranceCompensateFeign;
import cn.net.insurance.compensate.common.feign.InsuranceOrderOutsideFeign;
import cn.net.insurance.compensate.common.req.DictionaryReqDto;
import cn.net.insurance.core.base.model.RespResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/dictionary")
public class ServerDictionaryController {
    @Autowired
    private InsuranceCompensateFeign insuranceCompensateFeign;

    /**
     *
     * @param map
     * @return
     */
    @PostMapping("/list-page")
    public RespResult listPage(@Valid @RequestBody Map map){
        System.out.println("进入server....");
        RespResult respResult = insuranceCompensateFeign.listPage(map);
        return respResult;
    }
}

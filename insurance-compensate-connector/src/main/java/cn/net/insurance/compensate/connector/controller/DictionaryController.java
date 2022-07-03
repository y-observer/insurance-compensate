package cn.net.insurance.compensate.connector.controller;

import cn.net.insurance.compensate.entity.Dictionary;
import cn.net.insurance.compensate.connector.service.DictionaryService;
import cn.net.insurance.core.base.model.RespResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/dictionary")
@RestController
public class DictionaryController {

    @Autowired
    private DictionaryService dictionaryService;

    @PostMapping("/list-page")
    public RespResult listPage() {
        List<Dictionary> dictionaries = dictionaryService.queryDictionaryByTypeCode("company_type");
        System.out.println("[dictionary]业务实现---->" + dictionaries.toString());
        return RespResult.success(dictionaries);
    }
}

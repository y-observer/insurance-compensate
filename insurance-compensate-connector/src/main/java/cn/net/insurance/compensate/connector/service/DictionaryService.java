package cn.net.insurance.compensate.connector.service;


import cn.net.insurance.compensate.entity.Dictionary;

import java.util.List;

public interface DictionaryService {

    /**
     * 根据类型编码查询数据字典
     * @param typeCode
     * @return
     */
    List<Dictionary> queryDictionaryByTypeCode(String typeCode);
}

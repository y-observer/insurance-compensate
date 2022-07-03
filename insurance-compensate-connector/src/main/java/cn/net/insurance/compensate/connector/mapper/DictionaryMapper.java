package cn.net.insurance.compensate.connector.mapper;

import cn.net.insurance.compensate.entity.Dictionary;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface DictionaryMapper extends BaseMapper<Dictionary> {

    /**
     * 根据类型编码查询数据字典
     *
     * @param typeCode
     * @return
     */
    List<Dictionary> queryDictionaryByTypeCode(@Param("typeCode") String typeCode);
}

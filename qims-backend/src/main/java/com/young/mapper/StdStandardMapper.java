package com.young.mapper;

import com.young.pojo.StdStandard;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface StdStandardMapper {
    int insert(StdStandard record);
    int update(StdStandard record);
    int deleteById(Long id);
    StdStandard selectById(Long id);
    List<StdStandard> selectAll();
}

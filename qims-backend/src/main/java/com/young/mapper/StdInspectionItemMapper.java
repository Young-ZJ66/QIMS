package com.young.mapper;

import com.young.pojo.StdInspectionItem;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface StdInspectionItemMapper {
    int insert(StdInspectionItem record);
    int update(StdInspectionItem record);
    int deleteById(Long id);
    StdInspectionItem selectById(Long id);
    List<StdInspectionItem> selectAll();
}

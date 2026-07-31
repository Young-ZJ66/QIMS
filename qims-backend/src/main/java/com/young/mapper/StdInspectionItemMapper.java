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
    /** 按标准ID查询检测项目 */
    List<StdInspectionItem> selectByStandardId(@org.apache.ibatis.annotations.Param("standardId") Long standardId);
    /** 批量查询 */
    List<StdInspectionItem> selectByIds(@org.apache.ibatis.annotations.Param("ids") List<Long> ids);
}

package com.young.mapper;

import com.young.pojo.BizInspectionRecord;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface BizInspectionRecordMapper {
    int insert(BizInspectionRecord record);
    int update(BizInspectionRecord record);
    int deleteById(Long id);
    BizInspectionRecord selectById(Long id);
    List<BizInspectionRecord> selectAll();
}

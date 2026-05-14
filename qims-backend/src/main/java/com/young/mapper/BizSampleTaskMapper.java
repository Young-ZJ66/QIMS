package com.young.mapper;

import com.young.pojo.BizSampleTask;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface BizSampleTaskMapper {
    int insert(BizSampleTask record);
    int update(BizSampleTask record);
    int deleteById(Long id);
    BizSampleTask selectById(Long id);
    List<BizSampleTask> selectAll();
}

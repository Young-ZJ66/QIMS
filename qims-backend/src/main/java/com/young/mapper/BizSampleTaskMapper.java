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
    /** 按委托单ID查询盲样任务 */
    List<BizSampleTask> selectByDelegationId(@org.apache.ibatis.annotations.Param("delegationId") Long delegationId);
    /** 按检测员ID查询分配的任务 */
    List<BizSampleTask> selectByInspectorId(@org.apache.ibatis.annotations.Param("inspectorId") Long inspectorId);
}

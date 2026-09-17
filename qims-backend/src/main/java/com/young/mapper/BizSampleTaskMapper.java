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
    /** 按委托单ID列表批量查询 */
    List<BizSampleTask> selectByDelegationIds(@org.apache.ibatis.annotations.Param("delegationIds") List<Long> delegationIds);
    /** 按状态统计任务数 */
    int countByStatus(@org.apache.ibatis.annotations.Param("status") Integer status);
    /** 按检测员ID统计已完成任务数 */
    int countCompletedByInspectorId(@org.apache.ibatis.annotations.Param("inspectorId") Long inspectorId);
}

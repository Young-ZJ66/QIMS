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
    /** 按任务ID查询检测记录 */
    List<BizInspectionRecord> selectByTaskId(@org.apache.ibatis.annotations.Param("taskId") Long taskId);
    /** 按任务ID列表批量查询检测记录 */
    List<BizInspectionRecord> selectByTaskIds(@org.apache.ibatis.annotations.Param("taskIds") List<Long> taskIds);
    /** 按状态统计不合格记录数（按检验项目分组） */
    List<java.util.Map<String, Object>> countFailedByItemName();
}

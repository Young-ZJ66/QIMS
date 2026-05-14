package com.young.service;

import com.young.pojo.BizInspectionRecord;
import java.util.List;

public interface BizInspectionRecordService {
    int add(BizInspectionRecord record);
    int update(BizInspectionRecord record);
    int delete(Long id);
    BizInspectionRecord getById(Long id);
    List<BizInspectionRecord> getAll();

    /**
     * 检测员提交实测数据，系统自动判定合格状态
     */
    void submitInspectionData(BizInspectionRecord record);
}

package com.young.service;

import com.young.pojo.BizSampleTask;
import java.util.List;

public interface BizSampleTaskService {
    int add(BizSampleTask record);
    int update(BizSampleTask record);
    int delete(Long id);
    BizSampleTask getById(Long id);
    List<BizSampleTask> getAll();
}

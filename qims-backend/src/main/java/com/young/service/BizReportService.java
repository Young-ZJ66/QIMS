package com.young.service;

import com.young.pojo.BizReport;
import java.util.List;

public interface BizReportService {
    int add(BizReport record);
    int update(BizReport record);
    int delete(Long id);
    BizReport getById(Long id);
    List<BizReport> getAll();
}

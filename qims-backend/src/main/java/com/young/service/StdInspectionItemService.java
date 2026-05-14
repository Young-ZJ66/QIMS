package com.young.service;

import com.young.pojo.StdInspectionItem;
import java.util.List;

public interface StdInspectionItemService {
    int add(StdInspectionItem record);
    int update(StdInspectionItem record);
    int delete(Long id);
    StdInspectionItem getById(Long id);
    List<StdInspectionItem> getAll();
}

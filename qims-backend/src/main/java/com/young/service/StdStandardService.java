package com.young.service;

import com.young.pojo.StdStandard;
import java.util.List;

public interface StdStandardService {
    int add(StdStandard record);
    int update(StdStandard record);
    int delete(Long id);
    StdStandard getById(Long id);
    List<StdStandard> getAll();
}

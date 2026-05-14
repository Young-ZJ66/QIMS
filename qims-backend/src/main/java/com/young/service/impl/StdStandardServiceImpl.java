package com.young.service.impl;

import com.young.pojo.StdStandard;
import com.young.mapper.StdStandardMapper;
import com.young.service.StdStandardService;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

@Service
public class StdStandardServiceImpl implements StdStandardService {

    @Autowired
    private StdStandardMapper mapper;

    @Override
    public int add(StdStandard record) {
        return mapper.insert(record);
    }

    @Override
    public int update(StdStandard record) {
        return mapper.update(record);
    }

    @Override
    public int delete(Long id) {
        return mapper.deleteById(id);
    }

    @Override
    public StdStandard getById(Long id) {
        return mapper.selectById(id);
    }

    @Override
    public List<StdStandard> getAll() {
        return mapper.selectAll();
    }
}

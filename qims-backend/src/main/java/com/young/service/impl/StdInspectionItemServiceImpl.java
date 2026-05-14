package com.young.service.impl;

import com.young.pojo.StdInspectionItem;
import com.young.mapper.StdInspectionItemMapper;
import com.young.service.StdInspectionItemService;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

@Service
public class StdInspectionItemServiceImpl implements StdInspectionItemService {

    @Autowired
    private StdInspectionItemMapper mapper;

    @Override
    public int add(StdInspectionItem record) {
        return mapper.insert(record);
    }

    @Override
    public int update(StdInspectionItem record) {
        return mapper.update(record);
    }

    @Override
    public int delete(Long id) {
        return mapper.deleteById(id);
    }

    @Override
    public StdInspectionItem getById(Long id) {
        return mapper.selectById(id);
    }

    @Override
    public List<StdInspectionItem> getAll() {
        return mapper.selectAll();
    }
}

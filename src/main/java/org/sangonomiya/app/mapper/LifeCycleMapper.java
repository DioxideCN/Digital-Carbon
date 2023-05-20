package org.sangonomiya.app.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.sangonomiya.app.entity.LifeCycle;

import java.util.Map;

@Mapper
public interface LifeCycleMapper {
    void createLifeCycleTableAction(String companyId);

    void dropLifeCycleTableAction(String companyId);

    void insertLifeCycle(String companyId, LifeCycle lifeCycle);

    int deleteLifeCycleById(String companyId, int lifeCycleId);

    LifeCycle selectLifeCycleById(String companyId, int lifeCycleId);

    int updateById(String companyId, int lifeCycleId, LifeCycle lifeCycle);

    Map<Object, Object> checkTableExists(String companyId);
}

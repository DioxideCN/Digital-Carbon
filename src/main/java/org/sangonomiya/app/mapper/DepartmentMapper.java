package org.sangonomiya.app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.sangonomiya.app.entity.Company;
import org.sangonomiya.app.entity.Department;

/**
 * 部门Mapper在数据库中映射repo_department表并以Department对象的形式进行传输
 * @author Dioxide.CN
 * @date 2023/2/28 19:01
 * @since 1.0
 */
@Mapper
public interface DepartmentMapper extends BaseMapper<Department> {
}

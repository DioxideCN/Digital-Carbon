package org.sangonomiya.app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.sangonomiya.app.entity.Approval;
import org.sangonomiya.app.entity.Company;

/**
 * 公司Mapper在数据库中映射repo_approval表并以Approval对象的形式进行传输
 * @author Dioxide.CN
 * @date 2023/4/8 9:13:52
 * @since 1.0
 */
@Mapper
public interface ApprovalMapper extends BaseMapper<Approval> {
}

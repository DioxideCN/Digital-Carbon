package org.sangonomiya.app.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * @author Dioxide.CN
 * @date 2023/4/14 20:38
 * @since 1.0
 */
@Mapper
public interface GenerateMapper {

    /**
     * 创建当月的产品数据表
     * @param year 年份
     * @param month 月份
     */
    void generateCurrentTable(String year, String month);

    Map<String, Double> getDataStatistic(String year, String month, int company_id, String app_id, int product_id);

    void updateDataStatistic(String year, String month, String day, int company_id, String app_id, double new_data);

}

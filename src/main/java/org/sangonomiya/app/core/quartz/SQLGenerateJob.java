package org.sangonomiya.app.core.quartz;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.RowBounds;
import org.jetbrains.annotations.NotNull;
import org.quartz.JobExecutionContext;
import org.sangonomiya.app.core.NotificationHelper;
import org.sangonomiya.app.core.message.MessageAction;
import org.sangonomiya.app.core.message.MessageBuilder;
import org.sangonomiya.app.core.message.MessageType;
import org.sangonomiya.app.entity.Company;
import org.sangonomiya.app.entity.Payment;
import org.sangonomiya.app.entity.Permission;
import org.sangonomiya.app.entity.UserVO;
import org.sangonomiya.app.mapper.*;
import org.sangonomiya.app.service.ICompanyService;
import org.sangonomiya.app.service.INotificationService;
import org.sangonomiya.app.service.IProductService;
import org.sangonomiya.app.service.IUserService;
import org.sangonomiya.groovy.DateHandler;
import org.sangonomiya.kotlin.Pair;
import org.springframework.scheduling.quartz.QuartzJobBean;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.TemporalAdjusters;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * CleanCompanyJob任务在每天凌晨00:00的时候会检索过期和即将过期的企业账户，对于即将过期的企业账户所有者进行续费通知操作、对于已过期但未超过7天的企业账户所有者进行数据保留和续费通知，对于已经过期7天的企业账户进行数据删除操作和删除通知。
 * @author Dioxide.CN
 * @date 2023/4/1 15:51
 * @since 1.0
 */
@Slf4j
public class SQLGenerateJob extends QuartzJobBean{

    @Resource
    private GenerateMapper generateMapper;

    /**
     * 每分钟都会执行一次当月的产品统计数据表的创建（若不存在）
     * 如果是月末则会同时创建下一个月的
     */
    @Override
    public void executeInternal(@NotNull JobExecutionContext context) {
        Calendar calendar = Calendar.getInstance();
        LocalDate today = LocalDate.now();
        LocalDate lastDayOfMonth = today.with(TemporalAdjusters.lastDayOfMonth());

        SimpleDateFormat year = new SimpleDateFormat("yyyy");
        SimpleDateFormat month = new SimpleDateFormat("MM");

        String yearStr = year.format(calendar.getTime());
        String monthStr = month.format(calendar.getTime());
        generateMapper.generateCurrentTable(yearStr, monthStr);

        if (Period.between(today, lastDayOfMonth).getDays() == 0) {
            // 最后一天还需要创建下一个月的表
            calendar.add(Calendar.MONTH, 1);
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
            yearStr = year.format(calendar.getTime());
            monthStr = month.format(calendar.getTime());
            generateMapper.generateCurrentTable(yearStr, monthStr);
        }
    }

}

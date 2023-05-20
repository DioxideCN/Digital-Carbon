package org.sangonomiya.app.entity;

import com.deepoove.poi.data.ChartSingleSeriesRenderData;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@ApiModel(value = "Word数据", description = "Word生成所需要的数据")
public class WordData {

    @ApiModelProperty("产品对象")
    private Product product;

    @ApiModelProperty("公司对象")
    private Company company;

    @ApiModelProperty("排放数据")
    private EmissionData emissionData;

    @ApiModelProperty("企业注册人信息")
    private UserVO owner;

    @ApiModelProperty("格式化的工序信息")
    private List<ProcessData> processData;

    @ApiModelProperty("图表数据")
    private ChartsData chartsData;

    @ApiModelProperty("报告创建日期")
    private Date createDate = new Date(System.currentTimeMillis());

    /**
     * 定义排放数据类型
     */
    @Data
    public static class EmissionData{
        private Integer total;
        private List<Map<String,String>> sortByCategory;
        private List<Map<String,String>> sortByName;
        private List<Map<String,String>> sortByProcess;
        private List<Map<String,String>> sortByStage;
    }

    /**
     * 定义所有的工序数据
     */
    @Data
    public static class ProcessData{
        private String name;
        private String product;
        private String stage;
        private List<NodeData> inputs;
        private List<NodeData> outputs;
    }

    /**
     * 定义工序中输入输出数据
     */
    @Data
    public static class NodeData{
        private String name;
        private String unit;
        private String category;
        private String impactFactor;
        private int amount;
        private double weight;
    }

    @Data
    public static class ChartsData{
        ChartSingleSeriesRenderData sortByCategory;
        ChartSingleSeriesRenderData sortByName;
        ChartSingleSeriesRenderData sortByProcess;
        ChartSingleSeriesRenderData sortByStage;
    }

}

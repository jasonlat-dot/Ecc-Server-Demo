package io.github.jasonlat.trigger.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.util.Arrays;
import java.util.List;

@Data
public class CourseConfigRequest {
    
    /**
     * 配置名称
     */
    @NotBlank(message = "配置名称不能为空")
    @Size(max = 100, message = "配置名称长度不能超过100个字符")
    private String configName;

    /**
     * 学年（格式：2023-2024）
     */
    @NotBlank(message = "学年不能为空")
    @Pattern(regexp = "^\\d{4}-\\d{4}$", message = "学年格式必须为YYYY-YYYY")
    private String academicYear;

    /**
     * 学期（格式：spring、autumn、summer）
     */
    @NotBlank(message = "学期不能为空")
    @Pattern(regexp = "^(spring|autumn|summer)$", message = "学期格式必须为spring、autumn或summer")
    private String semester;


    /**
     * 第一周日期（格式：YYYY-MM-DD）
     */
    @NotBlank(message = "第一周日期不能为空")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "第一周日期格式必须为YYYY-MM-DD")
    private String firstWeekDate;
    
    /**
     * 节次设置列表
     */
    @Valid
    @NotEmpty(message = "节次设置不能为空")
    private List<PeriodRequest> periods;

    /**
     * 课程列表
     */
    @Valid
    private List<CourseRequest> courses;

    /**
     * 推送设置
     */
    @Valid
    @NotNull(message = "推送设置不能为空")
    private PushRequest pushSettings;

    
    /**
     * 节次设置内部类
     */
    @Data
    public static class PeriodRequest {
        
        /**
         * 节次值（唯一标识）
         */
        @NotNull(message = "节次值不能为空")
        @Min(value = 1, message = "节次值必须大于0")
        private Integer value;
        
        /**
         * 节次名称
         */
        @NotBlank(message = "节次名称不能为空")
        @Size(max = 50, message = "节次名称长度不能超过50个字符")
        private String name;
        
        /**
         * 时间段: 08:00-09:50
         */
        @NotBlank(message = "时间段不能为空")
        @Pattern(regexp = "^\\d{2}:\\d{2}-\\d{2}:\\d{2}$", message = "时间格式必须为HH:mm-HH:mm")
        private String time;
    }
    
    /**
     * 推送设置请求类
     */
    @Data
    public static class PushRequest {

        /**
         * 是否启用推送
         */
        @NotNull(message = "推送启用状态不能为空")
        private Boolean enabled = false;

        /**
         * 每日推送设置
         */
        @Valid
        @NotNull(message = "每日推送设置不能为空")
        private DailyPushRequest dailyPush = new DailyPushRequest();

        /**
         * 课程推送设置
         */
        @Valid
        @NotNull(message = "课程推送设置不能为空")
        private ClassPushRequest classPush = new ClassPushRequest();

        /**
         * 推送方式列表
         */
        @NotEmpty(message = "推送方式不能为空")
        private List<@NotBlank String> methods = Arrays.asList("email",  "weixin");

        /**
         * 每日推送设置内部类
         */
        @Data
        public static class DailyPushRequest {

            /**
             * 是否启用每日推送
             */
            @NotNull(message = "每日推送启用状态不能为空")
            private Boolean enabled = false;

            /**
             * 每日推送时间
             */
            @NotBlank(message = "每日推送时间不能为空")
            @Pattern(regexp = "^\\d{2}:\\d{2}$", message = "时间格式必须为HH:mm")
            private String time = "07:00";
        }

        /**
         * 课程推送设置内部类
         */
        @Data
        public static class ClassPushRequest {

            /**
             * 是否启用课程推送
             */
            @NotNull(message = "课程推送启用状态不能为空")
            private Boolean enabled = false;

            /**
             * 提前推送时间（分钟）
             */
            @NotNull(message = "提前推送时间不能为空")
            @Min(value = 0, message = "提前推送时间不能为负数")
            @Max(value = 120, message = "提前推送时间不能超过120分钟")
            private Integer advanceTime = 30;
        }
    }
    

    /**
     * 课程设置内部类
     */
    @Data
    public static class CourseRequest {

        /**
         * 课程ID（用于导入导出时的唯一标识）
         */
        @Size(max = 50, message = "课程ID长度不能超过50个字符")
        private String courseId;

        /**
         * 课程名称
         */
        @NotBlank(message = "课程名称不能为空")
        @Size(max = 100, message = "课程名称长度不能超过100个字符")
        private String name;

        /**
         * 任课教师
         */
        @Size(max = 50, message = "教师姓名长度不能超过50个字符")
        private String teacher;

        /**
         * 上课地点
         */
        @NotBlank(message = "上课地点不能为空")
        private String classroom;

        /**
         * 星期几（1-7，1为周一）
         */
        @NotNull(message = "星期不能为空")
        @Min(value = 1, message = "星期值必须在1-7之间")
        @Max(value = 7, message = "星期值必须在1-7之间")
        @JsonProperty("dayOfWeek")
        private Integer dayOfWeek;
        
        /**
         * 节次值
         */
        @NotNull(message = "节次不能为空")
        @Min(value = 1, message = "节次值必须大于0")
        private Integer period;

        /**
         * 课程周数（逗号分隔的字符串，表示在哪些周上课，如"1,2,3,5-10"）
         */
        @NotBlank(message = "课程周数不能为空")
        @Pattern(regexp = "^(\\d+(-\\d+)?(,\\d+(-\\d+)?)*|\\d+)$", message = "课程周数格式不正确，应为逗号分隔的数字或范围")
        private String weeks;

        /**
         * 周类型（"odd"表示单周，"even"表示双周，"all"表示所有周）
         */
        @NotBlank(message = "周类型不能为空")
        @Pattern(regexp = "^(odd|even|all)$", message = "周类型格式必须为odd、even或all")
        private String weekType;

    }


}
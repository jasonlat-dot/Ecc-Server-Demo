package io.github.jasonlat.trigger.model;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * @author li--jiaqiang 2025−06−19
 */
@Data
public class SemesterConfig {

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
}
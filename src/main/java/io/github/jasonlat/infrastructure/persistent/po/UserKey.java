package io.github.jasonlat.infrastructure.persistent.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 
 * </p>
 *
 * @author lijiaqiang@ljq1024.cc
 * @since 2025-08-09
 */
@Getter
@Setter
@TableName("user_key")
public class UserKey implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 用户名-》映射email
     */
    @TableField("username")
    private String username;

    /**
     * 用户公钥X坐标
     */
    @TableField("publicX")
    private String publicX;

    /**
     * 用户公钥Y坐标
     */
    @TableField("publicY")
    private String publicY;

    /**
     * 状态（ACTIVE-激活，INACTIVE-未激活，DISABLED-禁用）
     */
    @TableField("status")
    private String status;


}

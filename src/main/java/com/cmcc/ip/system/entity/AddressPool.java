package com.cmcc.ip.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.util.Date;

@Data
@TableName("tb_address_pool")
public class AddressPool {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String poolName;
    
    private String poolCode;
    
    private Integer ipType;
    
    private String startAddress;
    
    private String endAddress;
    
    private Integer totalCount;
    
    private Integer availableCount;
    
    private String regionCode;
    
    private Integer allocationStrategy;
    
    private Integer priority;
    
    private Integer status;
    
    @TableField(fill = FieldFill.INSERT)
    private Date createdTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updatedTime;
    
    public enum AllocationStrategy {
        SEQUENTIAL(1),
        RANDOM(2);
        
        private final Integer value;
        
        AllocationStrategy(Integer value) {
            this.value = value;
        }
        
        public Integer getValue() {
            return value;
        }
    }
    
    public enum PoolStatus {
        ENABLED(1),
        DISABLED(2);
        
        private final Integer value;
        
        PoolStatus(Integer value) {
            this.value = value;
        }
        
        public Integer getValue() {
            return value;
        }
    }
}
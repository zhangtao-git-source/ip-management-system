package com.cmcc.ip.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.util.Date;

@Data
@TableName("tb_ip_allocation")
public class IpAllocation {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long ipAddressId;
    
    private String ipAddress;
    
    private Long addressPoolId;
    
    private Integer allocationType;
    
    private String subscriberId;
    
    private String serviceOrderId;
    
    private Date allocationTime;
    
    private Date expirationTime;
    
    private Date releaseTime;
    
    private Integer releaseType;
    
    private Integer status;
    
    private String operatorId;
    
    @TableField(fill = FieldFill.INSERT)
    private Date createdTime;
    
    public enum AllocationType {
        AUTO(1),
        MANUAL(2);
        
        private final Integer value;
        
        AllocationType(Integer value) {
            this.value = value;
        }
        
        public Integer getValue() {
            return value;
        }
    }
    
    public enum ReleaseType {
        EXPIRE(1),
        MANUAL(2);
        
        private final Integer value;
        
        ReleaseType(Integer value) {
            this.value = value;
        }
        
        public Integer getValue() {
            return value;
        }
    }
    
    public enum AllocationStatus {
        IN_USE(1),
        RELEASED(2);
        
        private final Integer value;
        
        AllocationStatus(Integer value) {
            this.value = value;
        }
        
        public Integer getValue() {
            return value;
        }
    }
}
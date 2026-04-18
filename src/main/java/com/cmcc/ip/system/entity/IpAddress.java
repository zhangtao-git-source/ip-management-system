package com.cmcc.ip.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.util.Date;

@Data
@TableName("tb_ip_address")
public class IpAddress {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String ipAddress;
    
    private Integer ipType;
    
    private String subnetMask;
    
    private Integer prefixLength;
    
    private String gateway;
    
    private String dnsPrimary;
    
    private String dnsSecondary;
    
    private Long addressPoolId;
    
    private Integer status;
    
    private String regionCode;
    
    private Long deviceId;
    
    @TableField(fill = FieldFill.INSERT)
    private Date createdTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updatedTime;
    
    public enum IpType {
        IPv4(1),
        IPv6(2);
        
        private final Integer value;
        
        IpType(Integer value) {
            this.value = value;
        }
        
        public Integer getValue() {
            return value;
        }
    }
    
    public enum Status {
        UNASSIGNED(1),
        ASSIGNED(2),
        RESERVED(3),
        FROZEN(4);
        
        private final Integer value;
        
        Status(Integer value) {
            this.value = value;
        }
        
        public Integer getValue() {
            return value;
        }
    }
}
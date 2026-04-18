package com.cmcc.ip.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.util.Date;

@Data
@TableName("tb_ip_binding")
public class IpBinding {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long ipAddressId;
    
    private String ipAddress;
    
    private Long deviceId;
    
    private Integer bindingType;
    
    private Date bindingTime;
    
    private Date unbindingTime;
    
    private Integer status;
    
    @TableField(fill = FieldFill.INSERT)
    private Date createdTime;
    
    public enum BindingType {
        STATIC(1),
        DYNAMIC(2);
        
        private final Integer value;
        
        BindingType(Integer value) {
            this.value = value;
        }
        
        public Integer getValue() {
            return value;
        }
    }
    
    public enum BindingStatus {
        BOUND(1),
        UNBOUND(2);
        
        private final Integer value;
        
        BindingStatus(Integer value) {
            this.value = value;
        }
        
        public Integer getValue() {
            return value;
        }
    }
}
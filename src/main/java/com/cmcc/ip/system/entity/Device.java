package com.cmcc.ip.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.util.Date;

@Data
@TableName("tb_device")
public class Device {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String deviceName;
    
    private String deviceCode;
    
    private String deviceType;
    
    private String deviceIp;
    
    private String vendor;
    
    private String regionCode;
    
    private Integer status;
    
    private Date lastCollectionTime;
    
    @TableField(fill = FieldFill.INSERT)
    private Date createdTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updatedTime;
    
    public enum DeviceStatus {
        ONLINE(1),
        OFFLINE(2);
        
        private final Integer value;
        
        DeviceStatus(Integer value) {
            this.value = value;
        }
        
        public Integer getValue() {
            return value;
        }
    }
}
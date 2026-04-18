package com.cmcc.ip.system;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.cmcc.ip.system.mapper")
public class IpManagementSystemApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(IpManagementSystemApplication.class, args);
    }
}
package com.cmcc.ip.system.controller;

import com.cmcc.ip.system.common.Result;
import com.cmcc.ip.system.service.AuditService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/audit")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        Map<String, Object> data = auditService.getOverview();
        return Result.success(data);
    }

    @GetMapping("/history")
    public Result<Map<String, Object>> history(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Map<String, Object> data = auditService.getHistory(pageNum, pageSize);
        return Result.success(data);
    }

    @PostMapping("/trigger")
    public Result<String> triggerAudit() {
        log.info("手动触发稽核");
        auditService.triggerAudit();
        return Result.success("稽核任务已触发");
    }
}

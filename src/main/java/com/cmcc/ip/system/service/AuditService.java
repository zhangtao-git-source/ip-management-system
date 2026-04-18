package com.cmcc.ip.system.service;

import com.cmcc.ip.system.entity.IpAllocation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class AuditService {

    private final IpAllocationService ipAllocationService;
    private final IpAddressService ipAddressService;
    private final AddressPoolService addressPoolService;

    public AuditService(IpAllocationService ipAllocationService,
                        IpAddressService ipAddressService,
                        AddressPoolService addressPoolService) {
        this.ipAllocationService = ipAllocationService;
        this.ipAddressService = ipAddressService;
        this.addressPoolService = addressPoolService;
    }

    public Map<String, Object> getOverview() {
        Map<String, Object> data = new HashMap<>();

        Map<String, Object> overview = addressPoolService.getAuditOverview();
        data.putAll(overview);

        data.put("lastAuditTime", "2026-04-10 02:00:00");
        data.put("passRate", 98.7);
        data.put("exceptionCount", 17);

        List<Map<String, Object>> auditItems = new ArrayList<>();

        Map<String, Object> item1 = new HashMap<>();
        item1.put("name", "地址池总量与实际IP数比对");
        item1.put("total", 4);
        item1.put("normal", 4);
        item1.put("abnormal", 0);
        item1.put("passRate", "100%");
        auditItems.add(item1);

        Map<String, Object> item2 = new HashMap<>();
        item2.put("name", "已分配IP与分配记录比对");
        item2.put("total", 96384);
        item2.put("normal", 96370);
        item2.put("abnormal", 14);
        item2.put("passRate", "99.99%");
        auditItems.add(item2);

        Map<String, Object> item3 = new HashMap<>();
        item3.put("name", "到期未释放IP检测");
        item3.put("total", 342);
        item3.put("normal", 329);
        item3.put("abnormal", 13);
        item3.put("passRate", "96.2%");
        auditItems.add(item3);

        Map<String, Object> item4 = new HashMap<>();
        item4.put("name", "孤立IP检测（无地址池/无设备）");
        item4.put("total", 128512);
        item4.put("normal", 128509);
        item4.put("abnormal", 3);
        item4.put("passRate", "99.99%");
        auditItems.add(item4);

        data.put("auditItems", auditItems);
        return data;
    }

    public Map<String, Object> getHistory(Integer pageNum, Integer pageSize) {
        Map<String, Object> data = new HashMap<>();
        List<Map<String, Object>> historyList = new ArrayList<>();

        String[][] records = {
                {"2026-04-10 02:00", "success", "定时稽核完成", "生成稽核文件 AUDIT_20260410_0200.csv，已推送至资源中心SFTP"},
                {"2026-04-09 02:00", "success", "定时稽核完成", "生成稽核文件 AUDIT_20260409_0200.csv，已推送至资源中心SFTP"},
                {"2026-04-08 02:00", "fail", "SFTP推送失败", "稽核文件已生成，SFTP推送失败（连接超时），已自动重试3次，第2次重试成功"},
                {"2026-04-07 02:00", "success", "定时稽核完成", "生成稽核文件 AUDIT_20260407_0200.csv，已推送至资源中心SFTP"},
                {"2026-04-06 02:00", "success", "定时稽核完成", "生成稽核文件 AUDIT_20260406_0200.csv，已推送至资源中心SFTP"},
        };

        for (String[] r : records) {
            Map<String, Object> item = new HashMap<>();
            item.put("time", r[0]);
            item.put("status", r[1]);
            item.put("title", r[2]);
            item.put("detail", r[3]);
            historyList.add(item);
        }

        data.put("list", historyList);
        data.put("total", historyList.size());
        return data;
    }

    public void triggerAudit() {
        log.info("手动触发稽核任务");
    }
}

// ========== App Router & Navigation ==========
const TITLES = { dashboard:'系统首页', pool:'地址池管理', ipaddr:'IP地址管理', allocation:'地址分配管理', device:'设备管理', audit:'稽核管理', binding:'IP绑定管理', report:'统计报表' };
let currentPage = 'dashboard';
const loadedPages = {};

// 全局区域数据缓存
let regionData = null;
let regionMap = {};

// 加载区域数据
async function loadRegions() {
    if (regionData) return regionData;
    const res = await API.region.cities();
    regionData = res?.data || [];
    // 构建区域映射
    regionData.forEach(r => {
        regionMap[r.regionCode] = r.regionName;
    });
    return regionData;
}

// 生成区域选择框HTML
function renderRegionSelect(id, includeAll = true, selectedValue = '') {
    const options = regionData || [];
    let html = `<select id="${id}">`;
    if (includeAll) {
        html += `<option value="">全部</option>`;
    }
    options.forEach(r => {
        const selected = r.regionCode === selectedValue ? ' selected' : '';
        html += `<option value="${r.regionCode}"${selected}>${r.regionName}</option>`;
    });
    html += '</select>';
    return html;
}

function showPage(name) {
    currentPage = name;
    document.querySelectorAll('.page').forEach(p => p.classList.remove('active'));
    document.querySelectorAll('.nav-item').forEach(n => n.classList.remove('active'));
    document.getElementById('page-' + name).classList.add('active');
    document.querySelector('[data-page="'+name+'"]').classList.add('active');
    document.getElementById('page-title').textContent = TITLES[name] || '';
    if (!loadedPages[name]) { loadPage(name); loadedPages[name] = true; }
}

async function loadPage(name) {
    const el = document.getElementById('page-' + name);
    el.innerHTML = '<div class="loading">加载中...</div>';
    switch(name) {
        case 'dashboard': await renderDashboard(el); break;
        case 'pool': await renderPool(el); break;
        case 'ipaddr': await renderIpAddr(el); break;
        case 'allocation': await renderAllocation(el); break;
        case 'device': await renderDevice(el); break;
        case 'audit': await renderAudit(el); break;
        case 'binding': await renderBinding(el); break;
        case 'report': await renderReport(el); break;
    }
}

function openDialog(title, bodyHtml, footerHtml) {
    document.getElementById('dialog-title').textContent = title;
    document.getElementById('dialog-body').innerHTML = bodyHtml;
    document.getElementById('dialog-footer').innerHTML = footerHtml || '';
    document.getElementById('dialog-overlay').classList.add('show');
}
function closeDialog() { document.getElementById('dialog-overlay').classList.remove('show'); }

// Init
document.querySelectorAll('.nav-item').forEach(item => {
    item.addEventListener('click', () => showPage(item.dataset.page));
});
loadPage('dashboard'); loadedPages['dashboard'] = true;

// ========== 1. Dashboard ==========
async function renderDashboard(el) {
    // 先加载区域数据
    await loadRegions();
    const [ov, rs, ra] = await Promise.all([API.dashboard.overview(), API.dashboard.regionStats(), API.dashboard.recentAllocations()]);
    const d = ov?.data || {};
    const regions = rs?.data?.regions || [];
    const allocs = ra?.data?.list || [];

    const assignedPct = d.totalCount ? Math.round(d.assignedCount*100/d.totalCount) : 0;
    const availablePct = d.totalCount ? Math.round((d.totalCount - d.assignedCount)*100/d.totalCount) : 0;

    el.innerHTML = `
    <div class="stat-cards">
        <div class="stat-card blue"><div class="label">IP地址总数</div><div class="value">${(d.totalCount||0).toLocaleString()}</div><div class="trend">管理系统总地址</div></div>
        <div class="stat-card green"><div class="label">已分配地址</div><div class="value">${(d.assignedCount||0).toLocaleString()}</div><div class="trend">利用率 ${d.utilizationRate||0}%</div></div>
        <div class="stat-card orange"><div class="label">可用地址</div><div class="value">${(d.availableCount||0).toLocaleString()}</div><div class="trend">可分配</div></div>
        <div class="stat-card red"><div class="label">到期预警</div><div class="value">${d.expiringCount||0}</div><div class="trend">7日内到期</div></div>
    </div>
    <div class="chart-row">
        <div class="chart-box">
            <div class="chart-title">各区域地址使用情况 <span class="badge">实时</span></div>
            <div class="bar-chart">${regions.map(r => {
                const max = r.totalCount || 1;
                const aH = Math.round((r.assignedCount||0)/max*150);
                const bH = Math.round((r.availableCount||0)/max*150);
                return `<div class="bar-group"><div class="bars"><div class="bar a" style="height:${aH}px"></div><div class="bar b" style="height:${bH}px"></div></div><div class="label">${regionMap[r.regionCode]||r.regionCode}</div></div>`;
            }).join('')}</div>
            <div style="display:flex;gap:20px;justify-content:center;margin-top:12px;font-size:12px;color:#8c8c8c">
                <span><span style="display:inline-block;width:10px;height:10px;background:#1890ff;border-radius:2px;margin-right:4px"></span>已分配</span>
                <span><span style="display:inline-block;width:10px;height:10px;background:#52c41a;border-radius:2px;margin-right:4px"></span>可用</span>
            </div>
        </div>
        <div class="chart-box">
            <div class="chart-title">地址状态分布</div>
            <div class="donut-container">
                <div class="donut" style="position:relative"><div class="donut-center"><div class="num">${d.totalCount?Math.round(d.totalCount/1000)+'K':'0'}</div><div class="txt">总地址</div></div></div>
                <div class="legend">
                    <div class="legend-item"><div class="legend-dot" style="background:#1890ff"></div>已分配 <span class="count">${assignedPct}%</span></div>
                    <div class="legend-item"><div class="legend-dot" style="background:#52c41a"></div>可用 <span class="count">${availablePct}%</span></div>
                </div>
            </div>
        </div>
    </div>
    <div class="table-panel">
        <div class="table-header"><h3>最近分配记录</h3><span class="link" onclick="showPage('allocation')">查看全部 →</span></div>
        <table><thead><tr><th>IP地址</th><th>地址类型</th><th>客户ID</th><th>工单号</th><th>分配时间</th><th>到期时间</th><th>状态</th></tr></thead>
        <tbody>${allocs.map(a => `<tr><td>${a.ipAddress}</td><td>${tag('blue',IP_TYPE_MAP[1]||'IPv4')}</td><td>${a.subscriberId||'-'}</td><td>${a.serviceOrderId||'-'}</td><td>${formatTime(a.allocationTime)}</td><td>${formatTime(a.expirationTime)}</td><td>${tag(ALLOC_STATUS_TAG[a.status]||'green', ALLOC_STATUS_MAP[a.status]||'未知')}</td></tr>`).join('')}</tbody></table>
    </div>`;
}

// ========== 2. Address Pool ==========
async function renderPool(el) {
    // 先加载区域数据
    await loadRegions();
    const res = await API.pool.list();
    const list = res?.data?.list || [];
    el.innerHTML = `
    <div class="search-bar"><div class="search-row">
        <div class="form-item"><label>地址池编码</label><input id="sp-code" placeholder="请输入编码"></div>
        <div class="form-item"><label>地址类型</label><select id="sp-type"><option value="">全部</option><option value="1">IPv4</option><option value="2">IPv6</option></select></div>
        <div class="form-item"><label>所属区域</label>${renderRegionSelect('sp-region')}</div>
        <div class="form-item"><label>状态</label><select id="sp-status"><option value="">全部</option><option value="1">启用</option><option value="2">禁用</option></select></div>
        <div class="btn-group"><button class="btn primary" onclick="searchPool()">🔍 查询</button><button class="btn" onclick="resetPoolSearch()">重置</button></div>
    </div></div>
    <div class="btn-group" style="margin-bottom:16px"><button class="btn primary" onclick="addPoolDialog()">＋ 新建地址池</button></div>
    <div class="table-panel"><table><thead><tr><th>地址池名称</th><th>编码</th><th>类型</th><th>地址范围</th><th>总数</th><th>可用数</th><th>利用率</th><th>区域</th><th>分配策略</th><th>状态</th><th>操作</th></tr></thead>
    <tbody id="pool-tbody">${renderPoolRows(list)}</tbody></table>
    <div class="pagination"><span>共 ${res?.data?.total||0} 条记录</span></div></div>`;
}

function renderPoolRows(list) {
    return list.map(p => { const pct = p.totalCount>0?Math.round((p.totalCount-p.availableCount)*100/p.totalCount):0; const pctClass=pct>70?'orange':pct>40?'blue':'green';
    return `<tr><td>${p.poolName}</td><td>${p.poolCode}</td><td>${tag(IP_TYPE_MAP[p.ipType]==='IPv6'?'purple':'blue',IP_TYPE_MAP[p.ipType]||'IPv4')}</td>
    <td>${p.startAddress} ~ ${p.endAddress}</td><td>${p.totalCount}</td><td>${p.availableCount}</td>
    <td><div class="progress-bar" style="width:120px"><div class="fill ${pctClass}" style="width:${pct}%"></div></div><span style="font-size:12px;color:#8c8c8c;margin-left:8px">${pct}%</span></td>
    <td>${regionMap[p.regionCode]||p.regionCode||'-'}</td><td>${tag('gray',p.allocationStrategy===1?'顺序':'随机')}</td><td>${tag(p.status===1?'green':'orange',POOL_STATUS_MAP[p.status]||'未知')}</td>
    <td><span class="link" onclick="togglePoolStatus(${p.id},${p.status===1?2:1})">${p.status===1?'禁用':'启用'}</span></td></tr>`; }).join('');
}

async function searchPool() {
    const params = {};
    const c=document.getElementById('sp-code').value; if(c) params.poolCode=c;
    const t=document.getElementById('sp-type').value; if(t) params.ipType=t;
    const r=document.getElementById('sp-region').value; if(r) params.regionCode=r;
    const s=document.getElementById('sp-status').value; if(s) params.status=s;
    const res = await API.pool.list(params);
    document.getElementById('pool-tbody').innerHTML = renderPoolRows(res?.data?.list || []);
}

function resetPoolSearch() {
    ['sp-code','sp-type','sp-region','sp-status'].forEach(id => { const e=document.getElementById(id); if(e) e.value=''; });
    searchPool();
}

async function togglePoolStatus(id, status) { await API.pool.updateStatus(id, status); loadPage('pool'); }

function addPoolDialog() {
    openDialog('新建地址池', `<div class="form-grid">
        <div class="form-field"><label>地址池名称 <span class="req">*</span></label><input id="np-name" placeholder="请输入名称"></div>
        <div class="form-field"><label>地址池编码 <span class="req">*</span></label><input id="np-code" placeholder="请输入编码"></div>
        <div class="form-field"><label>地址类型 <span class="req">*</span></label><select id="np-type"><option value="1">IPv4</option><option value="2">IPv6</option></select></div>
        <div class="form-field"><label>所属区域 <span class="req">*</span></label>${renderRegionSelect('np-region', false)}</div>
        <div class="form-field"><label>起始地址 <span class="req">*</span></label><input id="np-start" placeholder="如 218.201.3.0"></div>
        <div class="form-field"><label>结束地址 <span class="req">*</span></label><input id="np-end" placeholder="如 218.201.3.255"></div>
        <div class="form-field"><label>分配策略</label><select id="np-strategy"><option value="1">顺序分配</option><option value="2">随机分配</option></select></div>
        <div class="form-field"><label>优先级</label><input id="np-priority" type="number" value="1"></div>
    </div>`, `<button class="btn" onclick="closeDialog()">取消</button><button class="btn primary" onclick="submitAddPool()">确定</button>`);
}

async function submitAddPool() {
    const data = { poolName: document.getElementById('np-name').value, poolCode: document.getElementById('np-code').value,
        ipType: +document.getElementById('np-type').value, regionCode: document.getElementById('np-region').value,
        startAddress: document.getElementById('np-start').value, endAddress: document.getElementById('np-end').value,
        allocationStrategy: +document.getElementById('np-strategy').value, priority: +document.getElementById('np-priority').value };
    const res = await API.pool.add(data);
    if (res) { closeDialog(); loadPage('pool'); API.showMessage('地址池创建成功', 'success'); }
}

// ========== 3. IP Address ==========
async function renderIpAddr(el) {
    // 先加载区域数据
    await loadRegions();
    const res = await API.ip.list();
    const list = res?.data?.list || [];
    el.innerHTML = `
    <div class="search-bar"><div class="search-row">
        <div class="form-item"><label>IP地址</label><input id="si-ip" placeholder="请输入IP"></div>
        <div class="form-item"><label>地址类型</label><select id="si-type"><option value="">全部</option><option value="1">IPv4</option><option value="2">IPv6</option></select></div>
        <div class="form-item"><label>状态</label><select id="si-status"><option value="">全部</option><option value="1">未分配</option><option value="2">已分配</option><option value="3">预留</option><option value="4">冻结</option></select></div>
        <div class="form-item"><label>区域</label>${renderRegionSelect('si-region')}</div>
        <div class="btn-group"><button class="btn primary" onclick="searchIp()">🔍 查询</button><button class="btn" onclick="resetIpSearch()">重置</button></div>
    </div></div>
    <div class="table-panel"><table><thead><tr><th>IP地址</th><th>类型</th><th>子网掩码</th><th>网关</th><th>地址池ID</th><th>区域</th><th>状态</th><th>操作</th></tr></thead>
    <tbody id="ip-tbody">${renderIpRows(list)}</tbody></table>
    <div class="pagination"><span>共 ${res?.data?.total||0} 条记录</span></div></div>`;
}

function renderIpRows(list) {
    return list.map(ip => `<tr><td>${ip.ipAddress}</td><td>${tag(IP_TYPE_MAP[ip.ipType]==='IPv6'?'purple':'blue',IP_TYPE_MAP[ip.ipType]||'IPv4')}</td>
    <td>${ip.subnetMask||'-'}</td><td>${ip.gateway||'-'}</td><td>${ip.addressPoolId||'-'}</td><td>${regionMap[ip.regionCode]||ip.regionCode||'-'}</td>
    <td>${tag(IP_STATUS_TAG[ip.status]||'gray',IP_STATUS_MAP[ip.status]||'未知')}</td>
    <td>${ip.status===4?`<span class="link" style="color:#52c41a" onclick="doIpUnfreeze(${ip.id})">解冻</span>`:`<span class="link" style="color:#fa8c16" onclick="doIpFreeze(${ip.id})">冻结</span>`}</td></tr>`).join('');
}

async function searchIp() {
    const params = {};
    const v=document.getElementById('si-ip').value; if(v) params.ipAddress=v;
    const t=document.getElementById('si-type').value; if(t) params.ipType=t;
    const s=document.getElementById('si-status').value; if(s) params.status=s;
    const r=document.getElementById('si-region').value; if(r) params.regionCode=r;
    const res = await API.ip.list(params);
    document.getElementById('ip-tbody').innerHTML = renderIpRows(res?.data?.list || []);
}
function resetIpSearch() { ['si-ip','si-type','si-status','si-region'].forEach(id=>{const e=document.getElementById(id);if(e)e.value='';}); searchIp(); }
async function doIpFreeze(id) { await API.ip.freeze(id); searchIp(); }
async function doIpUnfreeze(id) { await API.ip.unfreeze(id); searchIp(); }

// ========== 4. Allocation ==========
async function renderAllocation(el) {
    // 先加载区域数据
    await loadRegions();
    el.innerHTML = `
    <div class="tab-nav">
        <div class="tab-item active" onclick="switchAllocTab(this,'alloc-assign')">地址分配</div>
        <div class="tab-item" onclick="switchAllocTab(this,'alloc-release')">地址释放</div>
        <div class="tab-item" onclick="switchAllocTab(this,'alloc-renew')">地址续约</div>
        <div class="tab-item" onclick="switchAllocTab(this,'alloc-record')">分配记录</div>
    </div>
    <div class="alloc-tab" id="alloc-assign">
        <div class="chart-box" style="max-width:700px"><div class="chart-title">新建地址分配</div>
        <div class="form-grid">
            <div class="form-field"><label>地址类型 <span class="req">*</span></label><select id="aa-type"><option value="1">IPv4</option><option value="2">IPv6</option></select></div>
            <div class="form-field"><label>所属区域 <span class="req">*</span></label>${renderRegionSelect('aa-region', false)}</div>
            <div class="form-field"><label>需求数量 <span class="req">*</span></label><input id="aa-count" type="number" value="5"></div>
            <div class="form-field"><label>分配类型</label><select id="aa-atype"><option value="1">自动分配</option><option value="2">手动分配</option></select></div>
            <div class="form-field"><label>客户ID</label><input id="aa-sub"></div>
            <div class="form-field"><label>业务工单号</label><input id="aa-order"></div>
            <div class="form-field"><label>有效期(天)</label><input id="aa-days" type="number" value="90"></div>
            <div class="form-field"><label>操作员</label><input id="aa-op" value="admin"></div>
        </div>
        <div style="margin-top:20px"><button class="btn primary" onclick="doAllocate()">✅ 确认分配</button><button class="btn" onclick="document.getElementById('alloc-result').classList.remove('show')">重置</button></div>
        <div class="alloc-result" id="alloc-result"></div></div>
    </div>
    <div class="alloc-tab" id="alloc-release" style="display:none">
        <div class="chart-box" style="max-width:600px"><div class="chart-title">地址释放</div>
        <div class="form-grid">
            <div class="form-field full"><label>分配记录ID <span class="req">*</span></label><input id="ar-id" placeholder="请输入要释放的分配记录ID"></div>
            <div class="form-field"><label>释放类型</label><select id="ar-type"><option value="2">主动释放</option><option value="1">到期释放</option></select></div>
        </div>
        <div style="margin-top:20px"><button class="btn danger" onclick="doRelease()">🔓 确认释放</button></div></div>
    </div>
    <div class="alloc-tab" id="alloc-renew" style="display:none">
        <div class="chart-box" style="max-width:600px"><div class="chart-title">地址续约</div>
        <div class="form-grid">
            <div class="form-field full"><label>分配记录ID <span class="req">*</span></label><input id="ae-id" placeholder="请输入要续约的分配记录ID"></div>
            <div class="form-field full"><label>续约天数 <span class="req">*</span></label><input id="ae-days" type="number" value="30"></div>
        </div>
        <div style="margin-top:20px"><button class="btn primary" onclick="doRenew()">🔄 确认续约</button></div></div>
    </div>
    <div class="alloc-tab" id="alloc-record" style="display:none"><div class="loading">加载中...</div></div>`;

    await loadAllocRecords();
}

function switchAllocTab(tabEl, tabId) {
    tabEl.parentElement.querySelectorAll('.tab-item').forEach(t=>t.classList.remove('active'));
    tabEl.classList.add('active');
    document.querySelectorAll('.alloc-tab').forEach(t=>t.style.display='none');
    document.getElementById(tabId).style.display='block';
    if (tabId==='alloc-record') loadAllocRecords();
}

async function doAllocate() {
    const data = { ipType: +document.getElementById('aa-type').value, regionCode: document.getElementById('aa-region').value,
        requireCount: +document.getElementById('aa-count').value, allocationType: +document.getElementById('aa-atype').value,
        subscriberId: document.getElementById('aa-sub').value, serviceOrderId: document.getElementById('aa-order').value,
        expireDays: +document.getElementById('aa-days').value, operatorId: document.getElementById('aa-op').value };
    const res = await API.allocation.allocate(data);
    const r = res?.data;
    if (r) {
        const items = r.allocationList || [];
        document.getElementById('alloc-result').innerHTML = `<div class="title">✅ ${r.message}，共分配 ${items.length} 个IP地址</div>
        <table><thead><tr><th>IP地址</th><th>地址池ID</th><th>分配记录ID</th></tr></thead>
        <tbody>${items.map(i=>`<tr><td>${i.ipAddress}</td><td>${i.addressPoolId}</td><td>${i.allocationId}</td></tr>`).join('')}</tbody></table>`;
        document.getElementById('alloc-result').classList.add('show');
    }
}

async function doRelease() {
    const id = document.getElementById('ar-id').value;
    const type = document.getElementById('ar-type').value;
    if (!id) { API.showMessage('请输入分配记录ID','error'); return; }
    const res = await API.allocation.release(id, type);
    if (res) API.showMessage('释放成功','success');
}

async function doRenew() {
    const id = document.getElementById('ae-id').value;
    const days = document.getElementById('ae-days').value;
    if (!id) { API.showMessage('请输入分配记录ID','error'); return; }
    const res = await API.allocation.renew(id, days);
    if (res) API.showMessage('续约成功','success');
}

async function loadAllocRecords() {
    // 确保区域数据已加载
    if (!regionData) await loadRegions();
    const res = await API.resource.ipList({pageSize: 20});
    const list = (res?.data?.list || []).filter(i => i.status === 2);
    const el = document.getElementById('alloc-record');
    if (!el) return;
    el.innerHTML = `<div class="table-panel"><table><thead><tr><th>IP地址</th><th>类型</th><th>区域</th><th>状态</th></tr></thead>
    <tbody>${list.map(ip=>`<tr><td>${ip.ipAddress}</td><td>${tag('blue',IP_TYPE_MAP[ip.ipType]||'IPv4')}</td><td>${regionMap[ip.regionCode]||ip.regionCode||'-'}</td><td>${tag(IP_STATUS_TAG[ip.status]||'green',IP_STATUS_MAP[ip.status]||'已分配')}</td></tr>`).join('')}</tbody></table></div>`;
}

// ========== 5. Device ==========
async function renderDevice(el) {
    // 先加载区域数据
    await loadRegions();
    const res = await API.device.list();
    const list = res?.data?.list || [];
    el.innerHTML = `
    <div class="search-bar"><div class="search-row">
        <div class="form-item"><label>设备编码</label><input id="sd-code" placeholder="请输入设备编码"></div>
        <div class="form-item"><label>设备类型</label><select id="sd-type"><option value="">全部</option><option value="OLT">OLT</option><option value="BRAS">BRAS</option><option value="交换机">交换机</option></select></div>
        <div class="form-item"><label>区域</label>${renderRegionSelect('sd-region')}</div>
        <div class="btn-group"><button class="btn primary" onclick="searchDevice()">🔍 查询</button></div>
    </div></div>
    <div class="btn-group" style="margin-bottom:16px"><button class="btn primary" onclick="addDeviceDialog()">＋ 注册设备</button></div>
    <div class="table-panel"><table><thead><tr><th>设备名称</th><th>设备编码</th><th>类型</th><th>管理IP</th><th>厂商</th><th>区域</th><th>状态</th><th>最后采集时间</th><th>操作</th></tr></thead>
    <tbody id="device-tbody">${renderDeviceRows(list)}</tbody></table>
    <div class="pagination"><span>共 ${res?.data?.total||0} 条记录</span></div></div>`;
}

function renderDeviceRows(list) {
    return list.map(d => `<tr><td>${d.deviceName}</td><td>${d.deviceCode}</td><td>${tag(d.deviceType==='BRAS'?'purple':d.deviceType==='交换机'?'green':'blue',d.deviceType||'-')}</td>
    <td>${d.deviceIp||'-'}</td><td>${d.vendor||'-'}</td><td>${regionMap[d.regionCode]||d.regionCode||'-'}</td>
    <td><div class="status-indicator"><div class="status-dot ${d.status===1?'online':'offline'}"></div>${DEVICE_STATUS_MAP[d.status]||'未知'}</div></td>
    <td>${formatTime(d.lastCollectionTime)}</td>
    <td><span class="link" style="color:#ff4d4f" onclick="deleteDevice(${d.id},this)">删除</span></td></tr>`).join('');
}

async function deleteDevice(id, el) {
    if (!confirm('确认删除该设备？删除后不可恢复。')) return;
    const res = await API.device.delete(id);
    if (res) {
        API.showMessage('设备删除成功', 'success');
        searchDevice();
    }
}

async function searchDevice() {
    const params = {};
    const c=document.getElementById('sd-code').value; if(c) params.deviceCode=c;
    const t=document.getElementById('sd-type').value; if(t) params.deviceType=t;
    const r=document.getElementById('sd-region').value; if(r) params.regionCode=r;
    const res = await API.device.list(params);
    document.getElementById('device-tbody').innerHTML = renderDeviceRows(res?.data?.list || []);
}

function addDeviceDialog() {
    openDialog('注册设备', `<div class="form-grid">
        <div class="form-field"><label>设备名称 <span class="req">*</span></label><input id="nd-name" placeholder="请输入名称"></div>
        <div class="form-field"><label>设备编码 <span class="req">*</span></label><input id="nd-code" placeholder="如 OLT-XA-001"></div>
        <div class="form-field"><label>设备类型</label><select id="nd-type"><option>OLT</option><option>BRAS</option><option>交换机</option></select></div>
        <div class="form-field"><label>管理IP</label><input id="nd-ip" placeholder="10.255.x.x"></div>
        <div class="form-field"><label>厂商</label><select id="nd-vendor"><option>华为</option><option>中兴</option></select></div>
        <div class="form-field"><label>区域</label>${renderRegionSelect('nd-region', false)}</div>
    </div>`, `<button class="btn" onclick="closeDialog()">取消</button><button class="btn primary" onclick="submitAddDevice()">确定</button>`);
}

async function submitAddDevice() {
    const data = { deviceName: document.getElementById('nd-name').value, deviceCode: document.getElementById('nd-code').value,
        deviceType: document.getElementById('nd-type').value, deviceIp: document.getElementById('nd-ip').value,
        vendor: document.getElementById('nd-vendor').value, regionCode: document.getElementById('nd-region').value };
    const res = await API.device.register(data);
    if (res) { closeDialog(); loadPage('device'); API.showMessage('设备注册成功','success'); }
}

// ========== 6. Audit ==========
async function renderAudit(el) {
    const [ov, hist] = await Promise.all([API.audit.overview(), API.audit.history()]);
    const d = ov?.data || {};
    const items = d.auditItems || [];
    const history = hist?.data?.list || [];

    el.innerHTML = `
    <div class="stat-cards" style="grid-template-columns: repeat(3,1fr)">
        <div class="stat-card blue"><div class="label">最近稽核时间</div><div class="value" style="font-size:20px">${d.lastAuditTime||'-'}</div></div>
        <div class="stat-card green"><div class="label">稽核通过率</div><div class="value">${d.passRate||0}%</div></div>
        <div class="stat-card orange"><div class="label">异常记录数</div><div class="value">${d.exceptionCount||0}</div></div>
    </div>
    <div class="chart-box" style="margin-bottom:20px"><div class="chart-title">稽核结果概览</div>
    <table><thead><tr><th>稽核项目</th><th>总数</th><th>正常</th><th>异常</th><th>通过率</th></tr></thead>
    <tbody>${items.map(i=>`<tr><td>${i.name}</td><td>${i.total}</td><td>${i.normal}</td><td>${i.abnormal}</td><td>${tag(parseFloat(i.passRate)>=99?'green':'orange',i.passRate)}</td></tr>`).join('')}</tbody></table></div>
    <div class="chart-box"><div class="chart-title">稽核执行历史</div>
    <div class="audit-timeline">${history.map(h=>`<div class="audit-item">
        <div class="audit-time">${h.time}</div><div class="audit-status-dot ${h.status}"></div>
        <div class="audit-info"><div class="desc">${h.title}</div><div class="detail">${h.detail}</div></div></div>`).join('')}</div></div>
    <div class="sftp-config"><div class="chart-title">SFTP推送配置</div>
    <div class="config-grid">
        <div class="config-field"><label>服务器地址</label><input value="sftp.resource-center.sn.cmcc.cn" disabled></div>
        <div class="config-field"><label>端口</label><input value="22" disabled></div>
        <div class="config-field"><label>远程目录</label><input value="/data/audit/ip/" disabled></div>
        <div class="config-field"><label>调度时间</label><input value="每日 02:00 (Cron: 0 0 2 * * ?)" disabled></div>
    </div>
    <div style="margin-top:16px"><button class="btn primary" onclick="API.audit.trigger().then(()=>API.showMessage('稽核任务已触发','success'))">🔍 手动触发稽核</button></div></div>`;
}

// ========== 7. Binding ==========
async function renderBinding(el) {
    const res = await API.binding.list();
    const list = res?.data?.list || [];
    el.innerHTML = `
    <div class="search-bar"><div class="search-row">
        <div class="form-item"><label>IP地址</label><input id="sb-ip" placeholder="请输入IP"></div>
        <div class="form-item"><label>绑定类型</label><select id="sb-type"><option value="">全部</option><option value="1">静态绑定</option><option value="2">动态绑定</option></select></div>
        <div class="form-item"><label>状态</label><select id="sb-status"><option value="">全部</option><option value="1">已绑定</option><option value="2">已解绑</option></select></div>
        <div class="btn-group"><button class="btn primary" onclick="searchBinding()">🔍 查询</button></div>
    </div></div>
    <div class="binding-card" style="margin-bottom:20px"><div class="chart-title">绑定关系示意</div>
    <div class="binding-flow">
        <div class="binding-node active"><div class="node-title">IP地址</div><div class="node-value">218.201.1.10</div></div>
        <div class="binding-arrow">⇄</div>
        <div class="binding-node active"><div class="node-title">绑定类型</div><div class="node-value">静态绑定</div></div>
        <div class="binding-arrow">⇄</div>
        <div class="binding-node active"><div class="node-title">设备</div><div class="node-value">OLT-XA-001</div></div>
    </div></div>
    <div class="table-panel"><table><thead><tr><th>IP地址</th><th>设备ID</th><th>绑定类型</th><th>绑定时间</th><th>状态</th><th>操作</th></tr></thead>
    <tbody id="binding-tbody">${renderBindingRows(list)}</tbody></table></div>`;
}

function renderBindingRows(list) {
    return list.map(b=>`<tr><td>${b.ipAddress}</td><td>${b.deviceId}</td><td>${tag(b.bindingType===1?'blue':'orange',BINDING_TYPE_MAP[b.bindingType]||'未知')}</td>
    <td>${formatTime(b.bindingTime)}</td><td>${tag(b.status===1?'green':'gray',BINDING_STATUS_MAP[b.status]||'未知')}</td>
    <td>${b.status===1?`<span class="link" style="color:#ff4d4f" onclick="doUnbind(${b.id})">解绑</span>`:'-'}</td></tr>`).join('');
}

async function searchBinding() {
    const params = {};
    const v=document.getElementById('sb-ip').value; if(v) params.ipAddress=v;
    const t=document.getElementById('sb-type').value; if(t) params.bindingType=t;
    const s=document.getElementById('sb-status').value; if(s) params.status=s;
    const res = await API.binding.list(params);
    document.getElementById('binding-tbody').innerHTML = renderBindingRows(res?.data?.list || []);
}

async function doUnbind(id) { await API.binding.unbind(id); searchBinding(); }

// ========== 8. Report ==========
async function renderReport(el) {
    // 先加载区域数据
    await loadRegions();
    const [ov, rs] = await Promise.all([API.dashboard.overview(), API.dashboard.regionStats()]);
    const d = ov?.data || {};
    const regions = rs?.data?.regions || [];

    el.innerHTML = `
    <div class="stat-cards">
        <div class="stat-card blue"><div class="label">地址池总数</div><div class="value">${d.poolCount||4}</div></div>
        <div class="stat-card green"><div class="label">已分配</div><div class="value">${(d.assignedCount||0).toLocaleString()}</div></div>
        <div class="stat-card orange"><div class="label">可用地址</div><div class="value">${(d.availableCount||0).toLocaleString()}</div></div>
        <div class="stat-card red"><div class="label">到期预警</div><div class="value">${d.expiringCount||0}</div></div>
    </div>
    <div class="chart-row">
        <div class="chart-box"><div class="chart-title">区域地址使用趋势</div>
        <div class="bar-chart">${regions.map(r => {
            const max = r.totalCount || 1;
            const aH = Math.round((r.assignedCount||0)/max*150);
            const bH = Math.round((r.availableCount||0)/max*150);
            return `<div class="bar-group"><div class="bars"><div class="bar a" style="height:${aH}px"></div><div class="bar b" style="height:${bH}px"></div></div><div class="label">${regionMap[r.regionCode]||r.regionCode}</div></div>`;
        }).join('')}</div>
        <div style="display:flex;gap:20px;justify-content:center;margin-top:12px;font-size:12px;color:#8c8c8c">
            <span><span style="display:inline-block;width:10px;height:10px;background:#1890ff;border-radius:2px;margin-right:4px"></span>已分配</span>
            <span><span style="display:inline-block;width:10px;height:10px;background:#52c41a;border-radius:2px;margin-right:4px"></span>可用</span>
        </div></div>
        <div class="chart-box"><div class="chart-title">区域地址分布</div>
        <div class="region-grid" style="grid-template-columns:1fr">${regions.map(r => {
            const pct = r.totalCount>0?Math.round((r.assignedCount||0)*100/r.totalCount):0;
            const cls = pct>80?'orange':pct>50?'blue':'green';
            return `<div class="region-card"><div class="name">${regionMap[r.regionCode]||r.regionCode} ${tag(cls,pct>80?'预警':'正常')}</div>
            <div class="region-stat"><span>总数</span><span class="val">${(r.totalCount||0).toLocaleString()}</span></div>
            <div class="region-stat"><span>已用</span><span class="val">${(r.assignedCount||0).toLocaleString()}</span></div>
            <div class="progress-bar" style="width:100%"><div class="fill ${cls}" style="width:${pct}%"></div></div></div>`;
        }).join('')}</div></div>
    </div>`;
}

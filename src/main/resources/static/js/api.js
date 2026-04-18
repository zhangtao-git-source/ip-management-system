const API = {
    BASE: '/api/v1',

    async request(method, url, data) {
        const opts = { method, headers: { 'Content-Type': 'application/json' } };
        if (data && method !== 'GET') opts.body = JSON.stringify(data);
        try {
            const resp = await fetch(this.BASE + url, opts);
            const result = await resp.json();
            if (result.code !== 200 && result.code !== 201) {
                this.showMessage(result.message || '请求失败', 'error');
                return null;
            }
            return result;
        } catch (e) {
            console.error('API Error:', e);
            this.showMessage('网络请求异常', 'error');
            return null;
        }
    },

    get(url) { return this.request('GET', url); },
    post(url, data) { return this.request('POST', url, data); },
    put(url, data) { return this.request('PUT', url, data); },

    showMessage(msg, type = 'info') {
        const el = document.createElement('div');
        el.className = 'msg-toast ' + type;
        el.textContent = msg;
        document.body.appendChild(el);
        setTimeout(() => el.remove(), 3000);
    },

    // Dashboard
    dashboard: {
        overview: () => API.get('/dashboard/overview'),
        regionStats: () => API.get('/dashboard/region-stats'),
        recentAllocations: (limit = 10) => API.get('/dashboard/recent-allocations?limit=' + limit),
    },

    // Address Pool
    pool: {
        list: (params = {}) => {
            const q = new URLSearchParams(params).toString();
            return API.get('/pool/list' + (q ? '?' + q : ''));
        },
        getById: (id) => API.get('/pool/' + id),
        add: (data) => API.post('/pool/add', data),
        update: (data) => API.put('/pool/update', data),
        updateStatus: (id, status) => API.put('/pool/' + id + '/status?status=' + status),
    },

    // IP Address
    ip: {
        list: (params = {}) => {
            const q = new URLSearchParams(params).toString();
            return API.get('/ip/list' + (q ? '?' + q : ''));
        },
        getById: (id) => API.get('/ip/' + id),
        add: (data) => API.post('/ip/add', data),
        update: (data) => API.put('/ip/update', data),
        freeze: (id) => API.put('/ip/' + id + '/freeze'),
        unfreeze: (id) => API.put('/ip/' + id + '/unfreeze'),
        batchFreeze: (ids) => API.put('/ip/batch-freeze', ids),
        batchUnfreeze: (ids) => API.put('/ip/batch-unfreeze', ids),
    },

    // Allocation
    allocation: {
        allocate: (data) => API.post('/allocation/allocate', data),
        release: (allocationId, releaseType) => API.post('/allocation/release?allocationId=' + allocationId + '&releaseType=' + (releaseType || 2)),
        renew: (allocationId, extendDays) => API.post('/allocation/renew?allocationId=' + allocationId + '&extendDays=' + extendDays),
    },

    // Resource (allocation records)
    resource: {
        ipList: (params = {}) => {
            const q = new URLSearchParams(params).toString();
            return API.get('/resource/ip-address' + (q ? '?' + q : ''));
        },
        poolList: (params = {}) => {
            const q = new URLSearchParams(params).toString();
            return API.get('/resource/address-pool' + (q ? '?' + q : ''));
        },
    },

    // Device
    device: {
        list: (params = {}) => {
            const q = new URLSearchParams(params).toString();
            return API.get('/device/list' + (q ? '?' + q : ''));
        },
        register: (data) => API.post('/device/register', data),
        update: (data) => API.put('/device/update', data),
        delete: (id) => API.request('DELETE', '/device/' + id),
        getStatus: (code) => API.get('/device/status/' + code),
    },

    // Collection
    collection: {
        receive: (data) => API.post('/collection/receive', data),
        registerDevice: (data) => API.post('/collection/device/register', data),
    },

    // Binding
    binding: {
        list: (params = {}) => {
            const q = new URLSearchParams(params).toString();
            return API.get('/binding/list' + (q ? '?' + q : ''));
        },
        create: (data) => API.post('/binding/create', data),
        unbind: (id) => API.post('/binding/unbind?bindingId=' + id),
    },

    // Audit
    audit: {
        overview: () => API.get('/audit/overview'),
        history: (pageNum = 1, pageSize = 10) => API.get('/audit/history?pageNum=' + pageNum + '&pageSize=' + pageSize),
        trigger: () => API.post('/audit/trigger'),
    },

    // Region
    region: {
        cities: () => API.get('/region/cities'),
        tree: () => API.get('/region/tree'),
        list: () => API.get('/region/list'),
        districts: (cityCode) => API.get('/region/districts/' + cityCode),
        getName: (code) => API.get('/region/name/' + code),
    },
};

// Region/Status helper maps
const IP_TYPE_MAP = { 1: 'IPv4', 2: 'IPv6' };
const IP_STATUS_MAP = { 1: '未分配', 2: '已分配', 3: '预留', 4: '冻结' };
const IP_STATUS_TAG = { 1: 'gray', 2: 'green', 3: 'orange', 4: 'red' };
const ALLOC_STATUS_MAP = { 1: '使用中', 2: '已释放' };
const ALLOC_STATUS_TAG = { 1: 'green', 2: 'gray' };
const BINDING_TYPE_MAP = { 1: '静态', 2: '动态' };
const BINDING_STATUS_MAP = { 1: '已绑定', 2: '已解绑' };
const POOL_STATUS_MAP = { 1: '启用', 2: '禁用' };
const DEVICE_STATUS_MAP = { 1: '在线', 2: '离线' };
const REGION_MAP = { XIAN: '西安', XY: '咸阳', WN: '渭南', BJ: '宝鸡', TC: '铜川', HZ: '汉中', AK: '安康' };

function tag(type, text) {
    return '<span class="tag ' + type + '">' + text + '</span>';
}

function formatTime(t) {
    if (!t) return '-';
    return String(t).replace('T', ' ').substring(0, 16);
}

// API接口封装 - 这个文件包含了所有与后端服务器通信的API函数

// 定义后端API的基础URL地址 - 使用绝对URL确保请求发送到正确的服务器
const API_BASE_URL = 'http://localhost:8080/SA';

// 定义受保护的API路径前缀列表
const PROTECTED_API_PATHS = [
    '/api/AllGroups/',
    '/api/GroupMun/'
];

// 统一处理HTTP请求的响应
// 根据需求，后端返回的统一JSON格式为：{"state":"ok","code":"200","data":"后端数据"}
const handleResponse = async (response) => {
    if (!response.ok) {
        // 如果状态码是401，返回特殊的错误对象，用于处理token过期情况
        if (response.status === 401) {
            return { isAuthError: true, status: 401, message: '认证令牌无效或已过期' };
        }
        throw new Error('网络请求失败');
    }
    
    const result = await response.json();
    
    // 检查响应状态
    if (result.state !== 'ok' || result.code !== '200') {
        throw new Error(result.data || '请求失败');
    }
    
    return result.data;
};

// 检查URL是否是受保护资源
function isProtectedResource(url) {
    // 移除基础URL部分以获取相对路径
    let relativePath = url;
    if (url.startsWith(API_BASE_URL)) {
        relativePath = url.substring(API_BASE_URL.length);
    }
    
    // 检查是否匹配任何受保护路径前缀
    return PROTECTED_API_PATHS.some(prefix => relativePath.startsWith(prefix));
}

// 用户认证相关API - 包含登录、注册、令牌刷新和登出功能
const authAPI = {
    // 检查用户是否已登录（通过检查localStorage中是否有accessToken）
    checkLoginStatus: async () => {
        try {
            const accessToken = localStorage.getItem('accessToken');
            if (!accessToken) {
                return false;
            }
            
            // 尝试访问受保护资源来验证token是否有效
            const isValid = await authAPI.validateToken();
            return isValid;
        } catch (error) {
            console.error('检查登录状态失败:', error);
            return false;
        }
    },
    
    // 验证当前token是否有效
    validateToken: async () => {
        try {
            const accessToken = localStorage.getItem('accessToken');
            if (!accessToken) {
                return false;
            }
            
            // 尝试访问一个受保护的资源
            try {
                await fetchProtectedResource(`${API_BASE_URL}/api/AllGroups/list`, {
                    method: 'GET'
                });
                return true;
            } catch (error) {
                // 如果是认证错误，则token无效
                if (error.message === '认证失败，请重新登录') {
                    return false;
                }
                // 如果是其他错误但请求成功发送，则token有效
                return true;
            }
        } catch (error) {
            console.error('验证token失败:', error);
            return false;
        }
    },

    // 用户登录API
    // 参数说明：
    // - username: 用户名
    // - password: 密码
    // - rememberMe: 是否十天内免登录 (true表示勾选，false表示未勾选)
    login: async (username, password, rememberMe) => {
        const formData = new FormData();
        formData.append('username', username);
        formData.append('password', password);
        formData.append('f', rememberMe ? '1' : '0'); // 根据API规范，使用'f'参数

        const response = await fetch(`${API_BASE_URL}/api/Users/login`, {
            method: 'POST',
            body: formData,
            credentials: 'include' // 添加credentials选项，解决CORS问题
        });
        
        const result = await response.json();
        console.log(result);
        
        if (result.state !== 'ok' || result.code !== '200') {
            throw new Error(result.data || '登录失败');
        }
        
        // 存储令牌
        localStorage.setItem('accessToken', result.data.accessToken);
        console.log('已存储accessToken:'+result.data.accessToken);
        
        // 如果有refreshToken（十天内免登录），也存储它
        if (result.data.refreshToken) {
            localStorage.setItem('refreshToken', result.data.refreshToken);
            console.log('已存储refreshToken:' + result.data.refreshToken);
        }
        
        return result.data;
    },

    // 刷新访问令牌
    refreshToken: async () => {
        try {
            const refreshToken = localStorage.getItem('refreshToken');
            
            // 如果没有refreshToken，直接返回null
            if (!refreshToken) {
                console.log('没有可用的刷新令牌');
                return null;
            }
            
            const response = await fetch(`${API_BASE_URL}/api/refresh`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${refreshToken}`
                },
                credentials: 'include' // 添加credentials选项，解决CORS问题
            });
            
            const result = await response.json();
            
            if (result.state !== 'ok' || result.code !== '200') {
                // 刷新失败，清除所有令牌
                localStorage.removeItem('accessToken');
                localStorage.removeItem('refreshToken');
                return null;
            }
            
            // 更新accessToken
            localStorage.setItem('accessToken', result.data.accessToken);
            console.log('访问令牌刷新成功');
            
            return result.data.accessToken;
        } catch (error) {
            console.error('刷新令牌失败:', error);
            // 发生错误时清除所有令牌
            localStorage.removeItem('accessToken');
            localStorage.removeItem('refreshToken');
            return null;
        }
    },

    // 用户注册API
    // 参数说明：
    // - username: 新用户的用户名
    // - password: 新用户的密码
    register: async (username, password) => {
        const formData = new FormData();
        formData.append('username', username);
        formData.append('password', password);

        const response = await fetch(`${API_BASE_URL}/api/Users/register`, {
            method: 'POST',
            body: formData,
            credentials: 'include' // 添加credentials选项，解决CORS问题
        });
        return handleResponse(response);
    },

    // 退出登录
    logout: async () => {
        // 清除本地存储的所有令牌
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        console.log('已成功登出');
    }
};

// 通用的受保护资源访问函数
// 参数说明：
// - url: API的完整URL
// - options: fetch请求的选项，包括method, headers, body等
// - retried: 内部使用，标记是否已经尝试过刷新令牌
async function fetchProtectedResource(url, options = {}, retried = false) {
    const accessToken = localStorage.getItem('accessToken');
    if (!accessToken) {
        throw new Error('未登录，请先登录');
    }
    
    // 确保headers存在
    if (!options.headers) {
        options.headers = {};
    }
    
    // 添加认证头
    options.headers['Authorization'] = `Bearer ${accessToken}`;
    
    // 添加credentials选项，解决CORS问题
    options.credentials = 'include';
    
    // 发送请求
    const response = await fetch(url, options);
    const result = await handleResponse(response);
    console.log(result); // 打印返回的结果，方便调试和查看info信息
    
    // 如果返回了认证错误且未重试过，尝试刷新令牌
    if (result && result.isAuthError && !retried) {
        console.log('令牌过期，尝试刷新...');
        const newToken = await authAPI.refreshToken();
        
        // 如果成功获取了新令牌，重试请求
        if (newToken) {
            options.headers['Authorization'] = `Bearer ${newToken}`;
            return fetchProtectedResource(url, options, true); // 标记为已重试
        } else {
            throw new Error('认证失败，请重新登录');
        }
    }
    
    return result;
}

// 小组管理相关API
const groupAPI = {
    // 获取所有小组的信息
    getAllGroups: async () => {
        return fetchProtectedResource(`${API_BASE_URL}/api/AllGroups/list`, {
            method: 'GET'
        });
    },

    // 获取单个小组详情
    // 参数说明：
    // - id: 小组ID
    getGroupDetail: async (id) => {
        return fetchProtectedResource(`${API_BASE_URL}/api/AllGroups/show?id=${id}`, {
            method: 'GET'
        });
    },

    // 创建新的小组
    // 参数说明：
    // - name: 小组名称
    // - introduce: 小组介绍
    addGroup: async (name, introduce) => {
        // 使用URLSearchParams代替FormData，确保正确处理中文字符
        const formData = new URLSearchParams();
        formData.append('name', name);
        formData.append('introduce', introduce);

        return fetchProtectedResource(`${API_BASE_URL}/api/AllGroups/add`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8'
            },
            body: formData
        });
    },
    
    // createGroup作为addGroup的别名，保持与dashboard.js的兼容性
    createGroup: async (name, introduce) => {
        return groupAPI.addGroup(name, introduce);
    },
    
    // 更新小组信息
    // 参数说明：
    // - id: 小组ID
    // - name: 小组名称
    // - introduce: 小组介绍
    updateGroup: async (id, name, introduce) => {
        // 使用URLSearchParams代替FormData，确保正确处理中文字符
        const formData = new URLSearchParams();
        formData.append('id', id);
        formData.append('name', name);
        formData.append('introduce', introduce);

        return fetchProtectedResource(`${API_BASE_URL}/api/AllGroups/update`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8'
            },
            body: formData
        });
    },
    
    // 删除小组
    // 参数说明：
    // - id: 小组ID
    deleteGroup: async (id) => {
        const formData = new FormData();
        formData.append('id', id);

        return fetchProtectedResource(`${API_BASE_URL}/api/AllGroups/del`, {
            method: 'POST',
            body: formData
        });
    }
};

// 通用API访问函数 - 自动判断是否为受保护资源
// 参数说明：
// - endpoint: API端点路径（不包含基础URL）
// - options: fetch请求的选项，包括method, headers, body等
const api = {
    // 发送GET请求
    get: async (endpoint, params = {}) => {
        // 构建URL，处理查询参数
        let url = `${API_BASE_URL}${endpoint}`;
        const queryParams = new URLSearchParams();
        
        for (const key in params) {
            queryParams.append(key, params[key]);
        }
        
        const queryString = queryParams.toString();
        if (queryString) {
            url += `?${queryString}`;
        }
        
        // 判断是否为受保护资源
        if (isProtectedResource(url)) {
            return fetchProtectedResource(url, { method: 'GET' });
        } else {
            const response = await fetch(url, { 
                method: 'GET',
                credentials: 'include' // 添加credentials选项，解决CORS问题
            });
            return handleResponse(response);
        }
    },
    
    // 发送POST请求
    post: async (endpoint, data = {}, isFormData = false) => {
        const url = `${API_BASE_URL}${endpoint}`;
        let body;
        const options = { method: 'POST' };
        
        // 处理请求体
        if (isFormData) {
            // 如果已经是FormData对象，直接使用
            if (data instanceof FormData) {
                body = data;
            } else {
                // 否则创建新的FormData对象
                body = new FormData();
                for (const key in data) {
                    body.append(key, data[key]);
                }
            }
        } else {
            // 使用JSON格式
            body = JSON.stringify(data);
            options.headers = {
                'Content-Type': 'application/json'
            };
        }
        
        options.body = body;
        
        // 判断是否为受保护资源
        if (isProtectedResource(url)) {
            return fetchProtectedResource(url, options);
        } else {
            const response = await fetch(url, options);
            return handleResponse(response);
        }
    }
};

// 成员管理相关API
const memberAPI = {
    // 获取所有成员信息
    getAllMembers: async () => {
        return fetchProtectedResource(`${API_BASE_URL}/api/GroupMun/all`, {
            method: 'GET'
        });
    },
    
    // 根据小组ID获取成员列表
    // 参数说明：
    // - groupId: 小组ID
    getMembersByGroupId: async (groupId) => {
        return fetchProtectedResource(`${API_BASE_URL}/api/GroupMun/list?groupId=${groupId}`, {
            method: 'GET'
        });
    },
    
    // getGroupMembers作为getMembersByGroupId的别名，保持与dashboard.js的兼容性
    getGroupMembers: async (groupId) => {
        return memberAPI.getMembersByGroupId(groupId);
    },
    
    // 获取特定成员信息
    // 参数说明：
    // - id: 成员ID
    getMemberDetail: async (id) => {
        return fetchProtectedResource(`${API_BASE_URL}/api/GroupMun/show?id=${id}`, {
            method: 'GET'
        });
    },
    
    // 添加小组成员
    // 参数说明：
    // - name: 成员姓名
    // - gender: 成员性别
    // - number: 学号
    // - work: 职务
    // - groupId: 小组ID
    addMember: async (name, gender, number, work, groupId) => {
        // 使用URLSearchParams代替FormData，确保正确处理中文字符
        const formData = new URLSearchParams();
        formData.append('name', name);
        formData.append('gender', gender);
        formData.append('number', number);
        formData.append('work', work);
        formData.append('groupId', groupId);
        
        return fetchProtectedResource(`${API_BASE_URL}/api/GroupMun/add`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8'
            },
            body: formData
        });
    },
    
    // 更新成员信息
    // 参数说明：
    // - id: 成员ID
    // - name: 成员姓名
    // - gender: 成员性别
    // - number: 学号
    // - work: 职务
    // - groupId: 小组ID
    updateMember: async (id, name, gender, number, work, groupId) => {
        // 使用URLSearchParams代替FormData，确保正确处理中文字符
        const formData = new URLSearchParams();
        formData.append('id', id);
        formData.append('name', name);
        formData.append('gender', gender);
        formData.append('number', number);
        formData.append('work', work);
        formData.append('groupId', groupId);
        
        return fetchProtectedResource(`${API_BASE_URL}/api/GroupMun/update`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8'
            },
            body: formData
        });
    },
    
    // 删除成员
    // 参数说明：
    // - id: 成员ID
    deleteMember: async (id) => {
        const formData = new FormData();
        formData.append('id', id);
        
        return fetchProtectedResource(`${API_BASE_URL}/api/GroupMun/del`, {
            method: 'POST',
            body: formData
        });
    },
    
    // 获取所有成员列表
    getAllMembersList: async () => {
        return fetchProtectedResource(`${API_BASE_URL}/api/GroupMun/all`, {
            method: 'GET'
        });
    },
    
    // 按学号升序排列成员
    sortMembersByNumberAsc: async () => {
        return fetchProtectedResource(`${API_BASE_URL}/api/GroupMun/sort1`, {
            method: 'GET'
        });
    },
    
    // 按学号降序排列成员
    sortMembersByNumberDesc: async () => {
        return fetchProtectedResource(`${API_BASE_URL}/api/GroupMun/sort2`, {
            method: 'GET'
        });
    }

};

// 将API函数设置为全局变量，使其在非模块脚本中可用
window.authAPI = authAPI;
window.groupAPI = groupAPI;
window.memberAPI = memberAPI;
window.api = api;
window.isProtectedResource = isProtectedResource;
window.fetchProtectedResource = fetchProtectedResource;
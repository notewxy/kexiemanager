// 不再需要导入，因为api.js已经将这些函数设置为全局变量

// 登录页面的主要逻辑
document.addEventListener('DOMContentLoaded', () => {
    // 获取登录表单和消息显示元素
    const loginForm = document.getElementById('loginForm');
    const messageDiv = document.getElementById('message');
    const actionButtons = document.getElementById('actionButtons');
    
    // 检查是否已登录
    checkLoginStatus();
    
    // 监听表单提交事件
    loginForm.addEventListener('submit', async (e) => {
        // 阻止表单的默认提交行为
        e.preventDefault();
        
        // 获取用户输入
        const username = document.getElementById('username').value.trim();
        const password = document.getElementById('password').value.trim();
        const rememberMe = document.getElementById('rememberMe').checked;

        // 表单验证
        if (!username || !password) {
            displayMessage('请填写用户名和密码', 'error');
            return;
        }

        try {
            // 调用登录API
            await login(username, password, rememberMe);
        } catch (error) {
            // 处理登录失败的情况
            displayMessage(error.message || '登录失败，请重试', 'error');
        }
    });
    
    // 检查登录状态
    async function checkLoginStatus() {
        const isLoggedIn = await authAPI.checkLoginStatus();
        if (isLoggedIn) {
            displayMessage('您已登录，正在跳转到仪表盘...', 'success');
            // 跳转到仪表盘页面
            setTimeout(() => {
                window.location.href = 'dashboard.html';
            }, 1000);
        } else {
            loginForm.style.display = 'block';
            actionButtons.style.display = 'none';
        }
    }
    
    // 登录函数
    async function login(username, password, rememberMe) {
        try {
            displayMessage('正在登录...', 'info');
            
            // 调用登录API
            const result = await authAPI.login(username, password, rememberMe);
            
            // 登录成功
            displayMessage('登录成功！正在跳转...', 'success');
            
            // 跳转到仪表盘页面
            setTimeout(() => {
                window.location.href = 'dashboard.html';
            }, 1000);
            
        } catch (error) {
            throw error;
        }
    }
});

// 显示消息的辅助函数
// 将函数设置为全局函数，以便其他全局函数可以调用
window.displayMessage = function(msg, type = 'info') {
    const messageDiv = document.getElementById('message');
    messageDiv.textContent = msg;
    
    // 清除所有类型
    messageDiv.classList.remove('message-success', 'message-error', 'message-info');
    
    // 添加对应类型
    messageDiv.classList.add(`message-${type}`);
}

// 获取所有小组信息
// 将函数设置为全局函数，以便在HTML中通过onclick调用
window.getAllGroups = async function() {
    displayMessage('正在获取所有小组信息...', 'info');
    try {
        const groups = await groupAPI.getAllGroups();
        // 构建表格HTML
        let tableHtml = '<table class="table table-striped table-hover">';
        tableHtml += '<thead><tr><th>编号</th><th>小组名称</th><th>操作</th></tr></thead><tbody>';
        if (groups.length === 0) {
            tableHtml += '<tr><td colspan="3" class="text-center">暂无小组数据</td></tr>';
        } else {
            groups.forEach(group => {
                tableHtml += `<tr><td>${group.id}</td><td>${group.name}</td><td><button class='btn btn-sm btn-info' onclick='alert(\'查看功能待实现\')'>查看</button></td></tr>`;
            });
        }
        tableHtml += '</tbody></table>';
        document.getElementById('groupsTableContainer').innerHTML = tableHtml;
        displayMessage(`成功获取到 ${groups.length} 个小组信息`, 'success');
    } catch (error) {
        document.getElementById('groupsTableContainer').innerHTML = '';
        displayMessage(error.message || '获取小组信息失败', 'error');
    }
};

// 获取小组成员信息（示例使用固定的groupId）
// 将函数设置为全局函数，以便在HTML中通过onclick调用
window.getGroupMembers = async function() {
    try {
        // 这里使用一个示例的groupId，实际应用中应该是动态获取的
        const groupId = 1; // 示例groupId
        
        displayMessage(`正在获取小组ID ${groupId} 的成员信息...`, 'info');
        
        const members = await groupAPI.getGroupMembers(groupId);
        
        // 显示获取到的成员信息
        displayMessage(`成功获取到 ${members.length} 个成员信息`, 'success');
        console.log('成员信息:', members);
        
    } catch (error) {
        if (error.message === '认证失败，请重新登录') {
            // 如果是认证失败，显示登录表单
            document.getElementById('loginForm').style.display = 'block';
            document.getElementById('actionButtons').style.display = 'none';
        }
        displayMessage(error.message || '获取成员信息失败', 'error');
    }
}

// 登出函数
// 将函数设置为全局函数，以便在HTML中通过onclick调用
window.logout = async function() {
    try {
        await authAPI.logout();
        
        // 显示登录表单，隐藏操作按钮
        document.getElementById('loginForm').style.display = 'block';
        document.getElementById('actionButtons').style.display = 'none';
        
        displayMessage('已成功登出', 'info');
        
    } catch (error) {
        displayMessage(error.message || '登出失败', 'error');
    }
}
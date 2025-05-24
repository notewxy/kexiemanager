
// 注册页面的主要逻辑
document.addEventListener('DOMContentLoaded', () => {
    // 获取注册表单和错误信息显示元素
    const registerForm = document.getElementById('registerForm');
    const errorMessage = document.getElementById('errorMessage');
    const usernameInput = document.getElementById('username');
    const passwordInput = document.getElementById('password');
    const confirmPasswordInput = document.getElementById('confirmPassword');

    // 监听注册表单的提交事件
    registerForm.addEventListener('submit', async (e) => {
        // 阻止表单默认提交行为
        e.preventDefault();
        
        // 获取用户输入的注册信息
        const username = usernameInput.value.trim();
        const password = passwordInput.value.trim();
        const confirmPassword = confirmPasswordInput.value.trim();

        // 表单验证部分
        // 检查是否所有必填字段都已填写
        if (!username || !password || !confirmPassword) {
            showError('请填写所有必填字段');
            return;
        }

        // 检查两次输入的密码是否一致
        if (password !== confirmPassword) {
            showError('两次输入的密码不一致');
            return;
        }

        try {
            // 调用注册API，发送注册请求
            const result = await authAPI.register(username, password);
            
            // 根据后端响应处理结果
            if (result === '注册成功') {
                // 显示注册成功信息
                alert('注册成功');
                // 注册成功，跳转到登录页面
                window.location.href = 'login.html';
            } else {
                // 这种情况不应该发生，因为如果注册失败，handleResponse应该抛出异常
                showError('注册失败，请重试');
            }
        } catch (error) {
            // 处理注册失败的情况
            if (error.message === '用户名已存在') {
                showError('用户名已存在');
            } else {
                showError(error.message || '注册失败，请重试');
            }
        }
    });

    // 显示错误信息的辅助函数
    function showError(message) {
        errorMessage.textContent = message;
        errorMessage.style.display = 'block';
        
        // 3秒后自动隐藏错误信息
        setTimeout(() => {
            errorMessage.style.display = 'none';
        }, 3000);
    }
});
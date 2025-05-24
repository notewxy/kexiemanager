// 仪表盘页面逻辑

// 全局变量和函数
let currentGroupId = null;
let groupsListPage, groupDetailPage, allMembersPage;
let groupsTableBody, groupMembersTableBody, allMembersTableBody;
let backToGroupsBtn, groupDetailTitle;
let editGroupModal, addMemberModal, editMemberModal, addAllMemberModal;

// 初始化模态框
document.addEventListener('DOMContentLoaded', () => {
    // 初始化所有模态框
    setTimeout(() => {
        editGroupModal = new bootstrap.Modal(document.getElementById('editGroupModal'));
        addMemberModal = new bootstrap.Modal(document.getElementById('addMemberModal'));
        editMemberModal = new bootstrap.Modal(document.getElementById('editMemberModal'));
        addAllMemberModal = new bootstrap.Modal(document.getElementById('addAllMemberModal'));
    }, 100);
});

// 加载小组成员
window.loadGroupMembers = async function(groupId) {
    try {
        const members = await memberAPI.getGroupMembers(groupId);
        groupMembersTableBody.innerHTML = members.map(member => `
            <tr>
                <td>${member.id}</td>
                <td>${member.name}</td>
                <td>${member.gender || '未知'}</td>
                <td>${member.number || '未知'}</td>
                <td>${member.work || '成员'}</td>
                <td class="table-actions">
                    <button class="btn btn-sm btn-warning me-2" onclick="window.editMember(${member.id}, '${member.name}', '${member.gender}', '${member.number}', '${member.work}', ${member.group_id});">编辑</button>
                    <button class="btn btn-sm btn-danger" onclick="window.deleteMember(${member.id});">删除</button>
                </td>
            </tr>
        `).join('');
    } catch (error) {
        alert('加载成员列表失败：' + error.message);
        // 认证失败时，直接跳转到登录页面
        if (error.message.includes('认证失败') || error.message.includes('请先登录')) {
            window.location.href = 'index.html';
        }
    }
};

// 显示小组详情
window.showGroupDetail = async function(groupId, groupName) {
    currentGroupId = groupId;
    groupDetailTitle.textContent = `${groupName} - 成员管理`;
    groupsListPage.style.display = 'none';
    groupDetailPage.style.display = 'block';
    
    // 获取小组详情信息，包括介绍
    try {
        const groupDetail = await groupAPI.getGroupDetail(groupId);
        // 更新小组介绍文本
        const groupIntroduceText = document.getElementById('groupIntroduceText');
        if (groupDetail && groupDetail.introduce) {
            groupIntroduceText.textContent = groupDetail.introduce;
        } else {
            groupIntroduceText.textContent = '暂无介绍';
        }
    } catch (error) {
        console.error('获取小组详情失败:', error);
        document.getElementById('groupIntroduceText').textContent = '获取小组介绍失败';
        // 认证失败时，直接跳转到登录页面
        if (error.message && (error.message.includes('认证失败') || error.message.includes('请先登录'))) {
            alert('认证失败，即将跳转到登录页面');
            window.location.href = 'index.html';
        }
    }
    
    // 加载小组成员
    window.loadGroupMembers(groupId);
};

// 编辑小组函数
window.editGroup = async function(groupId, oldName, oldIntroduce) {
    // 确保模态框已初始化
    if (!editGroupModal) {
        editGroupModal = new bootstrap.Modal(document.getElementById('editGroupModal'));
    }
    
    // 填充表单
    document.getElementById('editGroupId').value = groupId;
    document.getElementById('editGroupName').value = oldName;
    document.getElementById('editGroupIntroduce').value = oldIntroduce || '';
    
    // 显示模态框
    editGroupModal.show();
};

// 删除小组函数
window.deleteGroup = async function(groupId) {
    if (confirm('确定要删除这个小组吗？删除小组将同时删除该小组的所有成员！')) {
        try {
            // 获取当前点击的删除按钮
            const deleteBtn = event.target;
            const originalText = deleteBtn.innerHTML;
            deleteBtn.innerHTML = '删除中...';
            deleteBtn.disabled = true;
            
            // 等待删除操作完成
            await groupAPI.deleteGroup(groupId);
            
            // 获取包含该小组的表格行
            const row = deleteBtn.closest('tr');
            
            // 立即从DOM中移除该行，提供即时的视觉反馈
            if (row) {
                row.style.transition = 'opacity 0.3s';
                row.style.opacity = '0';
                
                // 使用setTimeout确保过渡效果完成后再移除元素
                setTimeout(() => {
                    row.remove();
                }, 300);
            }
            
            // 如果当前正在查看被删除的小组，则返回小组列表
            if (currentGroupId === groupId) {
                backToGroupsBtn.click();
            }
            
            // 删除成功，不需要额外的alert，避免多次提示
            // 也不需要重新加载整个表格，因为我们已经移除了对应的行
            console.log('删除成功');
        } catch (error) {
            alert('删除小组失败：' + error.message);
        } finally {
            // 无论成功或失败，都恢复按钮状态
            if (event && event.target) {
                event.target.innerHTML = '删除';
                event.target.disabled = false;
            }
        }
    }
};

// 编辑成员函数
window.editMember = async function(memberId, oldName, oldGender, oldNumber, oldWork, groupId) {
    // 确保模态框已初始化
    if (!editMemberModal) {
        editMemberModal = new bootstrap.Modal(document.getElementById('editMemberModal'));
    }
    
    // 填充表单
    document.getElementById('editMemberId').value = memberId;
    document.getElementById('editMemberName').value = oldName;
    document.getElementById('editMemberGender').value = oldGender;
    document.getElementById('editMemberNumber').value = oldNumber || '';
    document.getElementById('editMemberWork').value = oldWork || '';
    
    // 如果在小组详情页面中编辑成员，优先使用当前小组ID
    if (currentGroupId && groupDetailPage.style.display === 'block') {
        document.getElementById('editMemberGroupId').value = currentGroupId;
    } else {
        // 确保在所有成员页面编辑时，显示成员原本的组别
        document.getElementById('editMemberGroupId').value = groupId || '';
    }
    
    // 显示模态框
    editMemberModal.show();
};

// 删除成员函数
window.deleteMember = async function(memberId) {
    if (confirm('确定要删除这个成员吗？')) {
        try {
            // 获取当前点击的删除按钮
            const deleteBtn = event.target;
            const originalText = deleteBtn.innerHTML;
            deleteBtn.innerHTML = '删除中...';
            deleteBtn.disabled = true;
            
            // 等待删除操作完成
            await memberAPI.deleteMember(memberId);
            
            // 获取包含该成员的表格行
            const row = deleteBtn.closest('tr');
            
            // 立即从DOM中移除该行，提供即时的视觉反馈
            if (row) {
                row.style.transition = 'opacity 0.3s';
                row.style.opacity = '0';
                
                // 使用setTimeout确保过渡效果完成后再移除元素
                setTimeout(() => {
                    row.remove();
                }, 300);
            }
            
            // 删除成功，不需要额外的alert，避免多次提示
            // 也不需要重新加载整个表格，因为我们已经移除了对应的行
        } catch (error) {
            alert('删除成员失败：' + error.message);
        } finally {
            // 无论成功或失败，都恢复按钮状态
            if (event && event.target) {
                event.target.innerHTML = '删除';
                event.target.disabled = false;
            }
        }
    }
};

// 页面加载完成后执行
document.addEventListener('DOMContentLoaded', () => {
    // 获取页面元素
    const logoutBtn = document.getElementById('logoutBtn');
    groupsListPage = document.getElementById('groupsListPage');
    groupDetailPage = document.getElementById('groupDetailPage');
    allMembersPage = document.getElementById('allMembersPage');
    groupsTableBody = document.getElementById('groupsTableBody');
    groupMembersTableBody = document.getElementById('groupMembersTableBody');
    allMembersTableBody = document.getElementById('allMembersTableBody');
    backToGroupsBtn = document.getElementById('backToGroups');
    groupDetailTitle = document.getElementById('groupDetailTitle');
    const navLinks = document.querySelectorAll('.nav-link[data-page]');

    // 页面切换逻辑
    navLinks.forEach(link => {
        link.addEventListener('click', (e) => {
            e.preventDefault();
            const page = e.target.dataset.page;
            
            // 更新导航栏激活状态
            navLinks.forEach(l => l.classList.remove('active'));
            e.target.classList.add('active');

            // 隐藏所有页面
            groupsListPage.style.display = 'none';
            groupDetailPage.style.display = 'none';
            allMembersPage.style.display = 'none';

            // 显示选中的页面
            if (page === 'groups') {
                groupsListPage.style.display = 'block';
                loadGroups();
            } else if (page === 'members') {
                allMembersPage.style.display = 'block';
                window.loadAllMembers();
            }
        });
    });

    // 退出登录
    logoutBtn.addEventListener('click', (e) => {
        e.preventDefault();
        // 不再需要清除本地存储，直接跳转到首页
        window.location.href = 'index.html';
    });

    // 返回小组列表
    backToGroupsBtn.addEventListener('click', () => {
        groupDetailPage.style.display = 'none';
        groupsListPage.style.display = 'block';
        currentGroupId = null;
    });

    // 加载小组列表
    window.loadGroups = async function() {
        try {
            const groups = await groupAPI.getAllGroups();
            groupsTableBody.innerHTML = groups.map(group => `
                <tr>
                    <td>${group.id}</td>
                    <td>
                        <span class="group-name" onclick="window.showGroupDetail(${group.id}, '${group.name}')">
                            ${group.name}
                        </span>
                    </td>
                    <td>${group.create_time || '未知'}</td>
                    <td class="table-actions">
                        <button class="btn btn-sm btn-warning me-2" onclick="window.editGroup(${group.id}, '${group.name}', '${group.introduce || ''}');">编辑</button>
                        <button class="btn btn-sm btn-danger" onclick="window.deleteGroup(${group.id});">删除</button>
                    </td>
                </tr>
            `).join('');
        } catch (error) {
            alert('加载小组列表失败：' + error.message);
            // 认证失败时，直接跳转到登录页面
            if (error.message.includes('认证失败') || error.message.includes('请先登录')) {
                window.location.href = 'index.html';
            }
        }
    }

    

    // 加载所有成员
    window.loadAllMembers = async function() {
        try {
            const members = await memberAPI.getAllMembers();
            const groups = await groupAPI.getAllGroups();
            const groupMap = new Map(groups.map(g => [g.id, g.name]));
            
            allMembersTableBody.innerHTML = members.map(member => `
                <tr>
                    <td>${member.id}</td>
                    <td>${member.name}</td>
                    <td>${groupMap.get(parseInt(member.group_id)) || '未知'}</td>
                    <td>${member.gender || '未知'}</td>
                    <td>${member.number || '未知'}</td>
                    <td>${member.work || '成员'}</td>
                    <td class="table-actions">
                        <button class="btn btn-sm btn-warning me-2" onclick="window.editMember(${member.id}, '${member.name}', '${member.gender}', '${member.number}', '${member.work}', ${member.group_id});">编辑</button>
                        <button class="btn btn-sm btn-danger" onclick="window.deleteMember(${member.id});">删除</button>
                    </td>
                </tr>
            `).join('');
        } catch (error) {
            alert('加载成员列表失败：' + error.message);
            // 认证失败时，直接跳转到登录页面
            if (error.message.includes('认证失败') || error.message.includes('请先登录')) {
                window.location.href = 'index.html';
            }
        }
    };
    
    // 按学号升序排列所有成员
    window.loadAllMembersSortedAsc = async function() {
        try {
            const members = await memberAPI.sortMembersByNumberAsc();
            const groups = await groupAPI.getAllGroups();
            const groupMap = new Map(groups.map(g => [g.id, g.name]));
            
            allMembersTableBody.innerHTML = members.map(member => `
                <tr>
                    <td>${member.id}</td>
                    <td>${member.name}</td>
                    <td>${groupMap.get(parseInt(member.group_id)) || '未知'}</td>
                    <td>${member.gender || '未知'}</td>
                    <td>${member.number || '未知'}</td>
                    <td>${member.work || '成员'}</td>
                    <td class="table-actions">
                        <button class="btn btn-sm btn-warning me-2" onclick="window.editMember(${member.id}, '${member.name}', '${member.gender}', '${member.number}', '${member.work}', ${member.group_id});">编辑</button>
                        <button class="btn btn-sm btn-danger" onclick="window.deleteMember(${member.id});">删除</button>
                    </td>
                </tr>
            `).join('');
        } catch (error) {
            alert('加载排序成员列表失败：' + error.message);
            // 认证失败时，直接跳转到登录页面
            if (error.message.includes('认证失败') || error.message.includes('请先登录')) {
                window.location.href = 'index.html';
            }
        }
    };
    
    // 按学号降序排列所有成员
    window.loadAllMembersSortedDesc = async function() {
        try {
            const members = await memberAPI.sortMembersByNumberDesc();
            const groups = await groupAPI.getAllGroups();
            const groupMap = new Map(groups.map(g => [g.id, g.name]));
            
            allMembersTableBody.innerHTML = members.map(member => `
                <tr>
                    <td>${member.id}</td>
                    <td>${member.name}</td>
                    <td>${groupMap.get(parseInt(member.group_id)) || '未知'}</td>
                    <td>${member.gender || '未知'}</td>
                    <td>${member.number || '未知'}</td>
                    <td>${member.work || '成员'}</td>
                    <td class="table-actions">
                        <button class="btn btn-sm btn-warning me-2" onclick="window.editMember(${member.id}, '${member.name}', '${member.gender}', '${member.number}', '${member.work}', ${member.group_id});">编辑</button>
                        <button class="btn btn-sm btn-danger" onclick="window.deleteMember(${member.id});">删除</button>
                    </td>
                </tr>
            `).join('');
        } catch (error) {
            alert('加载排序成员列表失败：' + error.message);
            // 认证失败时，直接跳转到登录页面
            if (error.message.includes('认证失败') || error.message.includes('请先登录')) {
                window.location.href = 'index.html';
            }
        }
    };

    // 排序按钮事件监听
    document.getElementById('sortOriginal').addEventListener('click', () => {
        window.loadAllMembers();
    });
    
    document.getElementById('sortAsc').addEventListener('click', () => {
        window.loadAllMembersSortedAsc();
    });
    
    document.getElementById('sortDesc').addEventListener('click', () => {
        window.loadAllMembersSortedDesc();
    });
    
    // 添加小组
    // 确保在DOM加载完成后初始化模态框
    setTimeout(() => {
        const addGroupModal = new bootstrap.Modal(document.getElementById('addGroupModal'));
        
        // 获取取消按钮并添加事件监听器
        const cancelAddGroupBtn = document.querySelector('#addGroupModal .btn-outline-secondary');
        if (cancelAddGroupBtn) {
            cancelAddGroupBtn.addEventListener('click', () => {
                // 重置表单
                document.getElementById('groupName').value = '';
                document.getElementById('groupIntroduce').value = '';
            });
        }
        
        // 确保submitAddGroup按钮存在
        const submitAddGroupBtn = document.getElementById('submitAddGroup');
        if (submitAddGroupBtn) {
            submitAddGroupBtn.addEventListener('click', async () => {
                const groupName = document.getElementById('groupName').value.trim();
                const groupIntroduce = document.getElementById('groupIntroduce').value.trim();
                
                if (!groupName) {
                    alert('请输入小组名称');
                    return;
                }

                try {
                    await groupAPI.addGroup(groupName, groupIntroduce);
                    addGroupModal.hide();
                    document.getElementById('groupName').value = '';
                    document.getElementById('groupIntroduce').value = '';
                    loadGroups();
                } catch (error) {
                    alert('添加小组失败：' + error.message);
                }
            });
        }
        
        // 阻止模态框关闭时自动提交
        const addGroupForm = document.getElementById('addGroupForm');
        if (addGroupForm) {
            addGroupForm.addEventListener('submit', (e) => {
                e.preventDefault();
            });
        }
    }, 500); // 给予足够的时间确保DOM完全加载

    // 初始化页面 - 默认加载小组列表
    loadGroups();

    // 添加成员
    document.getElementById('submitAddMember').addEventListener('click', async () => {
        const memberName = document.getElementById('memberName').value.trim();
        const memberGender = document.getElementById('memberGender').value;
        const memberNumber = document.getElementById('memberNumber').value.trim();
        const memberWork = document.getElementById('memberWork').value.trim();

        if (!memberName || !currentGroupId) {
            alert('请填写完整信息');
            return;
        }

        try {
            await memberAPI.addMember(memberName, memberGender, memberNumber, memberWork || '成员', currentGroupId);
            addMemberModal.hide();
            // 重置表单
            document.getElementById('memberName').value = '';
            document.getElementById('memberGender').value = '男';
            document.getElementById('memberNumber').value = '';
            document.getElementById('memberWork').value = '成员';
            window.loadGroupMembers(currentGroupId);
        } catch (error) {
            alert('添加成员失败：' + error.message);
        }
    });

    // 编辑小组 - 使用模态框代替prompt
    
    // 获取取消按钮并添加事件监听器
    const cancelEditGroupBtn = document.querySelector('#editGroupModal .btn-outline-secondary');
    if (cancelEditGroupBtn) {
        cancelEditGroupBtn.addEventListener('click', () => {
            // 重置表单
            document.getElementById('editGroupId').value = '';
            document.getElementById('editGroupName').value = '';
            document.getElementById('editGroupIntroduce').value = '';
        });
    }
    
    // 阻止模态框表单提交
    document.getElementById('editGroupForm').addEventListener('submit', (e) => {
        e.preventDefault();
    });
    
    // 处理编辑小组提交
    document.getElementById('submitEditGroup').addEventListener('click', async () => {
        const groupId = document.getElementById('editGroupId').value;
        const groupName = document.getElementById('editGroupName').value.trim();
        const groupIntroduce = document.getElementById('editGroupIntroduce').value.trim();
        
        if (!groupName) {
            alert('请输入小组名称');
            return;
        }

        try {
            await groupAPI.updateGroup(groupId, groupName, groupIntroduce);
            editGroupModal.hide();
            loadGroups();
        } catch (error) {
            alert('编辑小组失败：' + error.message);
        }
    });

    // 编辑成员 - 使用模态框代替prompt
    
    // 获取取消按钮并添加事件监听器
    const cancelEditMemberBtn = document.querySelector('#editMemberModal .btn-outline-secondary');
    if (cancelEditMemberBtn) {
        cancelEditMemberBtn.addEventListener('click', () => {
            // 重置表单
            document.getElementById('editMemberId').value = '';
            document.getElementById('editMemberName').value = '';
            document.getElementById('editMemberGender').value = '男';
            document.getElementById('editMemberNumber').value = '';
            document.getElementById('editMemberWork').value = '';
            document.getElementById('editMemberGroupId').value = '';
        });
    }
    
    // 阻止模态框表单提交
    document.getElementById('editMemberForm').addEventListener('submit', (e) => {
        e.preventDefault();
    });
    
    // 处理编辑成员提交
    document.getElementById('submitEditMember').addEventListener('click', async () => {
        const memberId = document.getElementById('editMemberId').value;
        const memberName = document.getElementById('editMemberName').value.trim();
        const memberGender = document.getElementById('editMemberGender').value;
        const memberNumber = document.getElementById('editMemberNumber').value.trim();
        const memberWork = document.getElementById('editMemberWork').value.trim();
        let groupId = document.getElementById('editMemberGroupId').value;
        
        // 如果groupId为空或未定义，使用当前选中的小组ID
        if (!groupId && currentGroupId) {
            groupId = currentGroupId;
        }
        
        if (!memberName) {
            alert('请输入成员姓名');
            return;
        }

        try {
            // 更新成员信息
            await memberAPI.updateMember(memberId, memberName, memberGender, memberNumber, memberWork, groupId);
            
            editMemberModal.hide();
            
            if (currentGroupId) {
                window.loadGroupMembers(currentGroupId);
            } else {
                window.loadAllMembers();
            }
        } catch (error) {
            alert('编辑成员失败：' + error.message);
        }
    });

    // 添加所有成员模态框处理
    
    // 获取取消按钮并添加事件监听器
    const cancelAddAllMemberBtn = document.querySelector('#addAllMemberModal .btn-outline-secondary');
    if (cancelAddAllMemberBtn) {
        cancelAddAllMemberBtn.addEventListener('click', () => {
            // 重置表单
            document.getElementById('allMemberName').value = '';
            document.getElementById('allMemberGender').value = '男';
            document.getElementById('allMemberNumber').value = '';
            document.getElementById('allMemberWork').value = '成员';
            document.getElementById('allMemberGroupId').value = '';
        });
    }
    
    // 阻止模态框表单提交
    document.getElementById('addAllMemberForm').addEventListener('submit', (e) => {
        e.preventDefault();
    });
    
    // 处理添加所有成员提交
    document.getElementById('submitAddAllMember').addEventListener('click', async () => {
        const memberName = document.getElementById('allMemberName').value.trim();
        const memberGender = document.getElementById('allMemberGender').value;
        const memberNumber = document.getElementById('allMemberNumber').value.trim();
        const memberWork = document.getElementById('allMemberWork').value.trim();
        const groupId = document.getElementById('allMemberGroupId').value.trim();
        
        if (!memberName) {
            alert('请填写成员姓名');
            return;
        }
        
        if (!groupId) {
            alert('请填写小组编号');
            return;
        }
        
        // 验证小组编号是否为数字
        if (isNaN(parseInt(groupId))) {
            alert('小组编号必须是数字');
            return;
        }
        
        try {
            // 先验证小组是否存在
            /*
            try {
                await groupAPI.getGroupDetail(groupId);
            } catch (error) {
                alert('小组编号不存在，请检查后重新输入');
                return;
            }
            */
            
            // 添加成员 - 使用正确的API调用
            await memberAPI.addMember(memberName, memberGender, memberNumber, memberWork || '成员', groupId);
            addAllMemberModal.hide();
            // 重置表单
            document.getElementById('allMemberName').value = '';
            document.getElementById('allMemberGender').value = '男';
            document.getElementById('allMemberNumber').value = '';
            document.getElementById('allMemberWork').value = '成员';
            document.getElementById('allMemberGroupId').value = '';
            // 重新加载所有成员列表
            window.loadAllMembers();
        } catch (error) {
            alert('添加成员失败：' + error.message);
        }
    });
    
    // 初始加载小组列表
    loadGroups();
});
// 所有成员管理页面脚本
// 依赖：api.js

document.addEventListener('DOMContentLoaded', function () {
    const memberTableBody = document.getElementById('memberTableBody');
    const sortAscBtn = document.getElementById('sortAscBtn');
    const sortDescBtn = document.getElementById('sortDescBtn');
    const sortOrigBtn = document.getElementById('sortOrigBtn');
    const addMemberModal = new bootstrap.Modal(document.getElementById('addMemberModal'));
    const editMemberModal = new bootstrap.Modal(document.getElementById('editMemberModal'));
    const deleteMemberModal = new bootstrap.Modal(document.getElementById('deleteMemberModal'));
    const addMemberForm = document.getElementById('addMemberForm');
    const editMemberForm = document.getElementById('editMemberForm');
    const confirmAddMember = document.getElementById('confirmAddMember');
    const confirmEditMember = document.getElementById('confirmEditMember');
    const confirmDeleteMember = document.getElementById('confirmDeleteMember');
    const messageDiv = document.getElementById('message');
    let members = [];
    let originalMembers = [];
    let groups = [];
    let deleteMemberId = null;

    // 显示消息
    function displayMessage(msg, type = 'success') {
        messageDiv.textContent = msg;
        messageDiv.className = '';
        if (type === 'success') messageDiv.classList.add('message-success');
        else if (type === 'error') messageDiv.classList.add('message-error');
        else messageDiv.classList.add('message-info');
        setTimeout(() => { messageDiv.textContent = ''; }, 2500);
    }

    // 渲染成员表格
    function renderTable(data) {
        memberTableBody.innerHTML = '';
        data.forEach((member, idx) => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${idx + 1}</td>
                <td>${member.name}</td>
                <td>${member.gender}</td>
                <td>${member.number}</td>
                <td>${member.work}</td>
                <td>${member.group_id}</td>
                <td class="action-buttons">
                    <button class="btn btn-sm btn-warning edit-member" data-id="${member.id}">修改</button>
                    <button class="btn btn-sm btn-danger delete-member" data-id="${member.id}">删除</button>
                </td>
            `;
            memberTableBody.appendChild(row);
        });
    }

    // 获取所有小组
    async function fetchGroups() {
        try {
            const res = await fetchProtectedResource('/api/AllGroups/list', { method: 'GET' });
            groups = res.data || [];
            // 填充添加/修改成员的小组编号下拉框
            const groupSelects = [document.getElementById('memberGroupId'), document.getElementById('editMemberGroupId')];
            groupSelects.forEach(select => {
                if (!select) return;
                select.innerHTML = '';
                groups.forEach(g => {
                    const option = document.createElement('option');
                    option.value = g.id;
                    option.textContent = g.id;
                    select.appendChild(option);
                });
            });
        } catch (e) {
            displayMessage('获取小组列表失败', 'error');
        }
    }

    // 获取所有成员
    async function fetchMembers() {
        try {
            const res = await fetchProtectedResource('/api/GroupMun/list', { method: 'GET' });
            members = res.data || [];
            originalMembers = [...members];
            renderTable(members);
        } catch (e) {
            displayMessage('获取成员列表失败', 'error');
        }
    }

    // 排序
    sortAscBtn.addEventListener('click', function () {
        members.sort((a, b) => a.number.localeCompare(b.number));
        renderTable(members);
    });
    sortDescBtn.addEventListener('click', function () {
        members.sort((a, b) => b.number.localeCompare(a.number));
        renderTable(members);
    });
    sortOrigBtn.addEventListener('click', function () {
        members = [...originalMembers];
        renderTable(members);
    });

    // 添加成员
    confirmAddMember.addEventListener('click', async function () {
        const name = document.getElementById('memberName').value.trim();
        const gender = document.getElementById('memberGender').value;
        const number = document.getElementById('memberNumber').value.trim();
        const work = document.getElementById('memberWork').value.trim();
        const group_id = document.getElementById('memberGroupId').value;
        if (!name || !gender || !number || !work || !group_id) {
            displayMessage('请填写完整信息', 'error');
            return;
        }
        try {
            await fetchProtectedResource('/api/GroupMun/add', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ name, gender, number, work, group_id })
            });
            addMemberModal.hide();
            displayMessage('成员添加成功', 'success');
            fetchMembers();
            addMemberForm.reset();
        } catch (e) {
            displayMessage('添加成员失败', 'error');
        }
    });

    // 编辑成员按钮事件
    memberTableBody.addEventListener('click', function (e) {
        if (e.target.classList.contains('edit-member')) {
            const id = e.target.getAttribute('data-id');
            const member = members.find(m => m.id == id);
            if (!member) return;
            document.getElementById('editMemberId').value = member.id;
            document.getElementById('editMemberName').value = member.name;
            document.getElementById('editMemberGender').value = member.gender;
            document.getElementById('editMemberNumber').value = member.number;
            document.getElementById('editMemberWork').value = member.work;
            document.getElementById('editMemberGroupId').value = member.group_id;
            editMemberModal.show();
        } else if (e.target.classList.contains('delete-member')) {
            deleteMemberId = e.target.getAttribute('data-id');
            deleteMemberModal.show();
        }
    });

    // 确认修改成员
    confirmEditMember.addEventListener('click', async function () {
        const id = document.getElementById('editMemberId').value;
        const name = document.getElementById('editMemberName').value.trim();
        const gender = document.getElementById('editMemberGender').value;
        const number = document.getElementById('editMemberNumber').value.trim();
        const work = document.getElementById('editMemberWork').value.trim();
        const group_id = document.getElementById('editMemberGroupId').value;
        if (!name || !gender || !number || !work || !group_id) {
            displayMessage('请填写完整信息', 'error');
            return;
        }
        try {
            await fetchProtectedResource('/api/GroupMun/update', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ id, name, gender, number, work, group_id })
            });
            editMemberModal.hide();
            displayMessage('成员修改成功', 'success');
            fetchMembers();
        } catch (e) {
            displayMessage('修改成员失败', 'error');
        }
    });

    // 确认删除成员
    confirmDeleteMember.addEventListener('click', async function () {
        if (!deleteMemberId) return;
        try {
            await fetchProtectedResource('/api/GroupMun/delete', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ id: deleteMemberId })
            });
            deleteMemberModal.hide();
            displayMessage('成员删除成功', 'success');
            fetchMembers();
        } catch (e) {
            displayMessage('删除成员失败', 'error');
        }
    });

    // 初始化
    fetchGroups();
    fetchMembers();
});
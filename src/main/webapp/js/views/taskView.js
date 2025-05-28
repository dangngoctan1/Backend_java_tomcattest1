function renderTasksListUI(tasks, containerId) {
    console.log('Hàm renderTasksListUI được gọi');
    containerId = containerId || "tasksList";
    var container = document.getElementById(containerId);
    if (!container) return;

    if (!tasks || tasks.length === 0) {
        container.innerHTML = '<div class="no-tasks"><p>Không có công việc nào. Hãy thêm mới!</p></div>';
        return;
    }

    var html = "";
    for (var i = 0; i < tasks.length; i++) {
        var task = tasks[i];
        var dueState = getTaskDueState(task);
        var dueClass = dueState === "overdue" ? "due-date-overdue" :
                       dueState === "soon" ? "due-date-soon" : "";

        html += '<div class="task-item" data-task-id="' + task.id + '">';
        html += '  <div class="task-title">';
        html += '    <h4>' + escapeHtml(task.title) + '</h4>';
        if (task.description) {
            html += '    <p class="task-description">' + escapeHtml(task.description) + '</p>';
        }
        html += '  </div>';
        html += '  <div class="task-priority priority-' + task.priority + '">';
        html += '    <span class="priority-badge">' + getPriorityLabel(task.priority) + '</span>';
        html += '  </div>';
        html += '  <div class="task-dates">';
        html += '    <div class="created-date">Ngày tạo: ' + formatDateTime(task.createdDate) + '</div>';
        html += '    <div class="due-date ' + dueClass + '">Ngày hết hạn: ';
        if (task.dueDate) {
            html += formatDate(task.dueDate);
        } else {
            html += '<span class="no-due-date">Không có</span>';
        }
        html += '</div>';
        html += '  </div>';
        html += '  <div class="task-status">';
        html += '    <select class="status-select" data-task-id="' + task.id + '">';
        html += '      <option value="new"' + (task.status === 'new' ? ' selected' : '') + '>Mới</option>';
        html += '      <option value="in-progress"' + (task.status === 'in-progress' ? ' selected' : '') + '>Đang thực hiện</option>';
        html += '      <option value="completed"' + (task.status === 'completed' ? ' selected' : '') + '>Hoàn thành</option>';
        html += '    </select>';
        html += '  </div>';
        html += '  <div class="task-actions">';
        html += '    <button class="action-btn edit-btn" data-edit="' + task.id + '">✏️</button>';
        html += '    <button class="action-btn delete-btn" data-delete="' + task.id + '">🗑️</button>';
        html += '  </div>';
        html += '</div>';
    }

    container.innerHTML = html;
}

window.renderTasksListUI = renderTasksListUI;
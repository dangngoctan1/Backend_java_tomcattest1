// taskView.js - chỉ xử lý UI rendering (không gọi API trực tiếp)

import { formatDate, formatDateTime, escapeHtml, getPriorityLabel, getTaskDueState } from "../utils/helpers.js";

export function renderTasksListUI(tasks, containerId = "tasksList") {
  const container = document.getElementById(containerId);
  if (!container) return;

  if (!tasks || tasks.length === 0) {
    container.innerHTML = `<div class="no-tasks"><p>Không có công việc nào. Hãy thêm mới!</p></div>`;
    return;
  }

  let html = tasks.map(task => {
    const dueState = getTaskDueState(task);
    const dueClass = dueState === "overdue" ? "due-date-overdue" :
                     dueState === "soon" ? "due-date-soon" : "";

    return `
      <div class="task-item" data-task-id="${task.id}">
        <div class="task-title">
          <h4>${escapeHtml(task.title)}</h4>
          ${task.description ? `<p class="task-description">${escapeHtml(task.description)}</p>` : ''}
        </div>
        <div class="task-priority priority-${task.priority}">
          <span class="priority-badge">${getPriorityLabel(task.priority)}</span>
        </div>
        <div class="task-dates">
          <div class="created-date">Ngày tạo: ${formatDateTime(task.createdDate)}</div>
          <div class="due-date ${dueClass}">Ngày hết hạn: ${task.dueDate ? formatDate(task.dueDate) : '<span class="no-due-date">Không có</span>'}</div>
        </div>
        <div class="task-status">
          <select class="status-select" data-task-id="${task.id}">
            <option value="new"${task.status === 'new' ? ' selected' : ''}>Mới</option>
            <option value="in-progress"${task.status === 'in-progress' ? ' selected' : ''}>Đang thực hiện</option>
            <option value="completed"${task.status === 'completed' ? ' selected' : ''}>Hoàn thành</option>
          </select>
        </div>
        <div class="task-actions">
          <button class="action-btn edit-btn" data-edit="${task.id}">✏️</button>
          <button class="action-btn delete-btn" data-delete="${task.id}">🗑️</button>
        </div>
      </div>
    `;
  }).join("");

  container.innerHTML = html;
}

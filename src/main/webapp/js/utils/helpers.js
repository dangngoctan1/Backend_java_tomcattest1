function escapeHtml(text) {
    console.log('Hàm escapeHtml được gọi');
    const div = document.createElement("div");
    div.textContent = text;
    return div.innerHTML;
}

function formatDate(date) {
    console.log('Hàm formatDate được gọi');
    if (!date) return "";
    return new Date(date).toLocaleDateString("vi-VN", { day: '2-digit', month: '2-digit', year: 'numeric' });
}

function formatDateTime(date) {
    console.log('Hàm formatDateTime được gọi');
    if (!date) return "";
    return new Date(date).toLocaleString("vi-VN", { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' });
}

function getPriorityLabel(priority) {
    console.log('Hàm getPriorityLabel được gọi');
    switch (priority) {
        case "high": return "Cao";
        case "medium": return "Trung bình";
        case "low": return "Thấp";
        default: return priority;
    }
}

function getTaskDueState(task) {
    console.log('Hàm getTaskDueState được gọi');
    if (!task.dueDate) return "none";
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const due = new Date(task.dueDate);
    due.setHours(0, 0, 0, 0);
    const diff = (due - today) / (1000 * 60 * 60 * 24);
    if (diff < 0) return "overdue";
    if (diff <= 2) return "soon";
    return "normal";
}

window.escapeHtml = escapeHtml;
window.formatDate = formatDate;
window.formatDateTime = formatDateTime;
window.getPriorityLabel = getPriorityLabel;
window.getTaskDueState = getTaskDueState;
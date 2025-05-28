function loadAndRenderTasks(filters) {
    console.log('Loading and rendering tasks with filters:', filters);
    getFilteredTasks(filters.status, filters.priority, filters.sortBy, function (tasks) {
        console.log('Tasks received:', tasks);
        renderTasksListUI(tasks);
    });
}

function attachTaskEventHandlers() {
    console.log('Attaching task event handlers');
    document.addEventListener("change", function (e) {
        if (e.target.classList.contains("status-select")) {
            var taskId = e.target.getAttribute("data-task-id");
            var newStatus = e.target.value;
            console.log('Status select changed for task ID:', taskId, 'to:', newStatus);
            updateTaskStatus(taskId, newStatus, function (success) {
                if (!success) {
                    console.log('Failed to update task status');
                    alert("Cập nhật trạng thái thất bại!");
                } else {
                    console.log('Task status updated successfully');
                    var filters = getCurrentFilters();
                    loadAndRenderTasks(filters);
                }
            });
        }
    });

    document.addEventListener("click", function (e) {
        if (e.target.matches("[data-delete]")) {
            var taskId = e.target.getAttribute("data-delete");
            console.log('Delete button clicked for task ID:', taskId);
            if (confirm("Bạn chắc chắn xóa?")) {
                deleteTask(taskId, function (success) {
                    if (success) {
                        console.log('Task deleted successfully');
                        var filters = getCurrentFilters();
                        loadAndRenderTasks(filters);
                    } else {
                        console.log('Failed to delete task');
                        alert("Xóa task thất bại!");
                    }
                });
            }
        }

        if (e.target.matches("[data-edit]")) {
            var taskId = e.target.getAttribute("data-edit");
            console.log('Edit button clicked for task ID:', taskId);
            editTask(taskId);
        }
    });
}

function editTask(taskId) {
    console.log('editTask called with ID:', taskId);
    getTaskById(taskId, function(task) {
        if (task) {
            console.log('Task data retrieved:', task);
            document.getElementById("modalTitle").textContent = "Chỉnh sửa công việc";
            document.getElementById("taskTitle").value = task.title;
            document.getElementById("taskDescription").value = task.description || "";
            document.getElementById("taskPriority").value = task.priority;

            if (task.dueDate) {
                var date = new Date(task.dueDate);
                var dateStr = date.getFullYear() + '-' +
                             String(date.getMonth() + 1).padStart(2, '0') + '-' +
                             String(date.getDate()).padStart(2, '0');
                document.getElementById("taskDueDate").value = dateStr;
            } else {
                document.getElementById("taskDueDate").value = "";
            }

            document.getElementById("taskIdInput").value = taskId;
            console.log('Set taskIdInput to:', taskId);
            document.getElementById("taskModal").style.display = "block";
        } else {
            console.log('Failed to retrieve task data for ID:', taskId);
        }
    });
}

function getCurrentFilters() {
    console.log('Getting current filters');
    return {
        status: document.getElementById("statusFilter").value,
        priority: document.getElementById("priorityFilter").value,
        sortBy: document.getElementById("sortBy").value
    };
}

window.loadAndRenderTasks = loadAndRenderTasks;
window.attachTaskEventHandlers = attachTaskEventHandlers;
window.editTask = editTask;
window.getCurrentFilters = getCurrentFilters;
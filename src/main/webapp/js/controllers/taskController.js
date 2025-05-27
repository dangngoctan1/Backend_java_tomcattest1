// taskController.js - điều khiển chức năng và luồng thao tác
import { getFilteredTasks, updateTaskStatus, deleteTask } from "../api/taskService.js";
import { renderTasksListUI } from "../views/taskView.js";

export function loadAndRenderTasks(filters) {
  getFilteredTasks(filters.status, filters.priority, filters.sortBy, function(tasks) {
    renderTasksListUI(tasks);
  });
}

export function attachTaskEventHandlers() {
  document.addEventListener("change", function(e) {
    if (e.target.classList.contains("status-select")) {
      const taskId = e.target.getAttribute("data-task-id");
      const newStatus = e.target.value;
      updateTaskStatus(taskId, newStatus, function(success) {
        if (!success) alert("Cập nhật trạng thái thất bại!");
        else loadAndRenderTasks(defaultFilters);
      });
    }
  });

  document.addEventListener("click", function(e) {
    if (e.target.matches("[data-delete]")) {
      const taskId = e.target.getAttribute("data-delete");
      if (confirm("Bạn chắc chắn xóa?")) {
        deleteTask(taskId, function(success) {
          if (success) loadAndRenderTasks(defaultFilters);
        });
      }
    }
  });
}

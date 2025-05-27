// main.js - khởi tạo app
import { isLoggedIn, getCurrentUser } from "./auth/auth.js";
import { loadAndRenderTasks, attachTaskEventHandlers } from "./controllers/taskController.js";

const defaultFilters = {
  status: "all",
  priority: "all",
  sortBy: "createdDate"
};

document.addEventListener("DOMContentLoaded", function () {
  if (!isLoggedIn()) {
    window.location.href = "login.html";
    return;
  }

  loadAndRenderTasks(defaultFilters);
  attachTaskEventHandlers();
});

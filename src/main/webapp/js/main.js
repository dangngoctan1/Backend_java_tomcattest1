const defaultFilters = {
    status: "all",
    priority: "all",
    sortBy: "createdDate"
};

document.addEventListener("DOMContentLoaded", function () {
    console.log('DOMContentLoaded triggered');
    if (!isLoggedIn()) {
        window.location.href = "/tomcattest1/login.html";
        return;
    }

    // Khởi tạo event handlers
    initializeEventHandlers();

    // Load và render tasks
    loadAndRenderTasks(defaultFilters);
    attachTaskEventHandlers();
});

function initializeEventHandlers() {
    console.log('Initializing event handlers');
    // Event handler cho nút đăng xuất
    document.getElementById("logoutBtn").addEventListener("click", function() {
        console.log('Logout button clicked');
        logout();
    });

    // Event handler cho filters
    document.getElementById("statusFilter").addEventListener("change", function() {
        console.log('Status filter changed');
        const filters = getCurrentFilters();
        loadAndRenderTasks(filters);
    });

    document.getElementById("priorityFilter").addEventListener("change", function() {
        console.log('Priority filter changed');
        const filters = getCurrentFilters();
        loadAndRenderTasks(filters);
    });

    document.getElementById("sortBy").addEventListener("change", function() {
        console.log('Sort by changed');
        const filters = getCurrentFilters();
        loadAndRenderTasks(filters);
    });

    // Event handler cho modal
    document.getElementById("addTaskBtn").addEventListener("click", function () {
        console.log('Add task button clicked');
        document.getElementById("modalTitle").textContent = "Thêm công việc mới";
        document.getElementById("taskForm").reset();
        document.getElementById("taskIdInput").value = "";
        document.getElementById("taskModal").style.display = "block";
    });

    document.getElementById("closeModal").addEventListener("click", function () {
        console.log('Close modal clicked');
        document.getElementById("taskModal").style.display = "none";
        document.getElementById("taskIdInput").value = "";
    });

    window.addEventListener("click", function (e) {
        const modal = document.getElementById("taskModal");
        if (e.target === modal) {
            console.log('Modal background clicked');
            document.getElementById("taskModal").style.display = "none";
            document.getElementById("taskIdInput").value = "";
        }
    });

    // Event handler cho form submit
    document.getElementById("taskForm").addEventListener("submit", function (e) {
        e.preventDefault();
        console.log('Form submitted');
        const title = document.getElementById("taskTitle").value.trim();
        const description = document.getElementById("taskDescription").value.trim();
        const dueDate = document.getElementById("taskDueDate").value;
        const priority = document.getElementById("taskPriority").value;
        const taskData = { title, description, dueDate, priority };

        const taskId = document.getElementById("taskIdInput").value;
        console.log('Task ID before submit:', taskId);

        if (taskId && taskId.trim() !== '') {
            console.log('Updating task with ID:', taskId);
            updateTask(taskId, taskData, function (success, message) {
                if (success) {
                    console.log('Task updated successfully');
                    document.getElementById("taskModal").style.display = "none";
                    document.getElementById("taskIdInput").value = "";
                    const filters = getCurrentFilters();
                    loadAndRenderTasks(filters);
                } else {
                    console.log('Failed to update task:', message);
                    alert("Cập nhật công việc thất bại: " + message);
                }
            });
        } else {
            console.log('Creating new task');
            createTask(taskData, function (success, message) {
                if (success) {
                    console.log('Task created successfully');
                    document.getElementById("taskModal").style.display = "none";
                    document.getElementById("taskIdInput").value = "";
                    const filters = getCurrentFilters();
                    loadAndRenderTasks(filters);
                } else {
                    console.log('Failed to create task:', message);
                    alert("Thêm công việc thất bại: " + message);
                }
            });
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
// taskService.js - API calls for tasks

function createQueryString(params) {
    var query = [];
    for (var key in params) {
        if (params.hasOwnProperty(key) && params[key] !== null && params[key] !== undefined) {
            query.push(
                encodeURIComponent(key) + "=" + encodeURIComponent(params[key])
            );
        }
    }
    return query.join("&");
}

function parseJavaDateTime(dateTimeString) {
    if (!dateTimeString) return null;
    try {
        return new Date(dateTimeString);
    } catch (e) {
        console.warn("Failed to parse date: " + dateTimeString);
        return null;
    }
}

function formatDateForJava(date) {
    if (!date) return null;
    if (typeof date === 'string') {
        date = new Date(date);
    }
    var year = date.getFullYear();
    var month = String(date.getMonth() + 1).padStart(2, '0');
    var day = String(date.getDate()).padStart(2, '0');
    var hours = String(date.getHours()).padStart(2, '0');
    var minutes = String(date.getMinutes()).padStart(2, '0');
    var seconds = String(date.getSeconds()).padStart(2, '0');
    return year + '-' + month + '-' + day + 'T' + hours + ':' + minutes + ':' + seconds;
}

function normalizeTaskData(task) {
    if (!task) return null;
    return {
        id: task.id,
        title: task.title,
        description: task.description,
        priority: task.priority ? task.priority.toLowerCase() : 'medium',
        status: task.status ? task.status.toLowerCase().replace('_', '-') : 'new',
        createdDate: parseJavaDateTime(task.createdDate || task.createdAt),
        dueDate: parseJavaDateTime(task.dueDate),
        updatedDate: parseJavaDateTime(task.updatedDate || task.updatedAt),
        userId: task.userId
    };
}

function prepareTaskDataForBackend(taskData) {
    var backendData = {
        title: taskData.title,
        description: taskData.description || "",
        priority: taskData.priority ? taskData.priority.toUpperCase() : 'MEDIUM',
        status: taskData.status ? taskData.status.toUpperCase().replace('-', '_') : 'NEW'
    };
    if (taskData.dueDate) {
        if (typeof taskData.dueDate === 'string' && taskData.dueDate.indexOf('T') === -1) {
            backendData.dueDate = taskData.dueDate + 'T00:00:00';
        } else {
            backendData.dueDate = formatDateForJava(taskData.dueDate);
        }
    }
    if (taskData.id) {
        backendData.id = taskData.id;
    }
    return backendData;
}

function getAllTasks(callback) {
    var user = getCurrentUser();
    if (!user) {
        callback([]);
        return;
    }
    var xhr = new XMLHttpRequest();
    xhr.open("GET", "/tomcattest1/api/tasks", true);
    xhr.setRequestHeader("Authorization", "Bearer " + localStorage.getItem("token"));
    xhr.setRequestHeader("Accept", "application/json");
    xhr.onreadystatechange = function () {
        if (xhr.readyState === 4) {
            if (xhr.status === 401) {
                console.error("Unauthorized: Token may be invalid or expired.");
                logout();
                callback([]);
                return;
            }
            if (xhr.status === 200) {
                try {
                    var tasks = JSON.parse(xhr.responseText);
                    var normalizedTasks = [];
                    for (var i = 0; i < tasks.length; i++) {
                        var normalizedTask = normalizeTaskData(tasks[i]);
                        if (normalizedTask) {
                            normalizedTasks.push(normalizedTask);
                        }
                    }
                    callback(normalizedTasks);
                } catch (e) {
                    console.error("Failed to parse tasks response: " + e.message);
                    callback([]);
                }
            } else {
                console.error("Failed to fetch tasks: " + xhr.status + " " + xhr.statusText);
                callback([]);
            }
        }
    };
    xhr.onerror = function () {
        console.error("Failed to fetch tasks: Network error");
        callback([]);
    };
    xhr.send();
}

function getFilteredTasks(statusFilter, priorityFilter, sortBy, callback) {
    if (typeof statusFilter === "undefined") statusFilter = "all";
    if (typeof priorityFilter === "undefined") priorityFilter = "all";
    if (typeof sortBy === "undefined") sortBy = "createdDate";
    getAllTasks(function (tasks) {
        if (statusFilter !== "all") {
            tasks = tasks.filter(function (task) {
                return task.status === statusFilter;
            });
        }
        if (priorityFilter !== "all") {
            tasks = tasks.filter(function (task) {
                return task.priority === priorityFilter;
            });
        }
        tasks.sort(function (a, b) {
            if (sortBy === "createdDate") {
                var dateA = a.createdDate || new Date(0);
                var dateB = b.createdDate || new Date(0);
                return dateB - dateA;
            } else if (sortBy === "dueDate") {
                if (!a.dueDate && !b.dueDate) return 0;
                if (!a.dueDate) return 1;
                if (!b.dueDate) return -1;
                return a.dueDate - b.dueDate;
            } else if (sortBy === "priority") {
                var priorityOrder = { high: 1, medium: 2, low: 3 };
                return (priorityOrder[a.priority] || 2) - (priorityOrder[b.priority] || 2);
            }
            return 0;
        });
        callback(tasks);
    });
}

function getTaskById(taskId, callback) {
    var xhr = new XMLHttpRequest();
    xhr.open("GET", "/tomcattest1/api/tasks/" + taskId, true);
    xhr.setRequestHeader("Authorization", "Bearer " + localStorage.getItem("token"));
    xhr.setRequestHeader("Accept", "application/json");
    xhr.onreadystatechange = function () {
        if (xhr.readyState === 4) {
            if (xhr.status === 401) {
                console.error("Unauthorized: Token may be invalid or expired.");
                logout();
                callback(null);
                return;
            }
            if (xhr.status === 200) {
                try {
                    var task = JSON.parse(xhr.responseText);
                    callback(normalizeTaskData(task));
                } catch (e) {
                    console.error("Failed to parse task response: " + e.message);
                    callback(null);
                }
            } else {
                console.error("Failed to fetch task: " + xhr.status + " " + xhr.statusText);
                callback(null);
            }
        }
    };
    xhr.onerror = function () {
        console.error("Failed to fetch task: Network error");
        callback(null);
    };
    xhr.send();
}

function createTask(taskData, callback) {
    var user = getCurrentUser();
    if (!user) {
        callback(false, "User not logged in");
        return;
    }
    var xhr = new XMLHttpRequest();
    xhr.open("POST", "/tomcattest1/api/tasks", true);
    xhr.setRequestHeader("Content-Type", "application/json");
    xhr.setRequestHeader("Authorization", "Bearer " + localStorage.getItem("token"));
    var backendData = prepareTaskDataForBackend(taskData);
    xhr.onreadystatechange = function () {
        if (xhr.readyState === 4) {
            if (xhr.status === 401) {
                console.error("Unauthorized: Token may be invalid or expired.");
                logout();
                callback(false, "Unauthorized");
                return;
            }
            if (xhr.status === 201 || xhr.status === 200) {
                callback(true, "Task created successfully");
            } else {
                try {
                    var response = JSON.parse(xhr.responseText);
                    var errorMsg = response.error || response.message || "Failed to create task";
                    console.error("Failed to create task: " + errorMsg);
                    callback(false, errorMsg);
                } catch (e) {
                    callback(false, "Failed to create task: Server error");
                }
            }
        }
    };
    xhr.onerror = function () {
        console.error("Failed to create task: Network error");
        callback(false, "Network error");
    };
    xhr.send(JSON.stringify(backendData));
}

function updateTask(taskId, taskData, callback) {
    var xhr = new XMLHttpRequest();
    xhr.open("PUT", "/tomcattest1/api/tasks/" + taskId, true);
    xhr.setRequestHeader("Content-Type", "application/json");
    xhr.setRequestHeader("Authorization", "Bearer " + localStorage.getItem("token"));
    var backendData = prepareTaskDataForBackend(taskData);
    backendData.id = taskId;
    xhr.onreadystatechange = function () {
        if (xhr.readyState === 4) {
            if (xhr.status === 401) {
                console.error("Unauthorized: Token may be invalid or expired.");
                logout();
                callback(false, "Unauthorized");
                return;
            }
            if (xhr.status === 200 || xhr.status === 204) {
                callback(true, "Task updated successfully");
            } else {
                try {
                    var response = JSON.parse(xhr.responseText);
                    var errorMsg = response.error || response.message || "Failed to update task";
                    console.error("Failed to update task: " + errorMsg);
                    callback(false, errorMsg);
                } catch (e) {
                    callback(false, "Failed to update task: Server error");
                }
            }
        }
    };
    xhr.onerror = function () {
        console.error("Failed to update task: Network error");
        callback(false, "Network error");
    };
    xhr.send(JSON.stringify(backendData));
}

function deleteTask(taskId, callback) {
    var xhr = new XMLHttpRequest();
    xhr.open("DELETE", "/tomcattest1/api/tasks/" + taskId, true);
    xhr.setRequestHeader("Authorization", "Bearer " + localStorage.getItem("token"));
    xhr.onreadystatechange = function () {
        if (xhr.readyState === 4) {
            if (xhr.status === 401) {
                console.error("Unauthorized: Token may be invalid or expired.");
                logout();
                callback(false, "Unauthorized");
                return;
            }
            if (xhr.status === 204 || xhr.status === 200) {
                callback(true, "Task deleted successfully");
            } else {
                try {
                    var response = JSON.parse(xhr.responseText);
                    var errorMsg = response.error || response.message || "Failed to delete task";
                    console.error("Failed to delete task: " + errorMsg);
                    callback(false, errorMsg);
                } catch (e) {
                    callback(false, "Failed to delete task: Server error");
                }
            }
        }
    };
    xhr.onerror = function () {
        console.error("Failed to delete task: Network error");
        callback(false, "Network error");
    };
    xhr.send();
}

function updateTaskStatus(taskId, newStatus, callback) {
    var xhr = new XMLHttpRequest();
    xhr.open("PATCH", "/tomcattest1/api/tasks/" + taskId + "/status", true);
    xhr.setRequestHeader("Content-Type", "application/json");
    xhr.setRequestHeader("Authorization", "Bearer " + localStorage.getItem("token"));
    var statusData = {
        status: newStatus.toUpperCase().replace('-', '_')
    };
    xhr.onreadystatechange = function () {
        if (xhr.readyState === 4) {
            if (xhr.status === 401) {
                console.error("Unauthorized: Token may be invalid or expired.");
                logout();
                callback(false, "Unauthorized");
                return;
            }
            if (xhr.status === 200 || xhr.status === 204) {
                callback(true, "Task status updated successfully");
            } else {
                try {
                    var response = JSON.parse(xhr.responseText);
                    var errorMsg = response.error || response.message || "Failed to update task status";
                    console.error("Failed to update task status: " + errorMsg);
                    callback(false, errorMsg);
                } catch (e) {
                    callback(false, "Failed to update task status: Server error");
                }
            }
        }
    };
    xhr.onerror = function () {
        console.error("Failed to update task status: Network error");
        callback(false, "Network error");
    };
    xhr.send(JSON.stringify(statusData));
}

function getTaskDueState(task) {
    if (!task.dueDate) return "none";
    var today = new Date();
    today.setHours(0, 0, 0, 0);
    var dueDate = new Date(task.dueDate);
    dueDate.setHours(0, 0, 0, 0);
    var timeDiff = dueDate - today;
    var daysDiff = Math.ceil(timeDiff / (1000 * 60 * 60 * 24));
    if (daysDiff < 0) return "overdue";
    if (daysDiff <= 2) return "soon";
    return "normal";
}

// Export các hàm ra window để frontend gọi được
window.getAllTasks = getAllTasks;
window.getFilteredTasks = getFilteredTasks;
window.getTaskById = getTaskById;
window.createTask = createTask;
window.updateTask = updateTask;
window.deleteTask = deleteTask;
window.updateTaskStatus = updateTaskStatus;
window.getTaskDueState = getTaskDueState;

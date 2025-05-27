// auth.js
// Authentication related functions

// Hàm hỗ trợ tạo chuỗi query từ object (thay thế URLSearchParams)
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

// Check if user is logged in
function isLoggedIn() {
    var token = localStorage.getItem("token");
    if (!token) return false;

    // Kiểm tra token có hết hạn không (nếu có thông tin expiry)
    try {
        var tokenData = JSON.parse(atob(token.split('.')[1])); // Decode JWT payload
        if (tokenData.exp && tokenData.exp < Date.now() / 1000) {
            localStorage.removeItem("token");
            localStorage.removeItem("currentUser");
            return false;
        }
    } catch (e) {
        // Token không phải định dạng JWT hoặc bị lỗi, vẫn cho phép sử dụng
    }

    return true;
}

// Get current user data
function getCurrentUser() {
    var userData = localStorage.getItem("currentUser");
    return userData ? JSON.parse(userData) : null;
}

// Login
function login(username, password, callback) {
    var xhr = new XMLHttpRequest();
    xhr.open("POST", "/tomcattest1/api/login", true);
    xhr.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");

    var data = createQueryString({
        username: username,
        password: password,
    });

    xhr.onreadystatechange = function () {
        if (xhr.readyState === 4) {
            if (xhr.status === 200) {
                try {
                    var response = JSON.parse(xhr.responseText);
                    localStorage.setItem("token", response.token);
                    localStorage.setItem(
                        "currentUser",
                        JSON.stringify({
                            id: response.userId,
                            username: username,
                            fullname: response.fullname || username
                        })
                    );
                    callback(true, null);
                } catch (e) {
                    console.error("Login failed: Invalid response format");
                    callback(false, "Invalid response format");
                }
            } else {
                try {
                    var response = JSON.parse(xhr.responseText);
                    var errorMsg = response.error || response.message || "Login failed";
                    console.error("Login failed: " + errorMsg);
                    callback(false, errorMsg);
                } catch (e) {
                    callback(false, "Login failed: Server error");
                }
            }
        }
    };

    xhr.onerror = function () {
        console.error("Login failed: Network error");
        callback(false, "Network error");
    };

    xhr.send(data);
}

// Register
function register(fullname, username, email, password, callback) {
    var xhr = new XMLHttpRequest();
    xhr.open("POST", "/tomcattest1/api/register", true);
    xhr.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");

    var data = createQueryString({
        fullname: fullname,
        username: username,
        email: email,
        password: password,
    });

    xhr.onreadystatechange = function () {
        if (xhr.readyState === 4) {
            if (xhr.status === 201 || xhr.status === 200) {
                callback(true, "Registration successful!");
            } else {
                try {
                    var response = JSON.parse(xhr.responseText);
                    var errorMsg = response.error || response.message || "Registration failed";
                    console.error("Registration failed: " + errorMsg);
                    callback(false, errorMsg);
                } catch (e) {
                    callback(false, "Registration failed: Server error");
                }
            }
        }
    };

    xhr.onerror = function () {
        console.error("Registration failed: Network error");
        callback(false, "Network error");
    };

    xhr.send(data);
}

// Logout
function logout() {
    var token = localStorage.getItem("token");

    if (token) {
        var xhr = new XMLHttpRequest();
        xhr.open("POST", "/tomcattest1/api/logout", true);
        xhr.setRequestHeader("Authorization", "Bearer " + token);

        xhr.onreadystatechange = function () {
            if (xhr.readyState === 4 && xhr.status !== 200 && xhr.status !== 204) {
                // Log error but still proceed with local logout
                console.warn("Server logout failed, proceeding with local logout");
            }
        };

        xhr.onerror = function () {
            console.warn("Network error during logout, proceeding with local logout");
        };

        xhr.send();
    }

    // Always clear local storage regardless of server response
    localStorage.removeItem("token");
    localStorage.removeItem("currentUser");
    window.location.href = "login.html";
}

// Auto-logout on token expiry
function checkTokenExpiry() {
    if (!isLoggedIn() && !window.location.pathname.includes("login.html") && !window.location.pathname.includes("register.html")) {
        window.location.href = "login.html";
    }
}

// If current page is not login or register, check authentication
const currentPath = window.location.pathname;
if (!currentPath.includes("login.html") && !currentPath.includes("register.html")) {
    if (!isLoggedIn()) {
        window.location.href = "login.html";
    } else {
        // Check token expiry every 5 minutes
        setInterval(checkTokenExpiry, 5 * 60 * 1000);
    }
}
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

function isLoggedIn() {
    var token = localStorage.getItem("token");
    if (!token) return false;

    try {
        var tokenData = JSON.parse(atob(token.split('.')[1]));
        if (tokenData.exp && tokenData.exp < Date.now() / 1000) {
            localStorage.removeItem("token");
            localStorage.removeItem("currentUser");
            return false;
        }
    } catch (e) {
        // Token không phải định dạng JWT hoặc bị lỗi
    }
    return true;
}

function getCurrentUser() {
    var userData = localStorage.getItem("currentUser");
    return userData ? JSON.parse(userData) : null;
}

function login(username, password, callback) {
    console.log('Hàm login được gọi');
    var xhr = new XMLHttpRequest();
    xhr.open("POST", "/tomcattest1/api/login", true);
    xhr.setRequestHeader("Content-Type", "application/json");

    var data = JSON.stringify({
        username: username,
        password: password
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

function register(fullname, username, email, password, callback) {
    console.log('Hàm register được gọi');
    var xhr = new XMLHttpRequest();
    xhr.open("POST", "/tomcattest1/api/register", true);
    xhr.setRequestHeader("Content-Type", "application/json");

    var data = JSON.stringify({
        fullname: fullname,
        username: username,
        email: email,
        password: password
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

function logout() {
    console.log('Hàm logout được gọi');
    var token = localStorage.getItem("token");

    if (token) {
        var xhr = new XMLHttpRequest();
        xhr.open("POST", "/tomcattest1/api/logout", true);
        xhr.setRequestHeader("Authorization", "Bearer " + token);

        xhr.onreadystatechange = function () {
            if (xhr.readyState === 4 && xhr.status !== 200 && xhr.status !== 204) {
                console.warn("Server logout failed, proceeding with local logout");
            }
        };

        xhr.onerror = function () {
            console.warn("Network error during logout, proceeding with local logout");
        };

        xhr.send();
    }

    localStorage.removeItem("token");
    localStorage.removeItem("currentUser");
    window.location.href = "/tomcattest1/login.html";
}

function checkTokenExpiry() {
    if (!isLoggedIn() && !window.location.pathname.includes("login.html") && !window.location.pathname.includes("register.html")) {
        window.location.href = "/tomcattest1/login.html";
    }
}

if (!window.location.pathname.includes("login.html") && !window.location.pathname.includes("register.html")) {
    if (!isLoggedIn()) {
        window.location.href = "/tomcattest1/login.html";
    } else {
        setInterval(checkTokenExpiry, 5 * 60 * 1000);
    }
}

window.createQueryString = createQueryString;
window.isLoggedIn = isLoggedIn;
window.getCurrentUser = getCurrentUser;
window.login = login;
window.register = register;
window.logout = logout;
window.checkTokenExpiry = checkTokenExpiry;
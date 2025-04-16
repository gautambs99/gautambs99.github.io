// ✅ Place this function at the top of dashboard.js before it is called
// ✅ Place this function at the top of dashboard.js before it is called

document.addEventListener('DOMContentLoaded', function () {
    console.log("✅ dashboard.js Loaded Successfully!");
    const userType = sessionStorage.getItem("userType");

    const viewMyCoursesBtn = document.getElementById("view-my-courses-btn");
    if (userType === "student") {
    if (viewMyCoursesBtn) {
        viewMyCoursesBtn.addEventListener("click", fetchStudentCourses);
    }
}});

function fetchStudentCourses() {
    const studentId = sessionStorage.getItem("userID");

    if (!studentId) {
        console.error("❌ Error: No student ID found in sessionStorage.");
        return;
    }

    fetch(`http://localhost:8080/CourseMgmtSys_war_exploded/StudentCoursesServlet?userID=${studentId}`)
        .then(response => response.json())
        .then(data => {
            console.log("✅ Student Courses Data:", data);

            const courseTable = document.getElementById("course-list");
            if (!courseTable) {
                console.error("❌ course-list element not found in HTML.");
                return;
            }

            courseTable.innerHTML = ""; // ✅ Clear old results

            if (!Array.isArray(data) || data.length === 0) {
                courseTable.innerHTML = "<tr><td colspan='4'>No enrolled courses found.</td></tr>";
                return;
            }

            data.forEach(course => {
                let row = document.createElement("tr");
                row.innerHTML = `
                    <td>${course.courseID}</td>
                    <td>${course.courseName}</td>
                    <td>${course.department}</td>
                    <td>${getStatusIcon(course.status)}</td>
                `;
                courseTable.appendChild(row);
            });

            console.log("✅ UI Updated with student courses.");
        })
        .catch(error => {
            console.error("❌ Error fetching student courses:", error);
        });
}

// ✅ Helper function to determine status icons
function getStatusIcon(status) {
    switch (status) {
        case "Completed":
            return `<span class="status-icon status-completed">✔️ Completed</span>`;
        case "Ongoing":
            return `<span class="status-icon status-in-progress">⏳ Ongoing</span>`;
        case "Pending":
            return `<span class="status-icon status-pending">🔜 Pending</span>`;
        default:
            return `<span class="status-icon status-not-taken">❌ Not Taken</span>`;
    }
}

document.addEventListener("DOMContentLoaded", function () {
    console.log("🚀 Page Loaded. Fetching Student Courses...");
    fetchStudentCourses();
});

document.addEventListener('DOMContentLoaded', function () {
    console.log("✅ dashboard.js Loaded Successfully!");
    const userType = sessionStorage.getItem("userType");
    const bookButton = document.getElementById("book-appointment");
    if (userType === "student") {
    if (bookButton) {
        bookButton.addEventListener("click", bookAppointment);
        console.log("📌 bookAppointment() event listener added.");
    } else {
        console.error("❌ book-appointment button not found in DOM.");
    }
}});
function bookAppointment() {
    console.log("🔄 bookAppointment() function called!");

    const studentId = sessionStorage.getItem("userID");
    const facultyId = sessionStorage.getItem("facultyId");
    const dateTime = document.getElementById("appointment-datetime").value;

    if (!studentId) {
        console.error("❌ Error: No student ID found in sessionStorage.");
        document.querySelector(".appointment-message").textContent = "❌ Unable to book: Student ID missing.";
        return;
    }

    if (!dateTime) {
        console.error("❌ No date and time selected.");
        document.querySelector(".appointment-message").textContent = "❗ Please select a date and time.";
        return;
    }

    console.log(`📅 Booking appointment: Student ID = ${studentId}, Faculty ID = ${facultyId}, DateTime = ${dateTime}`);

    fetch("http://localhost:8080/CourseMgmtSys_war_exploded/AppointmentServlet", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: new URLSearchParams({
            studentId: studentId,
            facultyId: facultyId,
            appointmentDateTime: dateTime
        }),
    })
        .then(response => response.json())
        .then(data => {
            console.log("✅ Parsed response:", data);

            if (data.success) {
                document.querySelector(".appointment-message").textContent = "📅 Appointment booked successfully!";
                loadStudentAppointments(); // Refresh appointment list
            } else {
                document.querySelector(".appointment-message").textContent = "❌ Booking failed. Please try again.";
                console.error("❌ Server Error:", data.message);
            }
        })
        .catch(error => {
            console.error("❌ Error booking appointment:", error);
            document.querySelector(".appointment-message").textContent = "❌ Booking failed due to a server issue.";
        });
}

function loadStudentAppointments() {
    const studentId = sessionStorage.getItem("userID");

    if (!studentId) {
        console.error("❌ Error: No student ID found in sessionStorage.");
        return;
    }

    fetch(`http://localhost:8080/CourseMgmtSys_war_exploded/AppointmentServlet?userId=${studentId}`)
        .then(response => response.json())
        .then(data => {
            console.log("✅ Upcoming Appointments:", data);

            const appointmentsList = document.getElementById("student-appointments");
            if (!appointmentsList) {
                console.error("❌ student-appointments element not found in DOM.");
                return;
            }

            appointmentsList.innerHTML = "";

            if (!Array.isArray(data) || data.length === 0) {
                appointmentsList.innerHTML = "<li>No upcoming appointments.</li>";
                return;
            }

            data.forEach(appt => {
                let appointmentDate = appt.date ? new Date(appt.date).toLocaleString() : "No Date";
                let facultyName = appt.faculty || "Unknown Faculty";

                let li = document.createElement("li");
                li.innerHTML = `<span class="appointment-icon">📅</span> ${appointmentDate} with <strong>${facultyName}</strong>`;
                appointmentsList.appendChild(li);
            });
        })
        .catch(error => console.error("❌ Error loading student appointments:", error));
}

document.addEventListener('DOMContentLoaded', function () {
    const userType = sessionStorage.getItem("userType");
    const userEmail = sessionStorage.getItem("userEmail");

    if (!userType || !userEmail) {
        console.error("❌ UserType or Email is missing. Redirecting to login...");
        window.location.href = "index.html";
        return;
    }

    console.log("✅ dashboard.js Loaded Successfully!");
    console.log("✅ User Type:", userType);
    console.log("✅ User Email:", userEmail);

    fetchUserDetails(userEmail);

    if (userType === "student") {
        const viewAllCoursesBtn = document.getElementById("view-all-courses-btn");
        if (viewAllCoursesBtn) {
            viewAllCoursesBtn.addEventListener("click", fetchCourses);
        }

        fetchFacultyDetails();
        loadStudentAppointments(); // ✅ Now it's properly defined before being used
    } /*else if (userType === "faculty") {
        loadFacultyAppointments();
    }*/

    document.getElementById("logout").addEventListener("click", (e) => {
        e.preventDefault();
        sessionStorage.clear();
        window.location.href = "index.html";
    });

    const defaultPage = userType === "faculty" ? "faculty-dashboard" : "student-dashboard";
    const lastPage = sessionStorage.getItem(`${userType}-lastPage`) || defaultPage;
    showPage(lastPage);

    document.querySelectorAll(".tab-link").forEach(link => {
        link.addEventListener("click", function (e) {
            e.preventDefault();
            const pageId = this.getAttribute("data-page");
            showPage(pageId);
            highlightActiveTab(pageId);
            sessionStorage.setItem(`${sessionStorage.getItem("userType")}-lastPage`, pageId);
        });
    });
});

// ✅ FIXED: Fetch User Details with error handling
function fetchUserDetails(email) {
    const userType = sessionStorage.getItem("userType");
    fetch("http://localhost:8080/CourseMgmtSys_war_exploded/UserServlet", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: new URLSearchParams({ email: email }),
    })
        .then(response => response.json())
        .then(userData => {
            console.log("✅ User Data Received:", userData);

            if (userData.success) {
                sessionStorage.setItem("userID", userData.userID);
                sessionStorage.setItem("userEmail", userData.email);

                // ✅ Store advisor_id only if it's not 0
                if (userData.advisor_id && userData.advisor_id !== 0) {
                    sessionStorage.setItem("facultyId", userData.advisor_id);
                    console.log("✅ Faculty ID Stored:", userData.advisor_id);
                } else {
                    console.warn("⚠️ No assigned faculty advisor.");
                }
                if (userType === "student") {
                    document.getElementById("profile-advisor").textContent = userData.advisor || "No Advisor Assigned";
                }
                updateUserDetails(
                    userData.name || "User",
                    userData.email || "N/A",
                    userData.userID || "N/A",
                    userData.department || "N/A",
                    userData.advisor || "No Advisor Assigned"
                );
            } else {
                console.error("❌ User not found:", userData.message);
            }
        })
        .catch(error => console.error("❌ Error fetching user details:", error));
}

// ✅ FIXED: Ensure Faculty Details Load
function fetchFacultyDetails() {
    const studentEmail = sessionStorage.getItem("userEmail");

    fetch("http://localhost:8080/CourseMgmtSys_war_exploded/FacultyServlet", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: new URLSearchParams({ email: studentEmail }),
    })
        .then(response => response.json())
        .then(data => {
            console.log("✅ Faculty Data:", data);

            if (data.success) {
                document.getElementById("advisor-name").textContent = data.name;
                document.getElementById("advisor-department").textContent = "Department ID: " + data.department;
                document.getElementById("advisor-courses").textContent = "Teaching: " + data.teaching_courses;
                document.getElementById("advisor-office").textContent = "Office Hours: " + data.office_hours;
            } else {
                console.error("❌ Faculty not found:", data.message);
                document.getElementById("advisor-name").textContent = "No Advisor Assigned";
                document.getElementById("advisor-department").textContent = "";
                document.getElementById("advisor-courses").textContent = "";
                document.getElementById("advisor-office").textContent = "";
            }
        })
        .catch(error => console.error("❌ Error fetching faculty details:", error));
}

// ✅ FIXED: Update User Details Safely
function updateUserDetails(name, email, userID, department, advisor) {
    document.getElementById("user-name").textContent = name || "User";
    document.getElementById("profile-name").textContent = name || "User";
    document.getElementById("profile-email").textContent = email || "N/A";
    document.getElementById("profile-id").textContent = userID || "N/A";
    document.getElementById("profile-department").textContent = department || "N/A";

    const advisorElement = document.getElementById("profile-advisor");
    if (advisorElement) {
        advisorElement.textContent = advisor || "No Advisor Assigned";
    }
}

// ✅ FIXED: Show the Correct Page
function showPage(pageId) {
    console.log(`🎯 Switching to page: ${pageId}`);

    document.querySelectorAll('.page').forEach(page => {
        page.style.display = 'none';
    });

    const selectedPage = document.getElementById(pageId);
    if (selectedPage) {
        selectedPage.style.display = 'block';

        // ✅ Show appointment section only in the advising tab
        const appointmentSection = document.querySelector(".appointment-section");
        if (appointmentSection) {
            if (pageId === "student-advising-tab") {
                appointmentSection.style.display = "block";
            } else {
                appointmentSection.style.display = "none";
            }
        }
    } else {
        console.error(`❌ Page ID '${pageId}' not found.`);
    }
}

// ✅ FIXED: Load Courses



// ✅ Helper function to determine status icons
function getStatusIcon(status) {
    switch (status) {
        case "Completed":
            return `<span class="status-icon status-completed">✔️ Completed</span>`;
        case "Ongoing":
            return `<span class="status-icon status-in-progress">⏳ Ongoing</span>`;
        case "Pending":
            return `<span class="status-icon status-pending">🔜 Pending</span>`;
        default:
            return `<span class="status-icon status-not-taken">❌ Not Taken</span>`;
    }
}

// ✅ Call function when page loads
document.addEventListener("DOMContentLoaded", function () {
    console.log("🚀 Page Loaded. Fetching Courses...");
    fetchCourses();
});

// ✅ FIXED: Highlight Active Tab
function highlightActiveTab(pageId) {
    document.querySelectorAll(".tab-link").forEach(link => {
        link.classList.remove("active");
        if (link.getAttribute("data-page") === pageId) {
            link.classList.add("active");
        }
    });
}

// ✅ FIXED: Ensure Dropdown Works
document.getElementById("profile-icon").addEventListener("click", function(event) {
    event.stopPropagation();
    document.getElementById("dropdown-menu").classList.toggle("show");
});

document.addEventListener("click", function(event) {
    var menu = document.getElementById("dropdown-menu");
    var icon = document.getElementById("profile-icon");
    if (!menu.contains(event.target) && !icon.contains(event.target)) {
        menu.classList.remove("show");
    }
});
document.addEventListener("DOMContentLoaded", function () {
    console.log("✅ dashboard.js Loaded Successfully!");

    const searchButton = document.getElementById("search-button");
    const searchInput = document.getElementById("course-search-input");

    if (searchButton) {
        searchButton.addEventListener("click", function () {
            const query = searchInput.value.trim();
            console.log(`🔍 Searching for: ${query}`);
            fetchCourses(query);
        });
    } else {
        console.error("❌ Search button not found.");
    }

    fetchCourses(); // ✅ Ensures courses are loaded on page load
});

// ✅ Function to fetch courses based on search input
function fetchCourses(query = "") {
    const userType = sessionStorage.getItem("userType");
    if (userType === "student") {
        fetch(`http://localhost:8080/CourseMgmtSys_war_exploded/CourseServlet?search=${encodeURIComponent(query)}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error(`Server error: ${response.status}`);
                }
                return response.json();
            })
            .then(data => {
                console.log("📡 API Response:", data);

                const courseList = document.getElementById("courses");
                if (!courseList) {
                    console.error("❌ courses element not found in HTML.");
                    return;
                }

                courseList.innerHTML = ""; // ✅ Clear old results

                if (!Array.isArray(data) || data.length === 0) {
                    console.warn("⚠️ No courses found.");
                    courseList.innerHTML = "<li>No courses found.</li>";
                    return;
                }

                // ✅ Display courses as clickable links
                data.forEach(course => {
                    let li = document.createElement("li");
                    let link = document.createElement("a");
                    link.href = `course-details.html?courseId=${course.id}`;
                    link.textContent = `${course.id} - ${course.name}`;
                    li.appendChild(link);
                    courseList.appendChild(li);
                });

                console.log("✅ UI Updated with clickable courses.");
            })
            .catch(error => {
                console.error("❌ Error fetching courses:", error);
                document.getElementById("courses").innerHTML;
            });
    }}

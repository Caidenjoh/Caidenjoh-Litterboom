
<img width="490" height="490" alt="image" src="https://github.com/user-attachments/assets/74d29c01-a509-4ed7-9098-1807b276c051" />

**LitterBoom Mobile Application**

**A mobile app for managing environmental clean-ups — from event creation to rubbish tracking**

**Overview**

LitterBoom is a mobile application that supports environmental clean-up initiatives by allowing administrators to manage events and employees to log rubbish collection data. The app provides real-time tracking of activities and visual reporting, helping organizations monitor environmental impact efficiently.
It is built with Kotlin and Jetpack Compose, using Firebase for secure authentication, data storage, and synchronization.

**Features**

Secure admin and user login system
Create, manage, and filter clean-up events
Log rubbish by category, sub-category, and weight
Upload photos of collected waste
View and export event waste data
Manage waste categories and field types
Role-based access control (Admin / User)


**Requirements**

Android Studio (latest version)
Android SDK 33 or higher
Minimum Android 8.0 (Oreo)
Firebase project with Firestore, Authentication, and Cloud Storage enabled
Internet connection for syncing data

**How to Run the App**

1. Clone the Repository
git clone https://github.com/Caidenjoh/Caidenjoh-Litterboom.git

2. Open in Android Studio
Open the cloned project in Android Studio.
Sync Gradle to download all dependencies.

3. Connect Firebase
Add your google-services.json file to connect the app to your Firebase project.

4. Launch
Build and run the app on an Android emulator or physical device.

5. Login
Admin Credentials:
Username: admin
Password: admin123
Employee Credentials: Created by the admin in the app.

**API Integration and Data Flow**

To ensure seamless communication between the mobile application and the central database, LitterBoom integrates with a custom-built API.
The API handles all data transactions, including:
Adding new clean-up records
Retrieving stored data
Updating existing entries
Whenever a user logs waste collection information—such as type, quantity, or brand—the data is sent securely to the API. The API validates and processes the request before storing it in the database.
By routing all database operations through the API:
Data remains consistent, structured, and synchronized across all devices.
Errors are managed efficiently.
Security rules are enforced.
Communication between the app and backend is standardized.
This design ensures reliability, security, and scalability, enabling future enhancements such as analytics dashboards or advanced reporting.

**Using the App**

**Admin**

Access Admin Panel via hamburger menu after login.
Create Events: Add name, date, and location.
Manage Categories/Fields: Add, edit, or disable categories and field types.
Approve Waste Logs: Review user entries and generate reports.
Admin adds new users/admins

**User**

Select an event to log rubbish.
Enter bag number, weight, and category/sub-category.
Upload a photo for each entry.
Submit data to sync automatically.
View totals for bags and total collected weight.

**Future Improvements**

Enable offline data entry for remote clean-up areas.
Add graphical dashboards for event statistics.
Implement push notifications for event updates.
Expand platform support to iOS and web.

**Team Members**

Teagan Griffiths – Group Leader
Responsible for documentation, project coordination, and app interface design.

Caiden Johanson – Vice Leader
Project manager and backend developer; coordinated the team and managed database design.

Emilio Govender – Full Stack Developer
Integrated frontend and backend, supporting database optimization.

Cameron Cowdrey – Backend Developer
Implemented APIs, backend logic, and conducted testing.

Jucal Maistry – Documentation
Assisted in writing and maintaining project documentation.

**GitHub Repositories**

github links: https://github.com/Caidenjoh/Caidenjoh-Litterboom.git 
https://github.com/ST10344544/LitterboomApiRender.git
https://github.com/ST10344544/LitterBoomApi.git

**Acknowledgements**
Thanks to The LitterBoom Project for collaboration and support, and to our mentors for guidance throughout development.

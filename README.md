# Calendar Booking System

A Spring Boot-based REST API for **appointment scheduling**. It provides endpoints to **create owners**, **manage work routines**, **book appointments**, **view availability**, and **get summaries**. The system can be extended to handle **group meetings**, **notifications**, and **advanced scheduling** in the future.

---

## 1. Features

- Create and manage calendar owners
- Set, modify, and view work routines of owners
- Provide invitee details and manage their appointments
- View available time slots for a given date
- Book appointments with owners
- Fetch summaries of upcoming and today's appointments for owners
- Calendar supports a **15-day timespan** excluding today (**total: 16 days**)

---

## 2. Tech Stack

| Technology         | Version | Purpose                  |
|--------------------|---------|--------------------------|
| Java              | 17      | Core application logic  |
| Spring Boot       | 3.4     | REST API framework      |
| Maven             | 4.x     | Dependency management   |
| JUnit             | 5       | Testing framework       |

---

## 3. Running the Project

```bash
# Clone the repository
git clone https://github.com/RITWIKRUDRA01/calendar-booking-system.git

# Navigate into the project directory
cd calendar-booking-system

# Build the project
mvn clean install

# Run the Spring Boot application
mvn spring-boot:run
OR
open project in ide (eclipse/intellij) and run:src/main/java/com/example/calendar/CalendarBookingSystemApplication.java

# Base url:
http://localhost:8080

---

## 4. API endpoints documentation

| **Endpoint**                             | **HTTP Method** | **Controller**          | **Request Body / Params**                                                                     | **Description / Use**                                                                   |
| ---------------------------------------- | --------------- | ----------------------- | --------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------- |
| `/api/owners`                            | **POST**        | CalendarOwnerController | `{ "name": "John", "email": "john@example.com" }`                                             | Creates a new **calendar owner** with name & email.                                     |
| `/api/owners`                            | **GET**         | CalendarOwnerController | None                                                                                          | Fetches **all calendar owners** from the database.                                      |
| `/api/owners/settings/work-details/{id}` | **GET**         | CalendarOwnerController | **Path Var:** Owner ID                                                                        | Gets **work details** (working hours, slots, etc.) of a specific owner.                 |
| `/api/owners/settings/work-details`      | **POST**        | CalendarOwnerController | `{ "id": "...", "workingHours": {...} }`                                                      | Updates the **work details** for a calendar owner.                                      |
| `/api/owners/{id}/appointments/summary`  | **GET**         | CalendarOwnerController | **Path Var:** Owner ID                                                                        | Returns a **full summary** of all appointments for the given owner.                     |
| `/api/owners/{id}/appointments/today`    | **GET**         | CalendarOwnerController | **Path Var:** Owner ID                                                                        | Fetches only **today's appointments** for the given owner.                              |
| `/api/invitees`                          | **POST**        | InviteeController       | `{ "name": "Alice", "email": "alice@mail.com" }`                                              | Creates a new **invitee** who can book appointments.                                    |
| `/api/invitees`                          | **GET**         | InviteeController       | None                                                                                          | Gets **invitee details** of the currently created invitee.                              |
| `/api/invitees/available-slots`          | **POST**        | InviteeController       | `{ "ownerId": "...", "year": 2025, "month": 8, "day": 20 }`                                   | Returns a list of **available slots** for a specific owner on a specific date.          |
| `/api/invitees/book-appointment`         | **POST**        | InviteeController       | `{ "ownerId": "...", "subject": "cricket", "day": 24, "month": 8, "year": 2025, "hour": 10 }` | Books an **appointment** if the slot is available and invitee has checked availability. |
| `/api/invitees/owner/{ownerId}`          | **GET**         | InviteeController       | **Path Var:** Owner ID                                                                        | Fetches **calendar owner details** using owner ID.                                      |
| `/api/invitees/invitee`                  | **GET**         | InviteeController       | None                                                                                          | Fetches **invitee info** related to the booked appointment.                             |


---

## 5. Usage Flow

A.Create a Calendar Owner → POST /api/owners
B.Set Work Details → POST /api/owners/settings/work-details
C.Create Invitee → POST /api/invitees
D.Check Available Slots → POST /api/invitees/available-slots
E.Book Appointment → POST /api/invitees/book-appointment
F.View Appointments Summary :
a.All appointments → GET /api/owners/{id}/appointments/summary
b.Today's appointments → GET /api/owners/{id}/appointments/today

---

## 6. PostMan snaps for illustration:
A.Create a CalendarOwner object:
<img width="940" height="676" alt="image" src="https://github.com/user-attachments/assets/4137faf1-0a02-4d40-acf8-9fbe38eae59b" />
B.View CalendarOwner objects:
<img width="940" height="598" alt="image" src="https://github.com/user-attachments/assets/065322ce-1783-4cbf-9ee5-978b344b9259" />
C.Update work details:
<img width="940" height="321" alt="image" src="https://github.com/user-attachments/assets/0fbdc108-3f18-4fe5-ae84-f9c6a323810f" />
D.View work details:
<img width="940" height="351" alt="image" src="https://github.com/user-attachments/assets/766fb280-1925-42d8-b44d-fe09c887c3fb" />
E.Create invitee:
<img width="940" height="410" alt="image" src="https://github.com/user-attachments/assets/a10831cc-89b0-4f8a-9583-203bc99624c7" />
F. View invitee:
<img width="940" height="391" alt="image" src="https://github.com/user-attachments/assets/541a04a0-8731-41e2-abfe-e19d81c1e76d" />
G.Check availability:
<img width="940" height="348" alt="image" src="https://github.com/user-attachments/assets/e88fca71-e458-4457-932e-e39078a13640" />
H.Book Appointment:
<img width="940" height="461" alt="image" src="https://github.com/user-attachments/assets/6788334f-a5c4-498c-9c46-180536d275b6" />
I.Get today's appointments:
<img width="940" height="440" alt="image" src="https://github.com/user-attachments/assets/b607313a-f309-4688-9d86-7c88c4700460" />
J.Get full list of appointments:
<img width="940" height="475" alt="image" src="https://github.com/user-attachments/assets/0f188363-ecf9-4fc1-9ec7-816304660092" />

---

## 7. Assumptions:

The following assumptions were made while designing this Calendar Booking System:

A. **Owner Uniqueness**  
   - Each calendar owner is uniquely identified by their **id**.
   - No two owners can share the same id.
   - To simulate this, we have taken unique id in string format as of now.

B. **Invitee Handling**  
   - Invitees are stored separately from owners.
   - One invitee can book multiple appointments but cannot double-book the **same time slot**.

C. **Time Slots**  
   - The owner defines their **working hours** using the `/api/owners/settings/work-details` API.
   - Available slots are generated based on these working hours.
   - Slot granularity is **1 hour** (configurable later).

D. **Appointments**  
   - Two appointments **cannot overlap** for the same owner.
   - An appointment is successfully booked only if the slot is **available** at the time of booking.

E. **Date & Time**  
   - Timezone assumed is **IST (Asia/Kolkata)** by default.
   - Input format: **24-hour format**.

F. **Notifications** *(Future Scope)*  
   - Currently, there is **no email or SMS notification** system implemented.

G. **Database Assumptions**  
   - Owner and invitee data are stored persistently.
   - No sharding or caching layer for now — this can be added later if the system scales.

---

## 8. Future Enhancements:
Support for recurring appointments
Integration with Google Calendar / Outlook
Authentication & Authorization (JWT, OAuth2)
Email/SMS notifications using AWS SES or Twilio
Slot customization — support for 15-min / 30-min slots
Scalability with Redis caching and horizontal partitioning

---

## 9. Author:
Ritwik Rudra













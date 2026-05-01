# 1. INTRODUCTION

## 1.1 Project Background
In the contemporary landscape of software engineering and IT management, large-scale organizations frequently rely on external vendors or third-party IT service providers to supply specialized engineering talent and software development capacity. While this outsourcing model offers flexibility, cost-efficiency, and scalability, it simultaneously introduces substantial administrative and operational challenges. Software procurement is traditionally a highly complex and often painful process. Organizations find themselves struggling with disjointed communication channels, opaque performance metrics, and a severe lack of centralized tracking for outsourced deliverables.

When an organization assigns a software development project to a third-party vendor, tracking the true progress and the individual developer's task performance often becomes obscured behind the vendor's internal, proprietary management processes. Project managers on the client side receive highly summarized, often sanitized reports that fail to reflect the granular realities of development progress. Conversely, the vendor administrators and their developers encounter friction in aligning with the client's agile processes, leading to misinterpretations of requirements, delayed feedback loops, and chaotic sprint planning. 

The Vendor Management System (VMS) developed in this graduation project is explicitly designed to address these profound industry pain points. By providing a unified, multi-tenant ecosystem, VMS bridges the gap between the client organization's Project Managers, the Vendor's Administrators, and the individual Vendor Developers. It introduces strict role isolation, direct synchronization with Agile development processes, and a transparent layer of performance analytics that transforms software procurement from a black-box operation into a measurable, collaborative enterprise. 

## 1.2 Purpose and Significance
The primary objective of this project is to develop a comprehensive Vendor Management System that unifies project tracking, vendor workforce management, and Agile sprint methodologies within a single, cohesive software architecture. The significance of VMS lies in its ability to eliminate the fragmentation typically associated with outsourced software engineering.

Traditionally, IT departments use one tool for high-level project management, another for external vendor communication, and vendor teams use entirely separate issue-tracking systems for their sprint management. VMS consolidates these distinct architectural layers. It allows client Project Managers to dynamically create projects, evaluate vendor performance mathematically based on executed tasks, and issue project-specific announcements. Concurrently, it grants Vendor Admins a robust interface to organize developer cohorts, construct strict 2-week active agile sprints, explicitly manage capacity up to definitive point limitations, and rigorously review task deliveries before they are considered completed. 

By centralizing these functions, VMS ensures that expectations are explicitly mapped to digital deliverables. It makes it exceptionally easy to track whether a software procurement contract is yielding the expected results, ultimately accelerating delivery timelines, improving software quality, and dramatically reducing the administrative burden on both the client and the vendor.

# 2. LITERATURE REVIEW

## 2.1 Software Outsourcing and Procurement Challenges
A review of existing literature on outsourced software engineering frequently highlights transparency, data security, and systemic integration as the dominant challenges in buyer-vendor relationships (Lacity & Willcocks, 2017). Vendor lock-in, misaligned incentives, and communication latency are widely documented phenomena that directly result in software delivery delays. Research indicates that when developer workflows (the lowest tier of operation) are decoupled from client-side project tracking (the highest tier of management), the probability of schedule overrun or requirement failure significantly increases. Conventional solutions often attempt to enforce heavy, centralized compliance reporting, which conversely decreases developer productivity by forcing manual data entry. VMS approaches this challenge by seamlessly integrating lightweight, sprint-based tracking directly into the daily operational workflow of the developers.

## 2.2 Agile Methodology in Distributed Teams
Agile software development, particularly the Scrum framework, has become the definitive industry standard for managing complex software projects. Agile is characterized by iterative development cycles known as Sprints, usually lasting between one to four weeks. During a sprint, developer capacity is estimated, and discrete units of work (Assignments/Tasks) are assigned numerical point values representing their effort and complexity. 

For highly distributed teams operating across multi-vendor boundaries, adhering strictly to Agile ceremonies requires sophisticated tooling. VMS mathematically enforces Agile best practices at the architectural level. By algorithmic design, it restricts sprints to a standard two-week active duration (automatically calculating 13 days from the start date to enforce exactly 14 calendar days) and caps total developer task assignments at 10 points per sprint. This automated enforcement of Agile capacity planning prevents vendor developer burnout while ensuring realistic delivery expectations for the client Project Manager. It transitions Agile from a theoretical management concept into a computationally enforced software constraint.

## 2.3 Existing Solutions vs. VMS
The enterprise market currently features several heavy-weight project management tools, most notably Jira (Atlassian) and Trello. While Jira is highly configurable and represents the industry standard, it is fundamentally designed as an internal issue tracker for single-organization usage. Configuring Jira for strict multi-vendor isolation—where distinct, competing vendors operate autonomously within the same instance, but their independent output mathematically rolls up into an overarching client analytics rubric—requires complex, often brittle administrative permission workarounds and costly third-party plugins.

VMS sets itself apart by making "Vendor Data Isolation" a native, first-class architectural component. Unlike generic Kanban board tools, VMS has a strict ontological understanding of the difference between a Project Manager (representing the purchasing client) and a Vendor Admin (representing the supplying company). In VMS, the code review pipeline is customized specifically for supply-chain software engineering: a developer cannot simply mark a task as "Done." They must move it to the `IN_REVIEW` status, natively requiring their Vendor Admin to explicitly assess the work and provide documented text justification in the event of a rejection. This built-in quality gate is uniquely tailored for software procurement contracts, providing an out-of-the-box alignment that tools like Jira require extensive manual configuration to mimic.

# 3. METHODOLOGIES AND TECHNOLOGIES

The development of the Vendor Management System demanded a robust, modern technology stack capable of handling complex relational logic, high concurrency, strict multi-tenancy access control, and a highly responsive user interface. The following technologies were selected based on their specific alignment with enterprise software requirements and their widespread presence in contemporary computer engineering.

## 3.1 Backend: Java and Spring Boot
For the core backend system architecture, Java coupled with the Spring Boot framework (version 3.x) was selected. Spring Boot is an industry leader in building production-ready, highly scalable enterprise REST APIs.
*   **Robustness and Typology:** Java is a strongly, statically typed, class-based object-oriented language. The rigorous compiler checks significantly reduce runtime logic errors, which is absolutely critical for a high-stakes management system processing relational task mappings and capacity mathematics. 
*   **Dependency Injection (IoC):** Spring Boot's core principle of Inversion of Control (IoC) via dependency injection allows for highly modular, easily testable service layers. This facilitated the clean separation of core business logic (such as calculating sprint date constraints, validating user access tokens, and enforcing the 10-point mathematical limits) from the HTTP Controller routing configurations.
*   **Spring Data JPA:** This powerful abstraction over the Hibernate Object-Relational Mapping (ORM) tool allowed for rapid and secure database operations. It successfully mapped complex entity relations—such as the Many-to-Many join tables between `Sprints` and `Users`—directly into the underlying infrastructure without writing error-prone, dialect-specific native SQL queries.

## 3.2 Frontend: Vue.js, Vite, and Modern UI
The user interface was constructed using Vue.js via the Vite build tool. A Single Page Application (SPA) architecture was deemed necessary to provide a highly interactive, native-app-like experience that prevents jarring page reloads during rapid sprint planning.
*   **Reactivity and Component Architecture:** Vue’s reactivity system (using the modern Composition API) automatically tracks states and deeply updates the Document Object Model (DOM) when the underlying backend data models change. This ensures that when an Assignment is dragged across columns on the Kanban board, the UI instantly reflects these state payload shifts.
*   **Design Paradigm:** The application was constructed utilizing a custom, high-contrast Light Mode design system. It explicitly avoids overused, overly chaotic UI elements, opting instead for a highly structured, professional, and accessible layout that enhances typographical readability. 
*   **Axios Interceptors and State Routing:** By utilizing Vue Router, the frontend strictly manages authentication patterns. Route guards evaluate the user's role string (`MANAGER`, `VENDOR_ADMIN`, `PERSONNEL`) dynamically parsed from local storage JWT-like session objects, ensuring unauthorized users cannot render dashboard trees meant for an elevated privilege sequence.

## 3.3 Infrastructure: Docker and Containerization
To guarantee environmental consistency across local development, rigorous testing, and eventual production server deployment, the entire application stack was heavily containerized utilizing Docker technology. 
*   **The Containerization Paradigm:** Historically, software engineering teams faced the infamous "it works on my machine" problem, where an application runs perfectly on a developer's Windows OS but fails on an Alpine Linux production server due to missing runtime libraries. Docker essentially eliminates this by packaging the compiled application alongside its exact environment (Java 17 JDK, specific Alpine binaries) into an isolated, reproducible image.
*   **Docker Compose Orchestration:** Utilizing a `docker-compose.yml` file, the entire multi-tiered application—consisting of the Vue frontend running on Node/Nginx, the Spring Boot backend exposing port 8081, and the PostgreSQL database container exposing 5432—is bound directly into a localized network bridge. With a single command (`docker-compose up`), the entire stack deploys concurrently, handling the service health-checks to ensure the database boots completely before the backend attempts a JDBC connection.

# 4. SYSTEM ARCHITECTURE AND DATABASE DESIGN

## 4.1 Layered MVC Architecture
The backend application was designed adhering strictly to Model-View-Controller (MVC) and N-Tier architecture logic.
*   **Controller Layer:** RESTful endpoints exposed across mapping paths (e.g., `/api/sprints`). This layer securely accepts incoming HTTP requests from the Vue SPA, validates JSON structures using `jakarta.validation` annotations on incoming Data Transfer Objects (DTOs), and routes the process.
*   **Service Layer:** Contains the definitive business rules. For instance, within `AssignmentServiceImpl.java`, when a Vendor Admin attempts to allocate a task, the Service Layer intercepts the transaction to mathematically verify that the desired sprint is active and that the assignment's rank will not push the total developer load past the static 10-point limit.
*   **Data Access Layer (Repository):** Interfaces extending the Spring Data `JpaRepository` abstraction lock business models directly to database tables, handling standard CRUD operations and parameterized queries to prevent SQL Injection vulnerabilities.

## 4.2 Database Schema: PostgreSQL 
PostgreSQL, a highly advanced open-source relational database management system, was specifically chosen to persist the system's states. VMS relies heavily on foreign key referential integrity constraints to maintain its multi-tenant vendor isolation structures.

### Core Database Entities and Relationships

**1. `Users` Table**
The central authentication entity containing `username`, `password`, `email`, and `user_type` (`MANAGER`, `VENDOR_ADMIN`, `PERSONNEL`). It features a strictly enforced Foreign Key `vendor_id` linking back to the `Vendor` table. This allows the system to easily query `WHERE vendor_id = ?` to ensure that standard personnel developers and vendor admins exclusively retrieve rows belonging to their payroll organization.

**2. `Vendor` Table**
A high-level categorization entity capturing the `vendor_name`. Its primary role is to act as the relational bridge that segregates the system. Projects are assigned to Vendors; Users belong to Vendors. It establishes the tenant walls within the software architecture.

**3. `Project` Table**
Represents a specific client initiative created by the Product Manager. It holds `project_name` and contains two critical Foreign Keys: `project_vendor_id` (the company assigned to execute the project) and `project_manager_id` (the specific client official overseeing it). This One-to-Many mapping means one Vendor can handle multiple Projects, and one PM can oversee multiple Projects.

**4. `Sprint` Table**
Represents an Agile sprint bound strictly to a single Project. It records the `sprint_name`, `start_date`, `end_date`, an overarching `goal`, and a `status` (PLANNED, ACTIVE, COMPLETED). 
To assign individual developers to a sprint, a Many-to-Many relationship is required. This is resolved via an explicit junction table called `sprint_members` containing `sprint_id` and `user_id`. The backend algorithm strictly validates this junction table to enforce the rule: "One developer can only exist in ONE active sprint at a time across the entire system."

**5. `Assignment` (Task) Table**
The granular block of Agile work. It holds the string `assignment_name`, `status` (`TODO`, `IN_PROGRESS`, `IN_REVIEW`, `COMPLETED`), `priority`, and a point `rank`. Crucially, it possesses Foreign Keys that link it upstream to a `project_id`, a `sprint_id`, and a specific `assignee_id` (representing a User). This architecture permits complex cascading dashboard queries—such as fetching a pie-chart summation of all `COMPLETED` assignment ranks across a specific Project's history, enabling the performance calculation views.

# 5. ROLES AND FUNCTIONALITIES

The system features robust role-based access control, offering deeply specialized interfaces constructed specifically for three distinct user personas: the Project Manager, the Vendor Admin, and the Vendor Developer.

## 5.1 Project Manager (PM)
The Project Manager acts as the ultimate client-side administrator overseeing the entire procurement ecosystem. They are responsible for high-level vendor evaluation and system initiation.

*   `[Insert Screenshot: Project Manager Dashboard - showing overall system matrices, active projects, and top performing vendors]`
*   **Project Creation and Vendor Allocation:** The PM interface permits the rapid definition of distinct software projects. The PM assigns these projects exclusively to a specific Vendor entity. This initial connection dictates the flow of data architecture for every downstream task and sprint.
*   **Announcements Protocol:** The PM utilizes a specialized broadcast interface to post project-wide announcements. These are critical for communicating shifting core business requirements or strategic feedback directly to all vendor personnel executing that specific project, bypassing intermediary distortions.
*   **Performance Tracking Metrics:** The most analytically robust feature on the PM dashboard is the vendor performance tracker. The system aggregates all completed Assignment points (`rank`) tied to a vendor's projects and processes them. It outputs clear, mathematical point-based performance scores measuring velocity and delivery reliability. This decisively eliminates opinion-based performance reviews, replacing them with immutable, point-based metrics derived directly from the PostgreSQL tables.

## 5.2 Vendor Admin
The Vendor Admin acts as the operational director. They are an employee of the software supply firm, tasked with orchestrating their internal developer workforce to meet the PM's strict Agile demands.

*   `[Insert Screenshot: Vendor Admin Sprint Creation Interface - highlighting the developer multi-selection tool and automatic date constraint locking]`
*   **Agile Sprint Construction:** Vendor Admins possess the exclusive right to construct Sprints. VMS executes hard-coded Agile mechanics during this process: when mapping a sprint to a project, the Admin selects their developers. The system intercepts the `start_date` parameter and computationally binds the `end_date` exactly 13 days later, forcing a standard two-week sprint lifecycle across all vendors.
*   **Algorithmic Developer Capacity Planning:** While assigning tasks (`Assignments`) to developers within an upcoming sprint, the system validates the payload against a pre-programmed 10-point capacity matrix. If an Admin attempts to assign tasks to a developer whose combined task ranks exceed 10 points within a single sprint ID, the `AssignmentServiceImpl` explicitly throws a validation error, forcing realistic load planning out-of-the-box.
*   **Task Review Pipeline:** A defining feature of VMS is the Review Board. Vendor Admins manage a unique dashboard queried exclusively for tasks holding the `IN_REVIEW` status. When a developer completes feature coding, the task hits this board. The Admin must manually inspect the execution, utilizing the interface to either "Approve" (converting the points to finalized performance metrics on the PM's dashboard) or "Reject". Architectural rules enforce that a rejection payload fundamentally requires a mandatory `rejection_reason` string, ensuring documented feedback loops exist seamlessly within the platform.

## 5.3 Vendor Developer (Personnel)
Vendor Developers interact with a sleek, highly focused operational dashboard designed strictly to manage their immediate code-execution assignments, systematically stripping away all high-level administrative noise.

*   `[Insert Screenshot: Vendor Developer Kanban Board - showing individual task cards, point ranks, and draggable status columns]`
*   **Task Execution Workflow:** The developer dashboard features a standard Agile dragging Kanban layout reflecting their targeted tasks. The developer is responsible for moving assignments dynamically through states: from `TODO`, advancing into `IN_PROGRESS` (which computationally timestamps the exact initiation date in the database), and ultimately transferring the task into the `IN_REVIEW` queue.
*   **Contextual Operational Visibility:** To optimize cognitive load, the Vue frontend requests fetch limited arrays consisting strictly of assignments, active sprints, and announcements directly tied by internal Foreign Keys to that specific developer's scope. They can natively view the Vendor Admin's text rejection justifications directly attached to an assignment card that was pushed back, allowing them to rapidly iterate and re-submit the logic into the review pipeline without needing separate email threads or messaging platforms.

# 6. CONCLUSION AND RECOMMENDATIONS

## 6.1 Conclusion
The Vendor Management System project successfully prototypes, compiles, and orchestrates a highly structured, enterprise-grade software solution mapped strictly against the complex logistical realities of modern IT software procurement. By forcefully moving away from generically chaotic issue-trackers and implementing an ecosystem deeply aware of Client vs. Vendor ontological differences, strict mathematical Agile capacities, and completely isolated code review pipelines, VMS demonstrates that software outsourcing can be intrinsically transparent and dynamically trackable. 

The implementation of Java Spring Boot provided rigorous data integrity validations, while Vue.js allowed for highly responsive, lightweight state management on the client side. The utilization of PostgreSQL ensured transactional consistency across deep associative join tables, while Docker encapsulated the entire deployment process into an automated, error-free architecture. Ultimately, VMS drastically decreases the administrative communication overhead and systemic opacity traditionally inherent in multi-vendor outsourced development operations.

## 6.2 Future Work and Recommendations
While the current version of the Vendor Management System provides a completely functional end-to-end framework, future extensions could further digitize the complex vendor application lifecycle.
*   **Real-time WebSocket Notifications:** Integrating WebSocket protocol systems (such as Spring WebSockets acting with STOMP) to push instant updates. For instance, a developer could receive a real-time reactive browser push notification the instant a Vendor Admin rejects a task assignment, eliminating the necessity to refresh the standard HTTP protocol dashboard.
*   **Automated Payment Gateways:** The existing point-evaluation tracking algorithms could be hooked directly into enterprise financial REST APIs (like Stripe). This system could automatically generate vendor billing invoices, calculating final financial compensation based exclusively on the mathematical sum of `COMPLETED` points approved in a designated 30-day billing cycle.
*   **AI-Enhanced Preliminary Code Review:** Future upgrades could link the system directly with Git repositories. Moving a task into the `IN_REVIEW` status could organically trigger a webhook, allowing a Large Language Model architecture to perform an automated code syntax and standard analysis, flagging obvious structural flaws well before the feature hits the human Vendor Admin's review queue.

# 7. REFERENCES

*   Atlassian. (2020). *Agile Coach: What is Scrum?* Atlassian Corporation.
*   Fowler, M. (2014). *Microservices Architecture and Docker Containerization Benefits*. ThoughtWorks Blog. 
*   Lacity, M. C., & Willcocks, L. P. (2017). *Advanced Outsourcing Practice: Rethinking IT, BPO and Cloud Services*. Palgrave Macmillan. 
*   PostgreSQL Global Development Group. (2023). *PostgreSQL 17.0 Documentation: Referential Integrity and Foreign Keys*.
*   Spring Platform Documentation. (2023). *Spring Boot and Spring Data JPA Reference Guides*. Pivotal Software.
*   Vue.js Core Team. (2023). *Vue.js Documentation: Reactivity and Composition API*.

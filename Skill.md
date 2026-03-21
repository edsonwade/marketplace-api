You are a senior frontend architect and staff-level engineer.

I have a fully implemented backend with the following services and modules:

* Core configs: ApplicationConfig, SecurityConfiguration, RedisConfiguration, KafkaNotificationConfig
* Auth & security: JwtAuthenticationFilter, JwtService, LogoutService
* Controllers:
  AuthenticationController (AuthenticationRequest, AuthenticationResponse, RegisterRequest)
  CartController
  CustomerController
  DiscountController
  OrderController
  OrderItemController
  PaymentController
  ProductController
  ProductWebController
  StockController

Your task is to build a **production-grade, enterprise-level frontend architecture** using:

* React + TypeScript
* Tailwind CSS v4
* Nginx (deployment & routing)

Warning: Do NOT create your plan ( do not).follow this one is mandatory, build services one by one and make sure is high grade otherwise will be rejected!

### Requirements:

1. **Architecture**

   * Define a scalable folder structure for a large production React app
   * Include separation of concerns (domain-driven or feature-based)
   * Include state management strategy (global + server state)
   * Include API layer design mapped to all backend controllers
   * Include authentication flow using JWT (login, refresh, logout, protected routes)

2. **Feature Mapping**

   * Map every backend controller into frontend modules/pages/services
   * Define exact UI features for:

     * Authentication
     * Product browsing (web + admin)
     * Cart management
     * Orders & order items
     * Payments
     * Discounts
     * Customer profile
     * Stock management (admin)
   * Include role-based UI behavior if applicable

3. **Data Layer**

   * Define API client architecture (axios/fetch abstraction)
   * Define request/response typing strategy (strict TypeScript)
   * Define error handling, retries, and caching strategy

4. **State Management**

   * Define how server state is handled (React Query or equivalent)
   * Define global state (auth, cart, UI state)
   * Define persistence strategy (localStorage, cookies, secure storage)


5. ✅ Authentication & Security (Refined)

**5.1. Registration & Login Flow**

* Users register via dashboard → redirected to login (✔ correct)
* Consider **auto-login after registration** (better UX, optional)
* Always verify email (recommended)

---

**5.2. JWT Storage & Handling**

* ❗ Avoid storing JWT in `localStorage` (XSS risk)
* Prefer:

  * **HTTP-only cookies** (most secure for web apps)
* If using access + refresh tokens:

  * Access token → short-lived (memory or cookie)
  * Refresh token → HTTP-only, secure cookie


**5.3. Route Protection**

* Frontend:

  * Protect routes (e.g., React guards, middleware)
* Backend:

  * Always validate JWT (never trust frontend alone)
* Role-based access (RBAC) if needed

---

**5.4. Token Refresh Strategy**

* Use **short-lived access tokens** (e.g., 5–15 min)
* Use **refresh tokens** to issue new access tokens
* Implement:

  * Silent refresh (on app load / expiry)
  * Rotation of refresh tokens (extra security)

---

**5.5. Secure API Communication**

* Always use **HTTPS**
* Send tokens via:

  * Authorization header (`Bearer token`) OR cookies
* Rate limiting + request validation
* Avoid exposing sensitive data in responses

---

**5.6. CSRF / XSS Protection**

* **XSS:**

  * Sanitize user input
  * Avoid `dangerouslySetInnerHTML`
  * Use proper escaping

* **CSRF (if using cookies):**

  * Use CSRF tokens
  * SameSite cookies (`Strict` or `Lax`)

**5.7. Logout Handling**

* Clear tokens (client + server if possible)
* Invalidate refresh tokens

**5.8. Password Security**

* Hash passwords (e.g., bcrypt)
* Enforce strong password policy

**5.9. Account Protection**

* Rate limit login attempts
* Optional: 2FA (two-factor authentication)

**5.10. Token Expiry & Revocation**

* Handle expired tokens gracefully
* Blacklist or rotate compromised refresh tokens


6. **UI System**

   * Tailwind v4 design system structure
   * Component library structure (atoms, molecules, organisms or equivalent)
   * Theming and responsiveness
   * Accessibility considerations

7. **Real-time & Events**

   * Plan for Kafka-based notifications integration (via WebSockets or SSE)
   * Notification system design

8. **Performance**

   * Code splitting
   * Lazy loading
   * API optimization
   * Image and asset optimization

9. **Testing**

   * Unit testing strategy
   * Integration testing
   * E2E testing setup

10. **DevOps & Deployment**

* Nginx configuration strategy
* Environment configuration
* CI/CD pipeline outline
* Build optimization

11. **Code Standards**

* Naming conventions
* Linting and formatting
* Git structure and branching strategy

12. **Deliverables Format**

* Structured sections
* Clear diagrams (textual if needed)
* Concrete examples (file paths, sample code snippets)
* No vague explanations


Here’s the **missing add-on block** to append to your prompt (do not repeat previous content):

---

### **13. DEVOPS EXTENSIONS (MANDATORY ADDITIONS)**

#### **Frontend Dockerization**

* Provide a **production-grade Dockerfile** for the React + Vite app:

  * Multi-stage build (node build → nginx serve)
  * Optimized layer caching
  * Static build served via Nginx
  * Environment variable injection strategy

---

#### **Docker Compose Integration**

* Extend system with a **frontend service**:

  * Must depend on backend service
  * Proper networking between frontend and backend
  * Environment variables wired correctly
  * Expose correct ports
* Include full `docker-compose` service definition for frontend

---

#### **Nginx (Frontend Container)**

* Provide **complete nginx.conf**:

  * SPA fallback (`/index.html`)
  * API proxy to backend (`/api`)
  * Gzip/Brotli enabled
  * Cache headers for static assets
  * Security headers

---

#### **CORS Configuration (Backend Requirement)**

* Define exact backend CORS configuration required to allow frontend:

  * Allowed origins (dev + prod)
  * Allowed methods
  * Credentials (JWT)
  * Headers

---

#### **CI/CD (Frontend Specific)**

* Define pipeline steps:

  * install
  * lint
  * test
  * build
  * docker build
  * docker push
* Include environment-based builds (dev/staging/prod)
* Include cache optimization strategy

---

#### **Startup Orchestration**

* Ensure frontend waits for backend readiness:

  * healthcheck or wait-for strategy
  * no race conditions on startup

---

**Constraint:**
All of the above must be implemented with **real configs and code**, not descriptions.
The output must reflect **senior-level engineering standards**, scalability, maintainability, and production readiness.

The user is correcting my behavior. They're telling me to:
1. Never lie again
2. Not build anything without their instruction
3. Follow their instructions exactly
4. Always validate if I'm building exactly what they request
5. Ask before doing something
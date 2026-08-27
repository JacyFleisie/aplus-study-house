---
name: project-health-checker
description: project-health-checker skill
---

Project Health Checker
Goal
Act as a software project health and release-readiness auditor.
The primary purpose of this skill is to answer:
"Is this project actually ready to ship?"

Do not simply give a general opinion. Evaluate the project systematically, identify evidence-based problems, assign scores, determine release readiness, and provide a prioritized action plan.
The skill should work for:
- Mobile applications
- Web applications
- Websites
- Backend/API projects
- Desktop applications
- Full-stack applications
- React/React Native projects
- Expo applications
- Supabase projects
- Firebase projects
- Node.js applications
- Java/Python/etc. projects
- Projects containing both mobile and web clients
Core Workflow
When invoked, follow this process.
1. Determine What Is Being Audited
Identify:
- Project type
- Technology stack
- Frontend
- Backend
- Database
- Authentication
- External services
- Deployment platform
- Target platforms
- Current development stage
If the project information is incomplete, do not invent information.
Clearly distinguish between:
- Verified
- Partially verified
- Unknown
- Not applicable
Unknown information should reduce confidence in the assessment rather than being treated as passing.
2. Perform the Health Assessment
Evaluate the project using these categories:
Architecture — 15%
Evaluate:
- Project structure
- Separation of concerns
- Component/module organization
- Scalability
- Reusability
- Dependency structure
- Frontend/backend separation
- Database architecture
- API architecture
- State management
- Error boundaries
- Technical debt
Look for architectural decisions that may cause problems as the project grows.
Code Quality — 15%
Evaluate:
- Readability
- Naming
- Duplication
- Complexity
- Maintainability
- Error handling
- Consistency
- Dead code
- Hardcoded values
- Proper abstraction
- Dependency management
- Code organization
Do not penalize a project simply for using a different coding style.
Focus on whether the code is understandable, maintainable, and reliable.
Security — 20%
Security is one of the highest-priority categories.
Evaluate:
- Authentication
- Authorization
- Role-based access
- Database security
- RLS policies
- API security
- Secrets
- Environment variables
- Input validation
- File uploads
- Storage permissions
- Sensitive information exposure
- Client-side trust
- Privilege escalation
- Session management
- Password handling
- Dependency vulnerabilities
- Production configuration
For Supabase projects specifically inspect:
- Supabase Auth
- RLS
- Storage policies
- Database policies
- Public/anonymous access
- Service-role key exposure
- Client-side database access
- Edge Functions
- Database permissions
A project must not receive a "Ready" status if a critical security vulnerability remains unresolved.
Testing — 15%
Evaluate:
- Unit testing
- Integration testing
- End-to-end testing
- Manual testing
- Authentication testing
- Authorization testing
- Error-path testing
- Edge cases
- Device/browser testing
- Regression testing
- Production testing
- Database testing
- API testing
Look for missing tests around important functionality.
Do not reward a project simply because it has many tests.
Test quality and coverage of important functionality matter more than test count.
Documentation — 10%
Evaluate:
- README
- Setup instructions
- Environment configuration
- Architecture documentation
- API documentation
- Database documentation
- Deployment instructions
- User documentation
- Admin documentation
- Troubleshooting information
- Known limitations
Documentation should be sufficient for another developer to understand and operate the project.
Deployment & Production Readiness — 10%
Evaluate:
- Production environment
- Environment variables
- Build configuration
- Hosting
- Database migrations
- Production database
- Production API endpoints
- App signing
- App store configuration
- Domain configuration
- HTTPS
- Backups
- Monitoring
- Logging
- Error reporting
- Rollback strategy
For mobile projects, also consider:
- APK/AAB build
- Android permissions
- iOS configuration where applicable
- App icons
- Splash screen
- Version number
- Build number
- Store metadata
- Production environment configuration
Performance & Reliability — 5%
Evaluate:
- Loading performance
- Database performance
- Network requests
- Memory usage
- Rendering performance
- API response times
- Error recovery
- Offline behaviour where applicable
- Retry behaviour
- Race conditions
- Crash risks
- Resource usage
Performance problems should be prioritized based on their real-world impact.
Maintainability — 5%
Evaluate:
- Ease of future changes
- Dependency management
- Technical debt
- Configuration management
- Code consistency
- Reusability
- Upgradeability
- Developer onboarding
- Project organization
3. Score Each Category
Use a score from 0–100%.
Interpret scores as:
Score	Meaning
90–100	Excellent
80–89	Good
70–79	Acceptable
60–69	Needs Improvement
40–59	Poor
0–39	Critical


Calculate the overall score using the category weights.
Do not inflate scores.
A project with unknown security configuration should not receive a high security score merely because no vulnerability has been observed.
4. Apply Release Gates
The overall percentage is not enough to determine whether a project is ready.
Apply these release rules.
Critical Security Gate
If there is a confirmed critical security vulnerability:
STATUS: NOT READY

regardless of the overall score.
Critical Functionality Gate
If a core feature is broken or unusable:
STATUS: NOT READY

Examples:
- Users cannot log in
- Payments cannot be processed
- Core data cannot be saved
- Major user role cannot access required functionality
- Application crashes during normal usage
Testing Gate
If critical functionality has not been adequately tested:
STATUS: NOT READY

Deployment Gate
If the application cannot reliably be deployed to its intended production environment:
STATUS: NOT READY

5. Determine Overall Status
Use the following statuses:
🟢 READY
Use when:
- Overall score ≥ 85%
- No critical security issues
- No critical functionality issues
- Core functionality has been tested
- Production deployment is verified
🟡 READY WITH WARNINGS
Use when:
- Overall score is 75–84%
- No critical blockers exist
- Remaining issues are low or medium priority
The project can potentially ship, but improvements are recommended.
🟠 NOT READY
Use when:
- Overall score is below 75%
- Important functionality remains incomplete
- Testing is insufficient
- Security requires significant work
- Deployment is incomplete
🔴 BLOCKED
Use when:
- Critical security vulnerability exists
- Critical functionality is broken
- Production deployment is fundamentally unsafe
- Data loss is possible
- Authentication/authorization is fundamentally compromised
6. Produce the Health Report
Always structure the final report like this:
PROJECT HEALTH

Architecture       XX%  [status]
Code Quality       XX%  [status]
Security           XX%  [status]
Testing            XX%  [status]
Documentation     XX%  [status]
Deployment         XX%  [status]
Performance        XX%  [status]
Maintainability    XX%  [status]

────────────────────────────

OVERALL             XX%

STATUS: [READY / READY WITH WARNINGS / NOT READY / BLOCKED]

CONFIDENCE: [HIGH / MEDIUM / LOW]
Then provide:
🚨 Critical Issues
List issues that must be addressed before release.
Each issue should contain:
- Problem
- Impact
- Evidence
- Recommended action
⚠️ Important Issues
List significant issues that should be addressed soon.
💡 Improvements
List non-critical improvements.
Do not treat cosmetic improvements as release blockers.
7. Top 5 Things to Fix
Always finish the assessment with:
TOP 5 THINGS TO FIX BEFORE RELEASE

1. [Highest priority]
2. [Second priority]
3. [Third priority]
4. [Fourth priority]
5. [Fifth priority]
Prioritize according to:
1. Security
2. Data loss
3. Critical functionality
4. Reliability
5. Testing
6. Deployment
7. Performance
8. Maintainability
9. Documentation
10. Cosmetic improvements
Do not prioritize tasks merely because they are easy.
8. Give a Release Checklist
After the report, provide a concise final checklist:
RELEASE CHECKLIST

[ ] No critical security issues
[ ] Authentication verified
[ ] Authorization verified
[ ] Core features tested
[ ] Error handling tested
[ ] Database security verified
[ ] Production environment verified
[ ] Environment variables verified
[ ] Backups configured
[ ] Deployment tested
[ ] Monitoring/logging configured
[ ] Documentation completed
[ ] Final regression test completed
Add or remove checklist items depending on the project.
Important Rules
Never Pretend Something Was Verified
If you cannot inspect something, say:
Not verified

Do not say:
"The project is secure."

Instead say:
"No security vulnerability was identified in the inspected areas, but security cannot be fully verified without testing the production environment."

Separate Evidence From Assumptions
Use:
Verified
for information directly observed.
Use:
Unknown
when information is unavailable.
Use:
Risk
when something appears potentially problematic but requires further investigation.
Do Not Give False Precision
Scores should represent an informed assessment, not mathematical certainty.
For example:
Security: 72%

should be accompanied by the major reasons for the score.
Prioritize Real Risks
Do not overwhelm the user with hundreds of minor suggestions.
A project health check should answer:
What could actually hurt this project, and what should I fix first?

Continuous Project Health Mode
If the project is being developed over multiple sessions, maintain the concept of a Project Health Baseline.
When a previous health report is available, compare the current assessment against it.
Example:
PROJECT HEALTH TREND

Previous: 68%
Current: 79%

Improvement: +11%

Security:       61% → 78%  +17
Testing:        48% → 65%  +17
Documentation:  42% → 55%  +13
Deployment:     70% → 82%  +12
Identify:
- Improvements
- Regressions
- Newly introduced risks
- Resolved issues
- Remaining blockers
This allows the skill to function as an ongoing project health monitor, not just a one-time audit.

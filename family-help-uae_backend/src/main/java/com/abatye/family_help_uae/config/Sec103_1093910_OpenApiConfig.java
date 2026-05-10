package com.abatye.family_help_uae.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger UI configuration for the FamilyHelpUAE platform.
 *
 * <p>
 * Defines the API title, comprehensive description (rendered as Markdown in
 * Swagger UI),
 * JWT bearer security scheme, and server URL. The description covers all five
 * required
 * documentation areas: endpoint overview, authentication workflow, platform
 * interaction flow,
 * trust/reputation algorithm, and usage examples.
 * </p>
 *
 * <h3>How to use Swagger UI</h3>
 * <ol>
 * <li>Start the application.</li>
 * <li>Open {@code http://localhost:8080/swagger-ui/index.html}.</li>
 * <li>Click <b>Authorize 🔒</b> and paste the JWT from
 * {@code POST /api/auth/signin}.</li>
 * <li>All secured endpoints can now be tested directly in the browser.</li>
 * </ol>
 */
@Configuration
@OpenAPIDefinition(info = @Info(title = "FamilyHelpUAE\u2122 \u2014 Community Family Support Platform", version = "1.0.0", description = "A distributed, family-centric RESTful API that enables families to offer and request "
        + "practical support services within trusted local communities. "
        + "Built with Spring Boot, JWT-based stateless authentication, Caffeine in-memory caching, "
        + "and a Bayesian reputation algorithm for transparent, abuse-resistant trust scoring.\n\n"

        + "---\n\n"

        + "## \uD83D\uDD10 Authentication Workflow\n\n"
        + "All endpoints except `/api/auth/**` require a valid JWT bearer token. "
        + "Click **Authorize \uD83D\uDD12** at the right side of this page and paste your token.\n\n"
        + "```\n"
        + "  STEP 1 \u2014 Register\n"
        + "  Client  \u2500\u2500 POST /api/auth/signup \u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u25b6  Server\n"
        + "          { familyName, email, password }         201 Created\n\n"
        + "  STEP 2 \u2014 Login\n"
        + "  Client  \u2500\u2500 POST /api/auth/signin \u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u25b6  Server\n"
        + "          { email, password }                     { token: \"eyJ...\" }\n\n"
        + "  STEP 3 \u2014 Use the token on every subsequent request\n"
        + "  Client  \u2500\u2500 GET /api/offers \u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u25b6  Server\n"
        + "          Authorization: Bearer eyJ...            200 OK\n"
        + "```\n\n"

        + "---\n\n"

        + "## Platform Interaction Flow\n\n"
        + "```\n"
        + "  Family A (Helper)           System              Family B (Requester)\n"
        + "       |\u2500\u2500 POST /api/offers \u2500\u2500\u2500\u2500\u2500\u2500\u25b6|  Offer: OPEN          |\n"
        + "       |                        |\u25c0\u2500 GET  /api/offers \u2500\u2500\u2500\u2500\u2500|  Browse\n"
        + "       |                        |\u25c0\u2500 POST /api/tasks \u2500\u2500\u2500\u2500\u2500|  Accept offer\n"
        + "       |                        |   Task: ACTIVE       |\n"
        + "       |\u25c0\u2500 POST /api/messages \u2500\u2500\u2500\u2500|  Communicate         |\n"
        + "       |                        |\u25c0\u2500 PUT  /api/tasks/{id}|  Mark COMPLETED\n"
        + "       |                        |   [Reputation        |\n"
        + "       |                        |    recalculated]     |\n"
        + "       |                        |\u25c0\u2500 POST /api/feedback \u2500\u2500|  Rate the helper\n"
        + "       |\u25c0\u2500 GET /api/reputations/{id}\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500|  View trust score\n"
        + "```\n\n"

        + "---\n\n"

        + "## \u2B50 Trust & Reputation Algorithm (Bayesian Average)\n\n"
        + "The platform uses a **Bayesian Average** to prevent score manipulation "
        + "by families with very few reviews. A simple average would let a family with "
        + "one 5-star review rank above a family with 100 solid reviews.\n\n"
        + "```\n"
        + "  STEP 1 \u2014 Bayesian Rating (pulls new families toward the global mean)\n"
        + "    bayesianRating = (C * m + n * avgRating) / (C + n)\n\n"
        + "  STEP 2 \u2014 Task Completion Bonus (rewards active helpers)\n"
        + "    taskBonus = min(completedTasks / 10, 1.0) * 0.5\n\n"
        + "  STEP 3 \u2014 Final Score clamped to [0.0, 5.0]\n"
        + "    reliabilityScore = clamp(bayesianRating + taskBonus, 0, 5.0)\n\n"
        + "  Parameters:\n"
        + "    C         = 5.0  (confidence weight)\n"
        + "    m         = platform-wide mean rating (cached 5 min, falls back to 3.0)\n"
        + "    n         = number of reviews this family received\n"
        + "    avgRating = mean of received ratings (1-5 scale)\n\n"
        + "  Score behaviour:\n"
        + "    n = 0  reviews  -> score = m  (global mean, prevents cold-start fraud)\n"
        + "    n = 5  reviews  -> score = 50/50 blend of m and own average\n"
        + "    n = 50 reviews  -> own average dominates (91% weight)\n"
        + "    taskBonus capped at +0.5 to prevent task-farming abuse\n"
        + "Scores are recalculated automatically on every feedback submit or task completion. "
        + "Results are cached in-memory (Caffeine, 10-min TTL) to reduce DB load at scale.\n"
        + "```\n\n"

        + "---\n\n", contact = @Contact(name = "Section 103 \u2014 1093910", email = "abatyemelie011@gmail.com")), servers = @Server(url = "https://localhost:8443", description = "Local development server"))
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT", description = "Paste the JWT token returned by POST /api/auth/signin. "
        + "Valid for 24 hours (configurable via JWT_EXPIRATION_MS env variable).")
public class Sec103_1093910_OpenApiConfig {
    // No beans required — all configuration is driven by the class-level
    // annotations above.
}

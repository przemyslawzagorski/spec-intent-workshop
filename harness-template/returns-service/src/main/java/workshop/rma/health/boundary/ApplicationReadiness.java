package workshop.rma.health.boundary;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

import jakarta.enterprise.context.ApplicationScoped;

/** Gotowosc aplikacji do przyjmowania ruchu. */
@Readiness
@ApplicationScoped
public class ApplicationReadiness implements HealthCheck {

    @Override
    public HealthCheckResponse call() {
        return HealthCheckResponse.up("returns-service");
    }
}

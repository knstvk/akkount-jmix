package akkount.service;

import io.jmix.data.persistence.DbmsType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class AppLifecycle {

    private static final Logger log = LoggerFactory.getLogger(AppLifecycle.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DbmsType dbmsType;

    @Value("${akk.shutdown-database-on-exit:false}")
    private String shutdownDatabaseOnExit;

    @EventListener(ContextClosedEvent.class)
    public void applicationStopped() {
        if ("hsql".equals(dbmsType.getType()) && Boolean.parseBoolean(shutdownDatabaseOnExit)) {
            log.info("Shutting down the HSQL database");
            jdbcTemplate.update("shutdown");
        }
    }
}

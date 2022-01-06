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

@Component("akk_AppLifecycle")
public class AppLifecycle {

    private Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DbmsType dbmsType;

    @Value("akk.shutdownDatabaseOnExit")
    private String shutdownDatabaseOnExit;

    @EventListener(ContextClosedEvent.class)
    public void applicationStopped() {
        if ("hsql".equals(dbmsType.getType()) && Boolean.parseBoolean(shutdownDatabaseOnExit)) {
            log.info("Shutting down the HSQL database");
            jdbcTemplate.update("shutdown");
        }
    }
}

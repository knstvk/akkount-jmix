package akkount;

import com.google.common.base.Strings;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;

@Push
@Theme(value = "akkount")
@PWA(name = "Akkount", shortName = "Akkount")
@SpringBootApplication
@ConfigurationPropertiesScan
public class AkkountApplication extends SpringBootServletInitializer implements AppShellConfigurator {

	@Autowired
	private Environment environment;

	public static void main(String[] args) {
		SpringApplication.run(AkkountApplication.class, args);
	}

	@Bean
	@Primary
	@ConfigurationProperties("main.datasource")
	DataSourceProperties dataSourceProperties() {
		return new DataSourceProperties();
	}

	@Bean
	@Primary
	@ConfigurationProperties("main.datasource.hikari")
	DataSource dataSource(DataSourceProperties dataSourceProperties) {
		return dataSourceProperties.initializeDataSourceBuilder().build();
	}

	@EventListener
	public void printApplicationUrl(ApplicationStartedEvent event) {
		LoggerFactory.getLogger(AkkountApplication.class).info("{user.dir} is "	+ SystemUtils.getUserDir());

		LoggerFactory.getLogger(AkkountApplication.class).info("Application started at "
				+ "http://localhost:"
				+ environment.getProperty("local.server.port")
				+ Strings.nullToEmpty(environment.getProperty("server.servlet.context-path")));
	}
}

package akkount;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.sql.DataSource;

@SpringBootApplication
public class AkkountApplication {

	public static void main(String[] args) {
		SpringApplication.run(AkkountApplication.class, args);
	}

	@Bean
	@Primary
	@ConfigurationProperties(prefix="main.datasource")
	DataSource dataSource() {
		return DataSourceBuilder.create().build();
	}

	@Bean
	public WebMvcConfigurer forwardFrontendToIndex() {
		return new WebMvcConfigurer() {
			@Override
			public void addViewControllers(ViewControllerRegistry registry) {
				registry.addRedirectViewController("/front", "/front/index.html");
			}
		};
	}
}

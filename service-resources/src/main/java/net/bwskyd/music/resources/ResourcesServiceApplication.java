package net.bwskyd.music.resources;

import net.bwskyd.music.resources.config.properties.StorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
@EnableConfigurationProperties(StorageProperties.class)
@EntityScan("net.bwskyd.music.entity")
public class ResourcesServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ResourcesServiceApplication.class, args);
	}

}

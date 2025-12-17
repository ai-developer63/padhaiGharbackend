package app.nepaliapp.padhaighar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync      // Enables the Admin Cleanup background task
@EnableScheduling
public class PadhaiGharApplication {

	public static void main(String[] args) {
		SpringApplication.run(PadhaiGharApplication.class, args);
	}

}

package processing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ProcessingApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProcessingApplication.class, args);
	}

}

package org.iprosoft.trademarks.aws.artefacts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportRuntimeHints;

@SpringBootApplication
@ImportRuntimeHints(AwsRuntimeHints.class)
public class AwsApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(AwsApiApplication.class, args);
	}

}

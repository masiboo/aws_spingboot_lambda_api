package org.iprosoft.trademarks.aws.artefacts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;

@TestConfiguration(proxyBeanMethods = false)
class TestAwsApiApplication {

	public static void main(String[] args) {
		SpringApplication.from(AwsApiApplication::main).with(TestAwsApiApplication.class).run(args);
	}

}

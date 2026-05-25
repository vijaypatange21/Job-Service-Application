package com.jobapp.api_gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"eureka.client.enabled=false",
		"gateway.security.jwt.issuer=jobapp-authms",
		"gateway.security.jwt.secret=change-this-dev-only-secret-key-change-this-dev-only-secret-key"
})
class ApiGatewayApplicationTests {

	@Test
	void contextLoads() {
	}

}

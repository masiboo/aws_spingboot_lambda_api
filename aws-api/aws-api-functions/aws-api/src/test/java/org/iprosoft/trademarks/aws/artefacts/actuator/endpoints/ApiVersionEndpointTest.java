package org.wipo.trademarks.Aws.artefacts.actuator.endpoints;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest
@AutoConfigureMockMvc
public class ApiVersionEndpointTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	public void testApiVersionEndPoint() throws Exception {
		String ACTUATOR_ENDPOINT = "/actuator/apiversion";
		mockMvc.perform(MockMvcRequestBuilders.get(ACTUATOR_ENDPOINT))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.jsonPath("$.coreVersion").exists())
			.andExpect(MockMvcResultMatchers.jsonPath("$.apiVersion").exists());
	}

}

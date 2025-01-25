package com.example.restservice.greeting;

import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class GreetingControllerTests {

	@Autowired
	private MockMvc mockMvc;

	private AtomicLong counter;

	@BeforeEach
	public void setup() {
		counter = new AtomicLong(1); // Start at 1 for testing increment
	}

	@Test
	public void noParamGreetingShouldReturnDefaultMessage() throws Exception {
		mockMvc.perform(get("/greeting"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.Content").value("Hello, World!"));

	}

	@Test
	public void paramGreetingShouldReturnTailoredMessage() throws Exception {
		String name = "Spring Community";
		String expectedGreeting = "Hello, " + name + "!";

		mockMvc.perform(get("/greeting").param("name", name))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(2))
				.andExpect(jsonPath("$.Content").value(expectedGreeting));
	}

}

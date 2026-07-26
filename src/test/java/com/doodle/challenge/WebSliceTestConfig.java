package com.doodle.challenge;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

// separate @SpringBootConfiguration root (not DoodleApplication) because pointing a @WebMvcTest slice at the
// real application class scans the whole flat package tree and pulls in the real SecurityConfig, which then
// fails building its Basic-auth filter chain - controller tests @Import exactly the controller under test
// plus GlobalExceptionHandler instead
@SpringBootConfiguration
@EnableAutoConfiguration
public class WebSliceTestConfig {
}

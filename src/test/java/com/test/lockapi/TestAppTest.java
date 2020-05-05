package com.test.lockapi;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestAppTest {
	
	@Test
	@DisplayName("Verify the increment operation output")
	public void testIncrementOperation() {
		System.out.println("running tests!!");
		assertEquals(82, TestApp.testIncrementOperation());
	}

}

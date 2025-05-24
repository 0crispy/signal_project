package com.cardio_generator.generators;

import com.cardio_generator.outputs.OutputStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AlertGeneratorTest {
    private AlertGenerator generator;
    private OutputStrategy mockOutput;

    @BeforeEach
    void setUp() {
        generator = new AlertGenerator(5);
        mockOutput = Mockito.mock(OutputStrategy.class);
    }

    @Test
    void constructor_InitializesAllAlertsFalse() {
        for (int i = 1; i <= 5; i++) {
            assertFalse(generator.AlertStates[i]);
        }
    }

    @Test
    void generate_CanTriggerAlert() {
        // Force trigger by mocking random
        AlertGenerator.randomGenerator = new Random() {
            @Override
            public double nextDouble() {
                return 0.05; // Below threshold to trigger
            }
        };

        generator.generate(1, mockOutput);
        assertTrue(generator.AlertStates[1]);
        verify(mockOutput).output(eq(1), anyLong(), eq("Alert"), eq("triggered"));
    }

    @Test
    void generate_CanResolveAlert() {
        // Set alert to true first
        generator.AlertStates[1] = true;

        // Force resolve by mocking random
        AlertGenerator.randomGenerator = new Random() {
            @Override
            public double nextDouble() {
                return 0.8; // Below 0.9 threshold to resolve
            }
        };

        generator.generate(1, mockOutput);
        assertFalse(generator.AlertStates[1]);
        verify(mockOutput).output(eq(1), anyLong(), eq("Alert"), eq("resolved"));
    }
}
package com.cardio_generator.generators;

import com.cardio_generator.outputs.OutputStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BloodSaturationDataGeneratorTest {
    private BloodSaturationDataGenerator generator;
    private OutputStrategy mockOutput;

    @BeforeEach
    void setUp() {
        generator = new BloodSaturationDataGenerator(5);
        mockOutput = Mockito.mock(OutputStrategy.class);
    }

    @Test
    void constructor_InitializesValuesWithinRange() {
        for (int i = 1; i <= 5; i++) {
            assertTrue(generator.lastSaturationValues[i] >= 95 && generator.lastSaturationValues[i] <= 100);
        }
    }

    @Test
    void generate_ProducesValidValues() {
        generator.generate(1, mockOutput);
        assertTrue(generator.lastSaturationValues[1] >= 90 && generator.lastSaturationValues[1] <= 100);
    }

    @Test
    void generate_OutputsCorrectFormat() {
        generator.generate(1, mockOutput);
        verify(mockOutput).output(
                eq(1), anyLong(), eq("Saturation"), argThat(s -> s.endsWith("%")));
    }
}
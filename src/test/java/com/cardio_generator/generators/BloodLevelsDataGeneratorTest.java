package com.cardio_generator.generators;

import com.cardio_generator.outputs.OutputStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BloodLevelsDataGeneratorTest {
    private BloodLevelsDataGenerator generator;
    private OutputStrategy mockOutput;

    @BeforeEach
    void setUp() {
        generator = new BloodLevelsDataGenerator(5);
        mockOutput = Mockito.mock(OutputStrategy.class);
    }

    @Test
    void constructor_InitializesValuesWithinRange() {
        for (int i = 1; i <= 5; i++) {
            assertTrue(generator.baselineCholesterol[i] >= 150 && generator.baselineCholesterol[i] <= 200);
            assertTrue(generator.baselineWhiteCells[i] >= 4 && generator.baselineWhiteCells[i] <= 10);
            assertTrue(generator.baselineRedCells[i] >= 4.5 && generator.baselineRedCells[i] <= 6.0);
        }
    }

    @Test
    void generate_ProducesThreeOutputs() {
        generator.generate(1, mockOutput);
        verify(mockOutput, times(3)).output(anyInt(), anyLong(), anyString(), anyString());
    }
}
package com.cardio_generator.generators;

import com.cardio_generator.outputs.OutputStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ECGDataGeneratorTest {
    private ECGDataGenerator generator;
    private OutputStrategy mockOutput;

    @BeforeEach
    void setUp() {
        generator = new ECGDataGenerator(5);
        mockOutput = Mockito.mock(OutputStrategy.class);
    }

    @Test
    void constructor_InitializesZeroValues() {
        for (int i = 1; i <= 5; i++) {
            assertEquals(0.0, generator.lastEcgValues[i]);
        }
    }

    @Test
    void generate_ProducesOutput() {
        generator.generate(1, mockOutput);
        verify(mockOutput).output(anyInt(), anyLong(), eq("ECG"), anyString());
    }

    @Test
    void simulateEcgWaveform_ReturnsValidValue() {
        double value = generator.simulateEcgWaveform(1, 0.0);
        assertNotEquals(0.0, value);
    }
}
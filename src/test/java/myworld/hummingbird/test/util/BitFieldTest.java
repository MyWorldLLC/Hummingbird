package myworld.hummingbird.test.util;

import myworld.hummingbird.util.BitField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BitFieldTest {

    private BitField bitField;

    @BeforeEach
    public void setupBitField(){
        bitField = new BitField(128);
    }

    @Test
    public void setAndClearBit(){
        bitField.set(62);
        assertTrue(bitField.isSet(62), "Failed to set bit");

        assertEquals(1L << 1, bitField.word(0), "Bit not set in the right place");

        bitField.clear(62);
        assertFalse(bitField.isSet(62), "Failed to clear bit");
    }

    @Test
    public void setEdgeBits(){
        // word 0 bit 0
        bitField.set(0);
        assertEquals(1L << 63, bitField.word(0), "Bit not set in the right place");
        bitField.clear(0);

        // word 0 bit 63
        bitField.set(63);
        assertEquals(1L, bitField.word(0), "Bit not set in the right place");
        bitField.clear(0);

        // word 1 bit 0
        bitField.set(64);
        assertEquals(1L << 63, bitField.word(1), "Bit not set in the right place");
        bitField.clear(64);

        // word 1 bit 63
        bitField.set(127);
        assertEquals(1L, bitField.word(1), "Bit not set in the right place");
        bitField.clear(127);
    }

}

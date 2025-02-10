package myworld.hummingbird;

import java.io.DataOutputStream;
import java.io.IOException;

public class Binary {

    public enum Section {
        DATA,
        SYMBOLS,
        CODE
    }

    public void encode(Opcode code, DataOutputStream os) throws IOException {

        /*var fields = new int[]{code.dst(), code.src(),
                code.extra(), code.extra1()};

        var trailingZeroes = 0;
        for(int i = fields.length - 1; i >= 0; i--){
            if(fields[i] == 0){
                trailingZeroes++;
            }else{
                break;
            }
        }

        // Opcode encoding:
        // 2-byte header:
        //    0: how many fields are encoded
        //    1: how many bytes are used to store each field

        final var encodedFields = fields.length - trailingZeroes;
        final var bytesPerField = byteWidthToEncode(fields);
        os.writeByte(encodedFields);
        os.writeByte(bytesPerField);

        for(int i = 0; i < encodedFields; i++){
            var field = fields[i];
            for(int remaining = bytesPerField; remaining > 0; remaining--){
                os.writeByte(getByte(field, remaining - 1));
            }
        }*/

    }

    protected int getByte(int value, int index){
        // index into an int as if it were an array of bytes with indices [3, 2, 1, 0],
        // and shift the result back into the low byte
        var mask = 0xFF << (index * 8);
        return 0xFF & ((value & mask) >> (index * 8));
    }

    protected int byteWidthToEncode(int[] values){
        var byteWidth = 0;

        for(int value : values){
            var mask = 0xFF << 24;
            for(int i = 0; i < 4; i++){
                // Move one-byte mask across the integer left to right,
                // stopping once we hit a non-zero byte
                if((mask & value) != 0){
                    byteWidth += 1;
                }
                mask >>>= 8; // Shift must be unsigned because of the sign bit initially being set
            }
        }

        return byteWidth;
    }

}

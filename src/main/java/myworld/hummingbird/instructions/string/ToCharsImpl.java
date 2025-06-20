package myworld.hummingbird.instructions.string;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.instructions.OpcodeImpl;

public class ToCharsImpl implements OpcodeImpl {
    @Override
    public int apply(Fiber fiber, Opcode ins, int ip, Opcode[] instructions) {
        //var reg = fiber.registers;
        var vm = fiber.vm;
        // TODO
        /*var charBuf = vm.memoryAsCharBuffer();

        var dst = (int) reg[regOffset + ins.dst()];
        var src = (int) reg[regOffset + ins.src()];

        if (fiber.vm.readObj(src) instanceof String s) {
            var chars = s.toCharArray();
            vm.writeInt(dst, chars.length);
            charBuf.put((dst + 4) / 2, chars);
        }else{
            vm.writeInt(dst, -1);
        }*/

        return ip + 1;
    }
}

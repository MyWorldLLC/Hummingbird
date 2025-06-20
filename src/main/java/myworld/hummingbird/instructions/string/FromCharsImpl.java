package myworld.hummingbird.instructions.string;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.instructions.OpcodeImpl;

public class FromCharsImpl implements OpcodeImpl {
    @Override
    public int apply(Fiber fiber, Opcode ins, int ip, Opcode[] instructions) {

        /*var reg = fiber.registers;
        var vm = fiber.vm;

        var src = (int) reg[regOffset + ins.src()];
        var dst = (int) reg[regOffset + ins.dst()];

        fiber.vm.writeObj(dst, vm.readString(src));*/

        return ip + 1;
    }
}

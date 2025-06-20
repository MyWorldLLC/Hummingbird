package myworld.hummingbird.instructions.string;

import myworld.hummingbird.Fiber;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.instructions.OpcodeImpl;

public class SubStrImpl implements OpcodeImpl {
    @Override
    public int apply(Fiber fiber, Opcode ins, int ip, Opcode[] instructions) {

        /*var reg = fiber.registers;
        var vm = fiber.vm;

        var dst = (int) reg[regOffset + ins.dst()];
        var src = (int) reg[regOffset + ins.src()];

        var start = (int) reg[regOffset + ins.extra()];
        var end = (int) reg[regOffset + ins.extra1()];

        var str = vm.objectToString(src);

        vm.writeObj(dst, str.substring(Math.max(0, start), Math.min(str.length(), end)));*/

        return ip + 1;
    }
}

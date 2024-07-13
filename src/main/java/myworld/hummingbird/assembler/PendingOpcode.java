package myworld.hummingbird.assembler;

import java.util.List;

public record PendingOpcode(int index, String name, List<Object> operands) {}

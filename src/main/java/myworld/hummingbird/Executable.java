package myworld.hummingbird;

import java.util.ArrayList;
import java.util.List;

public record Executable(byte[] data, Symbol[] symbols, Opcode[] code) {

    public static Builder builder(){
        return new Builder();
    }

    public static class Builder {

        protected final List<byte[]> data = new ArrayList<>();
        protected final List<Symbol> symbols = new ArrayList<>();
        protected final List<Opcode> opcodes = new ArrayList<>();

        public int appendData(byte[] data){
            var offset = 0;
            for(var prior : this.data){
                offset += prior.length;
            }
            this.data.add(data);
            return offset;
        }

        public int appendSymbol(Symbol symbol){
            var offset = symbols.size();
            symbols.add(symbol);
            return offset;
        }

        public void replaceSymbol(int index, Symbol symbol){
            symbols.set(index, symbol);
        }

        public int indexOfNextSymbol(){
            return symbols.size();
        }

        public int appendOpcode(Opcode opcode){
            var offset = opcodes.size();
            opcodes.add(opcode);
            return offset;
        }

        public void replaceOpcode(int index, Opcode opcode){
            opcodes.set(index, opcode);
        }

        public int indexOfNextOpcode(){
            return opcodes.size();
        }

        protected byte[] joinData(){
            var dataSize = this.data.stream()
                    .map(d -> d.length)
                    .reduce(0, Integer::sum);

            var dataSection = new byte[dataSize];
            var offset = 0;
            for(var bytes : this.data){
                System.arraycopy(bytes, 0, dataSection, offset, bytes.length);
                offset += bytes.length;
            }
            return dataSection;
        }

        public Executable build(){
            return new Executable(
                    joinData(),
                    symbols.toArray(Symbol[]::new),
                    opcodes.toArray(Opcode[]::new)
            );
        }

    }
}

package myworld.hummingbird.assembler;

import java.nio.ByteBuffer;

public class DataBufferEncoder {

    protected ByteBuffer buffer;

    public DataBufferEncoder(){
        buffer = ByteBuffer.allocate(1024);
    }

    public void write(Object o) throws AssemblyException {
        if(o instanceof Integer i){
            buffer.putInt(i);
        }else if(o instanceof Float f){
            buffer.putFloat(f);
        }else if(o instanceof Long l){
            buffer.putLong(l);
        }else if(o instanceof Double d){
            buffer.putDouble(d);
        }else if(o instanceof String s){
            buffer.putInt(s.length());
            for(char c : s.toCharArray()){
                buffer.putChar(c);
            }
        }else{
            throw new AssemblyException("Not a valid literal type: " + o.getClass().getName());
        }
    }

    public int indexOfNextWrite(){
        return buffer.position();
    }

    protected void grow(){
        var newBuffer = ByteBuffer.allocate(buffer.capacity() + 1024);
        newBuffer.put(buffer);
        buffer = newBuffer;
    }

    public byte[] toArray(){
        var result = new byte[buffer.position()];
        System.arraycopy(buffer.array(), 0, result, 0, buffer.position());
        return result;
    }
}

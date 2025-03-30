package myworld.hummingbird.util;

public final class BitField {

    private static final long MASK = 1L << 63;

    private long[] words;

    public BitField(int initialSize){
        words = new long[initialSize / 64 + (initialSize % 64 != 0 ? 1 : 0)];
    }

    public void resize(int newSize){
        var next = new long[newSize];
        System.arraycopy(words, 0, next, 0, Math.min(words.length, newSize));
        words = next;
    }

    public void set(int bitIndex){
        var wordIndex = bitIndex / 64;
        words[wordIndex] |= MASK >>> (bitIndex % 64);
    }

    public boolean isSet(int bitIndex){
        var wordIndex = bitIndex / 64;
        return (words[wordIndex] & (MASK >>> (bitIndex % 64))) != 0;
    }

    public void clear(int bitIndex){
        var wordIndex = bitIndex / 64;
        words[wordIndex] ^= MASK >>> (bitIndex % 64);
    }

    public long wordFor(int bitIndex){
        return words[bitIndex / 64];
    }

    public long word(int wordIndex){
        return words[wordIndex];
    }

    public int wordCount(){
        return words.length;
    }

    public int bitCount(){
        return words.length * 64;
    }

}

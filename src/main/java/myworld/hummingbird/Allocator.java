package myworld.hummingbird;

public interface Allocator {

    int malloc(int size);
    void free(int ptr);
    int freeSpace();
}

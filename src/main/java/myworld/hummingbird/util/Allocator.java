package myworld.hummingbird.util;

import myworld.hummingbird.HummingbirdVM;

import static myworld.hummingbird.HummingbirdVM.NULL;

/**
 * Code for malloc/free adapted for HVM from https://www.math.uni-bielefeld.de/~rehmann/Ckurs-f/b04/alloc.h.
 * No license is specified, but this is credited as the implementation from K&R.
 */

///* titel: malloc()/free()-Paar nach K&R 2, p.185ff */
//
//typedef long Align;
//
//union header {			/* Kopf eines Allokationsblocks */
//struct {
//    union header	*ptr;  	/* Zeiger auf zirkulaeren Nachfolger */
//    unsigned 	size;	/* Groesse des Blocks	*/
//} s;
//Align x;			/* Erzwingt Block-Alignierung	*/
//};
//
//typedef union header Header;
//
//static Header base;		/* Anfangs-Header	*/
//static Header *freep = NULL;	/* Aktueller Einstiegspunkt in Free-Liste */
//
//void* malloc(unsigned nbytes) {
//    Header *p, *prevp;
//    static Header *morecore(unsigned);	/* Aufruf ans Betriebssystem */
//    unsigned nunits;
//
//     /* Kleinstes Vielfaches von sizeof(Header), das die
//	geforderte Byte-Zahl enthalten kann, plus 1 fuer den Header selbst: */
//
//    nunits = (nbytes+sizeof(Header)-1)/sizeof(Header) + 1;
//
//    if ((prevp = freep) == NULL) {	/* Erster Aufruf, Initialisierung */
//        base.s.ptr = freep = prevp = &base;
//        base.s.size = 0;		/* base wird Block der Laenge 0 */
//    }
//    for (p = prevp->s.ptr; ; prevp = p, p = p->s.ptr) {
//
//	/* p durchlaeuft die Free-Liste, gefolgt von prevp, keine
//		Abbruchbedingung!!	*/
//
//        if (p->s.size >= nunits) {	/* Ist p gross genug? 		*/
//            if (p->s.size == nunits) 	/* Falls exakt, wird er... 	*/
//                prevp->s.ptr = p->s.ptr;/* ... der Liste entnommen 	*/
//            else {			/* andernfalls...	   	*/
//                p->s.size -= nunits;	/* wird p verkleinert		*/
//                p += p->s.size;		/* und der letzte Teil ... 	*/
//                p->s.size = nunits;	/* ... des Blocks...		*/
//            }
//            freep = prevp;
//            return (void*) (p+1);	/* ... zurueckgegeben, allerdings
//					   unter der Adresse von p+1,
//					   da p auf den Header zeigt.  	*/
//        }
//        if ( p == freep)		/* Falls die Liste keinen Block
//				           ausreichender Groesse enthaelt,
//					   wird morecore() aufgrufen	*/
//            if ((p = morecore(nunits)) == NULL)
//                return NULL;
//    }
//}
//
//#define NALLOC 	1024	/* Mindestgroesse fuer morecore()-Aufruf	*/
//
///* Eine static-Funktion ist ausserhalb ihres Files nicht sichtbar	*/
//
//static Header *morecore(unsigned nu) {
//    char *cp, *sbrk(int);
//    void free(void*);
//    Header *up;
//    if (nu < NALLOC)
//        nu = NALLOC;
//    cp = sbrk(nu * sizeof(Header));
//    if (cp == (char *) -1)		/* sbrk liefert -1 im Fehlerfall */
//    return NULL;
//    up = (Header*) cp;
//    up->s.size = nu;			/* Groesse wird eingetragen	*/
//    free((void*)(up+1));		/* Einbau in Free-Liste		*/
//    return freep;
//}
//
//void free(void *ap) {			/* Rueckgabe an Free-Liste	*/
//    Header *bp, *p;
//
//    bp = (Header*) ap - 1;		/* Hier ist der Header des Blocks */
//
//	/* Die Liste wird durchmustert, der Block soll der
//	   Adressgroesse nach richtig eingefuegt werden,
//	   um Defragmentierung zu ermoeglichen.				*/
//
//    for (p = freep; !(bp > p && bp < p->s.ptr); p = p->s.ptr)
//        if (p >= p->s.ptr && (bp > p || bp < p->s.ptr))
//            break;	/* bp liegt vor Block mit kleinster oder hinter
//			   Block mit groesster Adresse */
//
//    if (bp + bp->s.size == p->s.ptr) {
//        /* Vereinigung mit oberem Nachbarn 	*/
//        bp->s.size += p->s.ptr->s.size;
//        bp->s.ptr = p->s.ptr->s.ptr;
//    }
//    else
//        bp->s.ptr = p->s.ptr;
//    if ( p + p->s.size == bp ) {
//        /* Vereinigung mit unterem Nachbarn 	*/
//        p->s.size += bp->s.size;
//        p->s.ptr = bp->s.ptr;
//    } else
//        p->s.ptr = bp;
//    freep = p;
//}
public class Allocator {

    public static final int NALLOC = 1024;
    private static final int DEFAULT_INITIAL_SIZE = -1;

    private final HummingbirdVM hvm;
    private final HeaderStruct Header;

    private final int initialSize;
    private final int head;
    private int tail;

    public Allocator(HummingbirdVM vm, int baseAddress){
        this(vm, baseAddress, DEFAULT_INITIAL_SIZE);
    }

    public Allocator(HummingbirdVM vm, int baseAddress, int initialSize){
        if(baseAddress == NULL){
            throw new IllegalArgumentException("Allocator base address must be non-zero");
        }

        hvm = vm;
        head = baseAddress;
        tail = head;
        Header = new HeaderStruct(vm);

        this.initialSize = initialSize;

        initFreeList();
    }

    private void initFreeList(){
        // Init linked free list.
        // Invariants:
        // (1) There is always at least one block - the head block, it never moves from the base pointer, and its
        //     size is always 0.
        // (2) The base pointer of each block is a higher address than
        //     the base pointer of the block before it.
        // (3) Adjacent blocks are merged when they are added to the free list.
        // (4) The tail block's nextBlock field is always NULL.

        // Init head
        tail = head + Header.sizeOf();
        Header.nextBlock(head, tail);
        Header.size(head, 0);
        Header.nextBlock(tail, NULL);
        if(initialSize == DEFAULT_INITIAL_SIZE){
            Header.size(tail, hvm.memorySize() - head - 2 * Header.sizeOf());
        }else{
            Header.size(tail, initialSize - 2 * Header.sizeOf());
        }

    }

    private int morecore(int nbytes){

        nbytes = Math.max(nbytes + Header.sizeOf(), NALLOC);

        int newBlock = sbrk(nbytes);
        if(newBlock == -1){
            return NULL;
        }

        Header.size(newBlock, nbytes);
        // Have to add the size of the header because the first thing free()
        // will do is subtract the size of the header to inspect it.
        // We call free() here so that the new block is properly added into
        // the free list
        return freeInternal(newBlock + Header.sizeOf());
    }

    public synchronized int sbrk(int moreBytes){
        int oldSize = hvm.memorySize();
        if(moreBytes == 0){
            return oldSize;
        }
        int newSize = hvm.resize(oldSize + moreBytes);
        if(newSize == oldSize){
            return -1;
        }
        return oldSize;
    }

    public synchronized int malloc(int nbytes){

        if(nbytes == 0){
            return NULL;
        }

        int previousBlock = head;

        for(var block = Header.nextBlock(head); block != NULL; previousBlock = block, block = Header.nextBlock(block)){

            if(Header.size(block) == nbytes && !(previousBlock == head && Header.nextBlock(block) == NULL)){
                // Exact match - don't resize, just join the preceding and trailing blocks.
                // Can't do this on the head block because there is no prior block to work with.

                Header.nextBlock(previousBlock, Header.nextBlock(block));
                Header.nextBlock(block, NULL);

                return block + Header.sizeOf();
            }else if(Header.size(block) >= nbytes + Header.sizeOf()){

                // There's room in this block to claim the memory we need
                // and also leave behind a newly created block that's a fragment
                // of this one.
                int newBlock = block + Header.sizeOf() + nbytes;

                // Insert new free block into the list
                Header.nextBlock(previousBlock, newBlock);
                Header.nextBlock(newBlock, Header.nextBlock(block));

                Header.size(newBlock, Header.size(block) - nbytes - Header.sizeOf());
                Header.size(block, nbytes);

                if(block == tail){
                    // We just snagged the first bit of the tail block, so update
                    // the tail to the new block
                    tail = newBlock;
                    Header.nextBlock(newBlock, NULL);
                }
                return block + Header.sizeOf();
            }

            // We've done a full pass without finding a suitable allocation,
            // so request more memory from the VM. morecore() will return a pointer
            // to the next block to search for, which if morecore() succeeded is guaranteed
            // to be large enough.
            if(block == tail){
                block = morecore(nbytes);
                if(block == NULL){
                    return NULL;
                }
            }
        }
        return NULL;
    }

    protected int freeInternal(int ptr){
        // Return the address of the pointer's block, or the address
        // of the combined block that it was merged with

        if(ptr == NULL){
            return NULL;
        }

        int hPtr = ptr - Header.sizeOf();

        int block = head;
        if(hPtr > tail){
            block = tail;
        }else{
            while(block != tail){
                if(block < hPtr && hPtr < Header.nextBlock(block)){
                    break;
                }
                var next = Header.nextBlock(block);
                if(next == block){
                    // An invariant was broken due to data corruption, exit
                    return -1;
                }
                block = next;
            }
        }

        // Insert hPtr into the list
        // Note that when block == tail, nextBlock is NULL.
        int nextBlock = Header.nextBlock(block);

        Header.nextBlock(block, hPtr);
        Header.nextBlock(hPtr, nextBlock);

        // Now merge (if able) block, hPtr, and the trailing block
        var mergedOrNext = mergeIfAble(block, hPtr);
        if(nextBlock != NULL){
            mergedOrNext = mergeIfAble(mergedOrNext, nextBlock);
        }

        if(block == tail){
            // Sets the tail to the merged block (if merge happened),
            // or leaves it pointing to the old tail (if merge didn't happen).
            tail = mergedOrNext;
            Header.nextBlock(tail, NULL);
        }
        return mergedOrNext;
    }

    public synchronized void free(int ptr){
        freeInternal(ptr);
    }

    public synchronized int freeSpace(){
        int size = 0;
        for(var block = head; block != NULL; block = Header.nextBlock(block)){
            size += Header.size(block);
        }
        return size;
    }

    public synchronized int countFreeBlocks(){
        int count = 0;
        for(var block = head; block != NULL; block = Header.nextBlock(block)){
            count++;
        }
        return count;
    }

    public synchronized int[] blockSizes(){
        var blocks = countFreeBlocks();
        var sizes = new int[blocks];
        for(int i = 0, block = head; block != NULL; block = Header.nextBlock(block), i++){
            sizes[i] = Header.size(block);
        }
        return sizes;
    }

    private int mergeIfAble(int preceding, int trailing){
        if(preceding == head){
            return trailing; // Can't merge head
        }
        if(preceding + Header.sizeOf() + Header.size(preceding) == trailing){
            // Include the size of the header in the block being merged
            Header.size(preceding, Header.size(preceding) + Header.size(trailing) + Header.sizeOf());
            Header.nextBlock(preceding, Header.nextBlock(trailing));
            return preceding;
        }
        return trailing;
    }

    public static class HeaderStruct {

        public static final Struct layout;
        public static final int nextBlock;
        public static final int size;

        static {
            var builder = Struct.builder();
            nextBlock = builder.withField(4);
            size = builder.withField(4);

            layout = builder.build();
        }

        private final StructAccessor acc;

        public HeaderStruct(HummingbirdVM vm){
            acc = new StructAccessor(vm, layout);
        }

        public int nextBlock(int hPtr){
            return acc.readInt(hPtr, nextBlock);
        }

        public void nextBlock(int hPtr, int ptr){
            if(hPtr == 8 && ptr == 0){
                new Exception().printStackTrace();
                System.exit(0);
            }
            acc.writeInt(hPtr, Allocator.HeaderStruct.nextBlock, ptr);
        }

        public int size(int hPtr){
            return acc.readInt(hPtr, Allocator.HeaderStruct.size);
        }

        public void size(int hPtr, int size){
            acc.writeInt(hPtr, Allocator.HeaderStruct.size, size);
        }

        public int sizeOf(){
            return acc.struct().sizeOf();
        }

    }
}

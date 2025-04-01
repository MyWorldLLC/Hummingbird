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

    private final HeaderStruct Header;

    private final HummingbirdVM hvm;
    private int freeBlock;
    private final int base;

    public Allocator(HummingbirdVM vm, int baseAddress){
        hvm = vm;
        base = baseAddress;
        Header = new HeaderStruct(vm);
    }

    private int morecore(int nbytes){

        nbytes = Math.max(nbytes, NALLOC);

        int formerEndPtr = sbrk(nbytes);
        if(formerEndPtr == -1){
            return NULL;
        }

        Header.size(formerEndPtr, nbytes);
        // Have to add the size of the header because the first thing free()
        // will do is subtract the size of the header to inspect it.
        // We call free() here so that the new block is properly added into
        // the free list
        free(formerEndPtr + Header.sizeOf());
        return freeBlock;
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

        if(freeBlock == NULL){
            // Init circular linked list as base -> freeBlock -> base
            freeBlock = base + Header.sizeOf();
            Header.nextBlock(base, freeBlock);
            Header.size(base, 0);

            Header.nextBlock(freeBlock, base);
            Header.size(freeBlock, hvm.memorySize() - base - 2 * Header.sizeOf());
        }

        int lastFreeBlock = freeBlock;

        for(var block = Header.nextBlock(lastFreeBlock); ; lastFreeBlock = block, block = Header.nextBlock(block)){

            if(Header.size(block) >= nbytes){
                // Block will fit this allocation
                if(Header.size(block) == nbytes){

                    // Don't resize, just join the preceding and trailing blocks
                    Header.nextBlock(lastFreeBlock, Header.nextBlock(block));

                }else if(Header.size(block) > nbytes + Header.sizeOf()){

                    // There's room to insert a new free block
                    int newBlock = block + Header.sizeOf() + nbytes;

                    // Insert new free block into the list
                    Header.nextBlock(lastFreeBlock, newBlock);
                    Header.nextBlock(newBlock, Header.nextBlock(block));
                    Header.size(newBlock, Header.size(block) - nbytes - Header.sizeOf());
                    Header.size(block, nbytes);
                }
                return block + Header.sizeOf();
            }

            // We've done a full loop without finding a splittable block or we've allocated all
            // available memory, so attempt to allocate more from the VM
            if(block == freeBlock || Header.nextBlock(block) == block){
                block = morecore(nbytes + Header.sizeOf());
                if(block == NULL){
                    return NULL;
                }
            }
        }
    }

    public synchronized void free(int ptr){

        int hPtr = ptr - Header.sizeOf();

        // Find insertion point in free list by pointer order so that
        // we can merge adjacent blocks
        var block = Header.nextBlock(freeBlock);

        // Select the block before hPtr where its next block follows hPtr, stopping if we only have a single free block
        while(!(block < hPtr && Header.nextBlock(block) > hPtr) && Header.nextBlock(block) != block){
            block = Header.nextBlock(block);
        }

        // Insert hPtr into the list
        int nextBlock = Header.nextBlock(block);
        int preceeding = Math.min(Math.min(block, hPtr), nextBlock);
        int middle = Math.min(Math.max(block, hPtr), nextBlock);
        int trailing = Math.max(Math.max(block, hPtr), nextBlock);

        Header.nextBlock(preceeding, middle);
        Header.nextBlock(middle, trailing);

        // Now merge (if able) block, hPtr, and the trailing block
        var mergedOrTail = mergeIfAble(preceeding, middle);
        mergedOrTail = mergeIfAble(mergedOrTail, trailing);

        freeBlock = mergedOrTail;
    }

    public synchronized int freeSpace(){
        int size = 0;
        for(var block = Header.nextBlock(base); ; block = Header.nextBlock(block)){
            size += Header.size(block);
            if(block == base){
                return size;
            }
        }
    }

    public synchronized int countFreeBlocks(){
        int count = 0;
        for(var block = Header.nextBlock(base); ; block = Header.nextBlock(block)){
            count++;
            if(block == base){
                return count;
            }
        }
    }

    private int mergeIfAble(int preceding, int trailing){
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

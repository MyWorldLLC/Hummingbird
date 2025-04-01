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
    private int freep = NULL;
    private final int base;

    public Allocator(HummingbirdVM vm, int baseAddress){
        hvm = vm;
        base = baseAddress;
        Header = new HeaderStruct(vm);
    }

    private int morecore(int nu){
        int cp;
        int up;

        if(nu < NALLOC){
            nu = NALLOC;
        }

        cp = sbrk(nu * Header.sizeOf());
        if(cp == -1){
            return NULL;
        }

        up = cp;
        Header.size(up, nu);
        free(up + Header.sizeOf());
        return freep;
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
        int p, prevp;

        int nunits = (nbytes + Header.sizeOf() - 1)/Header.sizeOf() + 1;

        if((prevp = freep) == NULL){
            Header.ptr(base, freep = prevp = base);
            Header.size(base, 0);
        }

        for(p = Header.ptr(prevp); ; prevp = p, p = Header.ptr(p)){

            if(Header.size(p) >= nunits){
                if(Header.size(p) == nunits){
                    Header.ptr(prevp, Header.ptr(p));
                }else{
                    Header.size(p, Header.size(p) - nunits);
                    p += Header.size(p);
                    Header.size(p, nunits);
                }
                freep = prevp;
                return p + Header.sizeOf();
            }
            if(p == freep){
                if((p = morecore(nunits)) == NULL){
                    return NULL;
                }
            }
        }
    }

    public synchronized void free(int ap){
        int bp, p;

        bp = ap - Header.sizeOf();

        for(p = freep; !(bp > p && bp < Header.ptr(p)); p = Header.ptr(p)){
            if(p >= Header.ptr(p) && (bp > p || bp < Header.ptr(p))){
                break;
            }
        }

        if(bp + Header.size(bp) == Header.ptr(p)){
            Header.size(bp, Header.size(bp) + Header.size(Header.ptr(p)));
            Header.ptr(bp, Header.ptr(Header.ptr(p)));
        }else{
            Header.ptr(bp, Header.ptr(p));
        }

        if(p + Header.size(p) == bp){
            Header.size(p, Header.size(p) + Header.size(bp));
            Header.ptr(p, Header.ptr(bp));
        }else{
            Header.ptr(p, bp);
        }

        freep = p;
    }

    public static class HeaderStruct {

        public static final Struct layout;
        public static final int ptr;
        public static final int size;

        static {
            var builder = Struct.builder();
            ptr = builder.withField(4);
            size = builder.withField(4);

            layout = builder.build();
        }

        private final StructAccessor acc;

        public HeaderStruct(HummingbirdVM vm){
            acc = new StructAccessor(vm, layout);
        }

        public int ptr(int hPtr){
            return acc.readInt(hPtr, ptr);
        }

        public void ptr(int hPtr, int ptr){
            acc.writeInt(hPtr, Allocator.HeaderStruct.ptr, ptr);
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

package myworld.hummingbird.util;

import myworld.hummingbird.HummingbirdVM;

/**
 * Code for free/malloc adapted for HVM from https://www.math.uni-bielefeld.de/~rehmann/Ckurs-f/b04/alloc.h.
 * No license is specified, but this is credited as the implementation from K&R so it may be in the public domain.
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

    private static final int NULL = 0;
    public static final int NALLOC = 1024;

    private static final int HEADER_SIZE = 4 * 2; // 2 ints, 4 bytes long each

    private final HummingbirdVM hvm;
    private int freep = NULL;
    private final int base;

    public Allocator(HummingbirdVM vm, int baseAddress){
        hvm = vm;
        base = baseAddress;
    }

    // Note: header is implicitly modeled as two ints,
    // the first being the pointer to the next header
    // and the second being the size.
    private int getPtr(int hPtr){
        return hvm.memory.getInt(hPtr);
    }

    private void setPtr(int hPtr, int ptr){
        hvm.memory.putInt(hPtr, ptr);
    }

    private int getSize(int hPtr){
        return hvm.memory.getInt(hPtr + 4);
    }

    private void setSize(int hPtr, int size){
        hvm.memory.putInt(hPtr + 4, size);
    }

    private int morecore(int nu){
        int cp;
        int up;

        if(nu < NALLOC){
            nu = NALLOC;
        }

        cp = sbrk(nu * HEADER_SIZE);
        if(cp == -1){
            return NULL;
        }

        up = cp;
        setSize(up, nu);
        free(up + HEADER_SIZE);
        return freep;
    }

    public synchronized int sbrk(int moreBytes){
        int oldSize = hvm.memory.limit();
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

        int nunits = (nbytes + HEADER_SIZE - 1)/HEADER_SIZE + 1;

        if((prevp = freep) == NULL){
            setPtr(prevp, base);
            setPtr(freep, prevp);
            setPtr(base, freep);
            setSize(base, 0);
        }

        for(p = getPtr(prevp); ; prevp = p, p = getPtr(p)){

            if(getSize(p) >= nunits){
                if(getSize(p) == nunits){
                    setPtr(prevp, getPtr(p));
                }else{
                    setSize(p, getSize(p) - nunits);
                    p += getSize(p);
                    setSize(p, nunits);
                }
                freep = prevp;
                return p + HEADER_SIZE;
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

        bp = ap - HEADER_SIZE;

        for(p = freep; !(bp > p && bp < getPtr(p)); p = getPtr(p)){
            if(p >= getPtr(p) && (bp > p || bp < getPtr(p))){
                break;
            }
        }

        if(bp + getSize(bp) == getPtr(p)){
            setSize(bp, getSize(bp) + getSize(getPtr(p)));
            setPtr(bp, getPtr(getPtr(p)));
        }else{
            setPtr(bp, getPtr(p));
        }

        if(p + getSize(p) == bp){
            setSize(p, getSize(p) + getSize(bp));
            setPtr(p, getPtr(bp));
        }else{
            setPtr(p, bp);
        }

        freep = p;
    }
}

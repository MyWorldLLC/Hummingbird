package myworld.hummingbird;

public class SavedRegisters {

    protected int sizeInc;

    protected int[] ips;
    protected int ipIndex;

    protected int[] ireg;
    protected int iIndex;

    protected float[] freg;
    protected int fIndex;

    protected long[] lreg;
    protected int lIndex;

    protected double[] dreg;
    protected int dIndex;

    protected Object[] oreg;
    protected int oIndex;

    public SavedRegisters(int initialSize){
        sizeInc = initialSize;

        ips = new int[initialSize];
        ireg = new int[initialSize];
        freg = new float[initialSize];
        lreg = new long[initialSize];
        dreg = new double[initialSize];
        oreg = new Object[initialSize];
    }

    public void clear(){
        ipIndex = 0;
        iIndex = 0;
        fIndex = 0;
        lIndex = 0;
        dIndex = 0;
        oIndex = 0;
    }

    public void saveIp(int ip){
        if(ips.length <= ipIndex + 1){
            var tmp = ips;
            ips = new int[ips.length + sizeInc];
            System.arraycopy(tmp, 0, ips, 0, tmp.length);
        }
        ips[ipIndex] = ip;
        ipIndex++;
    }

    public int restoreIp(){
        ipIndex--;
        return ips[ipIndex];
    }

    public void save(int[] reg, int index, int count){
        if(ireg.length <= iIndex + index + count){
            var tmp = ireg;
            ireg = new int[ireg.length + sizeInc];
            System.arraycopy(tmp, 0, ireg, 0, tmp.length);
        }
        System.arraycopy(reg, index, ireg, iIndex, count);
        iIndex += count;
    }

    public void restore(int[] reg, int index, int count){
        iIndex -= count;
        System.arraycopy(ireg, iIndex, reg, index, count);
    }

    public void save(float[] reg, int index, int count){
        if(freg.length <= fIndex + index + count){
            var tmp = freg;
            freg = new float[freg.length + sizeInc];
            System.arraycopy(tmp, 0, freg, 0, tmp.length);
        }
        System.arraycopy(reg, index, freg, fIndex, count);
        fIndex += count;
    }

    public void restore(float[] reg, int index, int count){
        fIndex -= count;
        System.arraycopy(freg, fIndex, reg, index, count);
    }

    public void save(long[] reg, int index, int count){
        if(lreg.length <= iIndex + index + count){
            var tmp = lreg;
            lreg = new long[lreg.length + sizeInc];
            System.arraycopy(tmp, 0, lreg, 0, tmp.length);
        }
        System.arraycopy(reg, index, lreg, lIndex, count);
        lIndex += count;
    }

    public void restore(long[] reg, int index, int count){
        lIndex -= count;
        System.arraycopy(lreg, lIndex, reg, index, count);
    }

    public void save(double[] reg, int index, int count){
        if(dreg.length <= dIndex + index + count){
            var tmp = dreg;
            dreg = new double[dreg.length + sizeInc];
            System.arraycopy(tmp, 0, dreg, 0, tmp.length);
        }
        System.arraycopy(reg, index, dreg, dIndex, count);
        dIndex += count;
    }

    public void restore(double[] reg, int index, int count){
        dIndex -= count;
        System.arraycopy(dreg, dIndex, reg, index, count);
    }

    public void save(Object[] reg, int index, int count){
        if(oreg.length <= oIndex + index + count){
            var tmp = oreg;
            oreg = new Object[oreg.length + sizeInc];
            System.arraycopy(tmp, 0, oreg, 0, tmp.length);
        }
        System.arraycopy(reg, index, oreg, oIndex, count);
        oIndex += count;
    }

    public void restore(Object[] reg, int index, int count){
        oIndex -= count;
        System.arraycopy(oreg, oIndex, reg, index, count);
    }

}

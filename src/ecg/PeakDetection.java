// File: ecg/PeakDetection.java
package ecg;

import dsl.Q;
import dsl.Query;
import dsl.Sink;
import dsl.S;
import utils.functions.Func2;

public class PeakDetection {

    /**
     * Curve-length transform:
     *  1) adjust: x[n] = raw[n] - 1024
     *  2) smooth over 5 samples
     *  3) derivative over 2-sample offset
     *  4) sum sqrt(1 + d^2) over a 41-sample window
     */
    public static Query<Integer,Double> qLength() {
        return new Query<>() {
            private final double[] xbuf = new double[5];
            private int xpos = 0, xcnt = 0;
            private final double[] ybuf = new double[3];
            private int ypos = 0, ycnt = 0;
            private final double[] dbuf = new double[41];
            private int dpos = 0, dcnt = 0;

            @Override
            public void start(Sink<Double> sink) {
                xpos = xcnt = ypos = ycnt = dpos = dcnt = 0;
            }

            @Override
            public void next(Integer raw, Sink<Double> sink) {
                // 1) adjust
                double x = raw - 1024.0;
                xbuf[xpos] = x;
                xpos = (xpos + 1) % 5;
                if (xcnt < 5) { xcnt++; return; }

                // 2) smooth
                double sx = 0;
                for (double v : xbuf) sx += v;
                double y = sx / 5.0;
                ybuf[ypos] = y;
                ypos = (ypos + 1) % 3;
                if (ycnt < 3) { ycnt++; return; }

                // 3) derivative
                int newest = (ypos + 2) % 3;
                int oldest2 = ypos;
                double d = (ybuf[newest] - ybuf[oldest2]) / 2.0;
                dbuf[dpos] = d;
                dpos = (dpos + 1) % 41;
                if (dcnt < 41) { dcnt++; return; }

                // 4) curve length
                double L = 0;
                for (double dv : dbuf) {
                    L += Math.sqrt(1.0 + dv * dv);
                }
                sink.next(L);
            }

            @Override
            public void end(Sink<Double> sink) { }
        };
    }

    /**
     * qPeaks = tag each raw sample with its sample‐index,
     *           turn into VT, attach length → VTL,
     *           then run Detect()
     */
    public static Query<Integer,Long> qPeaks() {
        // 1) scan indices 0,1,2,... over raw stream
        Query<Integer,Long> idxQ = Q.<Integer,Long>scan(
            -1L,
            (Func2<Long,Integer,Long>)(i, raw) -> i + 1
        );
        Query<Integer,Integer> rawQ = Q.<Integer>id();

        Query<Integer,VT> toVT = Q.parallel(
            idxQ, rawQ,
            (Func2<Long,Integer,VT>)(ts, v) -> new VT(v, ts)
        );

        // 2) attach curve‐length → VTL
        Query<Integer,VTL> toVTL = Q.parallel(
            toVT, qLength(),
            (Func2<VT,Double,VTL>)(vt, L) -> vt.extendl(L)
        );

        // 3) DEBUG: print every VTL before detection
        Query<Integer,VTL> debugVTL = Q.pipeline(
            toVTL,
            Q.map(vtl -> {
                // adjust formatting as you like
                // System.out.printf(
                //   "DEBUG VTL → ts=%4d,   L=%.2f%n",
                //   vtl.ts, vtl.l
                // );
                return vtl;
            })
        );

        // 4) FEED into your Detect (replace with your Detect implementation)
        return Q.pipeline(debugVTL, new Detect());
    }


    public static void main(String[] args) {
        System.out.println("****************************************");
        System.out.println("***** Algorithm for Peak Detection *****");
        System.out.println("****************************************\n");
        Q.execute(Data.ecgStream("100.csv"), qPeaks(), S.printer());
    }
}

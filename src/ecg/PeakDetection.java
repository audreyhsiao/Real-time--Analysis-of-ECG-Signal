package ecg;

import dsl.S;
import dsl.Q;
import dsl.Query;

public class PeakDetection {

	// The curve length transformation:
	//
	// adjust: x[n] = raw[n] - 1024
	// smooth: y[n] = (x[n-2] + x[n-1] + x[n] + x[n+1] + x[n+2]) / 5
	// deriv: d[n] = (y[n+1] - y[n-1]) / 2
	// length: l[n] = t(d[n-w]) + ... + t(d[n+w]), where
	//         w = 20 (samples) and t(d) = sqrt(1.0 + d * d)

	public static Query<Integer,Double> qLength() {
		// adjust >> smooth >> deriv >> length
		return new Query<>() {
            // buffers & counters
            private final double[] xbuf = new double[5];
            private int xpos, xcnt;
            private final double[] ybuf = new double[3];
            private int ypos, ycnt;
            private final double[] dbuf = new double[41];
            private int dpos, dcnt;

            @Override
            public void start(dsl.Sink<Double> sink) {
                xpos = xcnt = ypos = ycnt = dpos = dcnt = 0;
            }
			 @Override
            public void next(Integer raw, dsl.Sink<Double> sink) {
                // 1) adjust
                double x = raw - 1024.0;
                // update xbuf
                xbuf[xpos] = x;
                xpos = (xpos + 1) % 5;
                if (xcnt < 5) { xcnt++; return; }

                // 2) smooth: average of xbuf
                double sumx = 0;
                for (double v : xbuf) sumx += v;
                double y = sumx / 5.0;
                // update ybuf
                ybuf[ypos] = y;
                ypos = (ypos + 1) % 3;
                if (ycnt < 3) { ycnt++; return; }

                // 3) deriv: (y[n] - y[n-2]) / 2
                int newest  = (ypos + 2) % 3; // just-emitted idx
                int oldest2 = ypos;          // two ago
                double d = (ybuf[newest] - ybuf[oldest2]) / 2.0;
                // update dbuf
                dbuf[dpos] = d;
                dpos = (dpos + 1) % 41;
                if (dcnt < 41) { dcnt++; return; }

                // 4) curve length over 41 last d’s
                double L = 0;
                for (double dv : dbuf) {
                    L += Math.sqrt(1.0 + dv*dv);
                }
                sink.next(L);
            }

            @Override
            public void end(dsl.Sink<Double> sink) { }
        };
    }
		
	}

	// In order to detect peaks we need both the raw (or adjusted)
	// signal and the signal given by the curve length transformation.
	// Use the datatype VTL and implement the class Detect.

	public static Query<Integer,Long> qPeaks() {
		// 1) sample‐indexing: scan(-1, (i,raw)->i+1)  ==> Query<Integer,Long>
        var idxQ = Q.scan(-1L, (i, raw) -> i + 1);
        // 2) raw value passthrough: id()
        var rawQ = Q.id();
        // 3) make VT
        Query<Integer,VT> toVT = Q.<Integer, Long, Integer, VT>parallel(
            idxQ, rawQ,
            (ts, v) -> new VT(v, ts)
        );
        // 4) attach length → VTL
        Query<Integer,VTL> toVTL = Q.parallel(
            toVT, qLength(),
            (vt, L) -> vt.extendl(L)
        );
        // 5) detect peaks
        return Q.pipeline(toVTL, new Detect());
    
	}

	public static void main(String[] args) {
		System.out.println("****************************************");
		System.out.println("***** Algorithm for Peak Detection *****");
		System.out.println("****************************************");
		System.out.println();

		Q.execute(Data.ecgStream("100.csv"), qPeaks(), S.printer());
	}

}

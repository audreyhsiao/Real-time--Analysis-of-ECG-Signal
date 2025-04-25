package ecg;

import java.util.ArrayDeque;
import java.util.Deque;

import dsl.Query;
import dsl.Sink;

// The detection algorithm (decision rule) that we described in class
// (or your own slight variant of it).
//
// (1) Determine the threshold using the class TrainModel.
//
// (2) When l[n] exceeds the threhold, search for peak (max x[n] or raw[n])
//     in the next 40 samples.
//
// (3) No peak should be detected for 72 samples after the last peak.
//
// OUTPUT: The timestamp of each peak.

public class Detect implements Query<VTL,Long> {

	// Choose this to be two times the average length
	// over the entire signal.
	// private static final double THRESHOLD = 0.0;
	private static final double THRESHOLD = 80;//127.6533171951914; 
	
	
	private long lastPeakTs;
    private final VTL[] window;
    private int windowCount;

	public Detect() {
		window = new VTL[41];
        windowCount = 0;
		
	}

	@Override
	public void start(Sink<Long> sink) {
        // Ensure the first real candidate always passes the refractory check
		lastPeakTs    = Long.MIN_VALUE / 2;
        windowCount   = 0;
	}

	@Override
	public void next(VTL item, Sink<Long> sink) {
		 // add the new sample into window
		 window[windowCount++] = item;

		 //  wait until we have the "current"+"next 40" = 41 samples
		 if (windowCount < window.length) {
            return;
        }
 
		 
		 VTL candidate = window[20];
		 if (windowCount == 41) {
			System.out.println("DEBUG candidate.ts=" 
				+ window[0].ts + "  l=" + window[0].l);
		  }
		
		  // No peak should be detected for 72 samples after the last peak.
        if (candidate.l >= THRESHOLD 
            && candidate.ts - lastPeakTs >= 72)
        {
            // find the peak in the middle of the window
            VTL peak = candidate;
            for (int i = 21; i < window.length; i++) {
                if (window[i].v > peak.v) {
                    peak = window[i];
                }
            }
            // 6) the real peak
            sink.next(peak.ts);
            lastPeakTs = peak.ts;
        }

		// move the window forward
		// Remove the first element (the oldest sample)
        System.arraycopy(window, 1, window, 0, window.length-1);
        windowCount = window.length-1;
	}

 @Override
 public void end(Sink<Long> sink) {
	 sink.end();
 }
}

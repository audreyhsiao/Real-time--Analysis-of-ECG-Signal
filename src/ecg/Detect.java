package ecg;

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
	// private static final double THRESHOLD = 0.0; // TODO
	private static final double THRESHOLD = 0.0; /* paste 2 * value printed by TrainModel */;
	// TODO
	private long lastPeakTs;
	private int samplesSinceLastPeak;
	private VTL[] lookaheadBuffer; // to hold the next 40 samples
	private int bufferPos;

	public Detect() {
		lookaheadBuffer = new VTL[40];
		bufferPos = 0;
		// TODO
	}

	@Override
	public void start(Sink<Long> sink) {
		lastPeakTs = -Long.MAX_VALUE;
        samplesSinceLastPeak = Integer.MAX_VALUE;
        bufferPos = 0;
		// TODO
	}

	@Override
	public void next(VTL item, Sink<Long> sink) {
		// fill lookahead window
        lookaheadBuffer[bufferPos++] = item;
        if (bufferPos < lookaheadBuffer.length) {
            return;
        }
        // when full, check center sample:
        VTL center = lookaheadBuffer[20];
        if (center.l >= THRESHOLD && samplesSinceLastPeak >= 72) {
            // find max v in the 40-sample window
            VTL peak = center;
            for (VTL w : lookaheadBuffer) {
                if (w.v > peak.v) peak = w;
            }
            sink.next(peak.ts);
            lastPeakTs = peak.ts;
            samplesSinceLastPeak = 0;
        }
        // slide window by 1
        System.arraycopy(lookaheadBuffer, 1, lookaheadBuffer, 0, 39);
        bufferPos = 39;
        samplesSinceLastPeak++;
    }
	
	

	@Override
	public void end(Sink<Long> sink) {
		// TODO
	}
	
}

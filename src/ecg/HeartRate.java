package ecg;

import dsl.S;
import dsl.Q;
import dsl.Query;

// This file is devoted to the analysis of the heart rate of the patient.
// It is assumed that PeakDetection.qPeaks() has already been implemented.

public class HeartRate {

	// RR interval length (in milliseconds)
	public static Query<Integer,Double> qIntervals() {
        return Q.pipeline(
            PeakDetection.qPeaks(),
            // consecutive pairs → interval
            Q.sWindow2((t0, t1) -> (t1 - t0) * 1000.0 / 360.0),
            Q.map(interval -> {
                System.out.println("DEBUG RR interval (ms): " + interval);
                return interval;
            })
        );
        
    }

	// Average heart rate (over entire signal) in bpm.
	public static Query<Integer,Double> qHeartRateAvg() {
        return Q.pipeline(
            qIntervals(),
            Q.foldAvg(),
            Q.map(avgRR -> 60000.0 / avgRR)
        );
    }

	// Standard deviation of NN interval length (over the entire signal)
	// in milliseconds.
	public static Query<Integer,Double> qSDNN() {
        return Q.pipeline(qIntervals(), Q.foldStdev());
    }

    /** RMSSD: sqrt( average of squared successive diffs ) */
    public static Query<Integer,Double> qRMSSD() {
        return Q.pipeline(
            qIntervals(),
            Q.sWindow2((r0, r1) -> r1 - r0),
            Q.map(d -> d*d),
            Q.foldAvg(),
            Q.map(avgSq -> Math.sqrt(avgSq))
        );
    }

    /** pNN50: % of successive diffs > 50 ms */
    public static Query<Integer,Double> qPNN50() {
        return Q.pipeline(
            qIntervals(),
            Q.sWindow2((r0, r1) -> Math.abs(r1 - r0)),
            Q.map(d -> d > 50.0 ? 1.0 : 0.0),
            Q.foldAvg(),
            Q.map(frac -> 100.0 * frac)
        );
    
    }

	public static void main(String[] args) {
		System.out.println("****************************************");
		System.out.println("***** Algorithm for the Heart Rate *****");
		System.out.println("****************************************");
		System.out.println();

		System.out.println("***** Intervals *****");
		Q.execute(Data.ecgStream("100.csv"), qIntervals(), S.printer());
		System.out.println();

		System.out.println("***** Average heart rate *****");
		Q.execute(Data.ecgStream("100-all.csv"), qHeartRateAvg(), S.printer());
		System.out.println();

		System.out.println("***** HRV Measure: SDNN *****");
		Q.execute(Data.ecgStream("100-all.csv"), qSDNN(), S.printer());
		System.out.println();

		System.out.println("***** HRV Measure: RMSSD *****");
		Q.execute(Data.ecgStream("100-all.csv"), qRMSSD(), S.printer());
		System.out.println();

		System.out.println("***** HRV Measure: pNN50 *****");
		Q.execute(Data.ecgStream("100-all.csv"), qPNN50(), S.printer());
		System.out.println();
	}

}

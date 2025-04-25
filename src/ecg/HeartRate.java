package ecg;

import dsl.S;
import dsl.Q;
import dsl.Query;

// This file is devoted to the analysis of the heart rate of the patient.
// It is assumed that PeakDetection.qPeaks() has already been implemented.

public class HeartRate {

	// RR interval length (in milliseconds)
	public static Query<Integer,Double> qIntervals() {
        return Q.pipe(
            PeakDetection.qPeaks(),
            Q.slidingWindow(2, pair -> {
                long dt = pair.get(1) - pair.get(0);     // samples
                return dt * 1000.0 / 360.0;              // → ms at 360 Hz
            })
        );
    }

	// Average heart rate (over entire signal) in bpm.
	public static Query<Integer,Double> qHeartRateAvg() {
        return Q.pipe(
            qIntervals(),
            Q.fold(0.0, (sum, rr) -> sum + rr),
            Q.map((sum, count) -> 60000.0 / (sum / count))
        );
    }

	// Standard deviation of NN interval length (over the entire signal)
	// in milliseconds.
	public static Query<Integer,Double> qSDNN() {
        return Q.pipe(
            qIntervals(),
            Q.foldStats(),    // emits { count, mean, m2 }
            Q.map(stats -> Math.sqrt(stats.m2 / (stats.count - 1)))
        );
    }

	// RMSSD measure (over the entire signal) in milliseconds.
	public static Query<Integer,Double> qRMSSD() {
        return Q.pipe(
            qIntervals(),
            Q.slidingWindow(2, w -> w.get(1) - w.get(0)),
            Q.map(v -> v * v),
            Q.fold(0.0, (sum, x2) -> sum + x2),
            Q.map((sum, count) -> Math.sqrt(sum / (count - 1)))
	};

	// Proportion (in %) derived by dividing NN50 by the total number
	// of NN intervals (calculated over the entire signal).
	public static Query<Integer,Double> qPNN50() {
        return Q.pipe(
            qIntervals(),
            Q.slidingWindow(2, w -> Math.abs(w.get(1) - w.get(0))),
            Q.map(delta -> delta > 50 ? 1.0 : 0.0),
            Q.fold(0.0, (sum, v) -> sum + v),
            Q.map((sum, count) -> 100.0 * sum / (count - 1))
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

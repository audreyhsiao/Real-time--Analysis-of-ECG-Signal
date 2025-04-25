package ecg;

import dsl.S;
import dsl.Q;
import dsl.Query;

public class TrainModel {

	// The average value of the signal l[n] over the entire input.
	public static Query<Integer,Double> qLengthAvg() {
		//TODO
		return Q.pipe(
            PeakDetection.qLength(),
            Q.fold(0.0, (sum, l) -> sum + l),
			Q.map(pair -> pair.getFirst() / pair.getSecond())  // divide by total samples seen
        );
		// Hint: Use PeakDetection.qLength()
		return null;
	}

	public static void main(String[] args) {
		System.out.println("***********************************************");
		System.out.println("***** Algorithm for finding the threshold *****");
		System.out.println("***********************************************");
		System.out.println();

		Q.execute(Data.ecgStream("100-all.csv"), qLengthAvg(), S.printer());
	}

}

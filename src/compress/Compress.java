package compress;

import java.util.Iterator;

import dsl.*;
import ecg.Data;

public class Compress {

	public static final int BLOCK_SIZE = 10;

	// delta encoding
	public static Query<Integer,Integer> delta() {
		// TODO
		return null;
	}

	// the inverse of delta encoding
	public static Query<Integer,Integer> deltaInv() {
		// TODO
		return null;
	}

	// zigzag encoding
	public static Query<Integer,Integer> zigzag() {
		// TODO
		return null;
	}

	// the inverse of zigzag encoding
	public static Query<Integer,Integer> zigzagInv() {
		// TODO
		return null;
	}

	// pack a block of (encoded) elements into a compressed message
	public static Query<Integer,Integer> pack() {
		// TODO
		return null;
	}

	// unpack each compressed message
	public static Query<Integer,Integer> unpack() {
		// TODO
		return null;
	}

	// compress the input stream
	public static Query<Integer,Integer> compress() {
		// TODO
		return null;
	}

	// decompress the input stream (it is assumed to be compressed)
	public static Query<Integer,Integer> decompress() {
		// TODO
		return null;
	}

	public static void main(String[] args) {
		System.out.println("**********************************************");
		System.out.println("***** ToyDSL & Compression/Decompression *****");
		System.out.println("**********************************************");
		System.out.println();

		System.out.println("***** Compress *****");
		{
			// from range [0,2048) to [0,256)
			Query<Integer,Integer> q1 = Q.map(x -> x / 8);
			Query<Integer,Integer> q2 = compress();
			Query<Integer,Integer> q = Q.pipeline(q1, q2);
			Iterator<Integer> it = Data.ecgStream("100-all.csv");
			Q.execute(it, q, S.lastCount());
		}
		System.out.println();

		System.out.println("***** Compress & Decompress *****");
		{
			// from range [0,2048) to [0,256)
			Query<Integer,Integer> q1 = Q.map(x -> x / 8);
			Query<Integer,Integer> q2 = compress();
			Query<Integer,Integer> q3 = decompress();
			Query<Integer,Integer> q = Q.pipeline(q1, q2, q3);
			Iterator<Integer> it = Data.ecgStream("100-all.csv");
			Q.execute(it, q, S.lastCount());
		}
		System.out.println();
	}

}

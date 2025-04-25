package ra;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import dsl.Query;
import dsl.Sink;
import utils.Or;
import utils.Pair;

// A streaming implementation of the equi-join operator.
//
// We view the input as consisting of two channels:
// one with items of type A and one with items of type B.
// The output should contain all pairs (a, b) of input items,
// where a \in A is from the left channel, b \in B is from the
// right channel, and the equality predicate f(a) = g(b) holds.

public class EquiJoin<A,B,T> implements Query<Or<A,B>,Pair<A,B>> {

	// TODO
	private final Function<A,T> f;
    private final Function<B,T> g;
    private final List<A> leftBuffer = new ArrayList<>();
    private final List<B> rightBuffer = new ArrayList<>();

	private EquiJoin(Function<A,T> f, Function<B,T> g) {
		this.f = f;
        this.g = g;
		// TODO
	}

	public static <A,B,T> EquiJoin<A,B,T> from(Function<A,T> f, Function<B,T> g) {
		return new EquiJoin<>(f, g);
	}

	@Override
	public void start(Sink<Pair<A,B>> sink) {
		// TODO

	}

	@Override
	public void next(Or<A,B> item, Sink<Pair<A,B>> sink) {
		// TODO
		if (item.isLeft()) {
            A a = item.getLeft();
            T key = f.apply(a);
            for (B b : rightBuffer) {
                if (key.equals(g.apply(b))) {
					sink.next(new Pair<A, B>(a, b));
                }
            }
            leftBuffer.add(a);
        } else {
            B b = item.getRight();
            T key = g.apply(b);
            for (A a : leftBuffer) {
                if (key.equals(f.apply(a))) {
                    sink.next(new Pair<>(a, b));
                }
            }
            rightBuffer.add(b);
        }
	}

	@Override
	public void end(Sink<Pair<A,B>> sink) {
		// TODO
	}
	
}

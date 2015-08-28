package sorra.tracesonar.util;

public class Pair<L, R> {
  public final L _1;
  public final R _2;

  public Pair(L _1, R _2) {
    this._1 = _1;
    this._2 = _2;
  }

  public static <L, R> Pair<L, R> of(L _1, R _2) {
    return new Pair<>(_1, _2);
  }
}

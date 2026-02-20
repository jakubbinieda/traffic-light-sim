package lol.omg.jakubbinieda.sim.model;

import java.util.Objects;

public record Movement(Direction from, Direction to) {
  public Movement {
    Objects.requireNonNull(from, "from cannot be null");
    Objects.requireNonNull(to, "to cannot be null");
  }

  public TurnType getTurnType() {
    if (from == to) {
      return TurnType.U_TURN;
    }

    if (to == from.opposite()) {
      return TurnType.STRAIGHT;
    } else if (to == from.clockwise()) {
      return TurnType.RIGHT;
    } else {
      return TurnType.LEFT;
    }
  }
}

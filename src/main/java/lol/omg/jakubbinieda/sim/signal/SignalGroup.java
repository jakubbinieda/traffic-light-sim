package lol.omg.jakubbinieda.sim.signal;

import java.util.Objects;
import java.util.Set;
import lol.omg.jakubbinieda.sim.model.Movement;

public record SignalGroup(String id, Set<Movement> movements) {
  public SignalGroup {
    Objects.requireNonNull(id, "id cannot be null");
    Objects.requireNonNull(movements, "movements cannot be null");

    if (movements.isEmpty()) {
      throw new IllegalArgumentException("Signal group must control at least one movement");
    }
  }
}

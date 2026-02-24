package lol.omg.jakubbinieda.sim.geometry;

import java.util.Objects;
import java.util.Set;
import lol.omg.jakubbinieda.sim.model.Direction;
import lol.omg.jakubbinieda.sim.model.Movement;

public record Lane(String id, Direction approach, Set<Movement> allowedMovements) {
  public Lane {
    Objects.requireNonNull(id, "id cannot be null");
    Objects.requireNonNull(approach, "approach cannot be null");
    Objects.requireNonNull(allowedMovements, "allowedMovements cannot be null");

    if (allowedMovements.isEmpty()) {
      throw new IllegalArgumentException("Lane must allow at least one maneuver");
    }

    for (Movement movement : allowedMovements) {
      if (movement.from() != approach) {
        throw new IllegalArgumentException(
            "Maneuver " + movement + " is not compatible with lane approach " + approach);
      }
    }
  }

  public boolean allowsMovement(Movement movement) {
    return allowedMovements.contains(movement);
  }
}

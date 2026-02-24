package lol.omg.jakubbinieda.sim.geometry;

import java.util.List;
import java.util.Objects;
import lol.omg.jakubbinieda.sim.model.Direction;
import lol.omg.jakubbinieda.sim.model.Movement;

public record Road(Direction approach, List<Lane> lanes) {
  public Road {
    Objects.requireNonNull(approach, "approach cannot be null");
    Objects.requireNonNull(lanes, "lanes cannot be null");

    if (lanes.isEmpty()) {
      throw new IllegalArgumentException("Road must have at least one lane");
    }

    for (Lane lane : lanes) {
      if (lane.approach() != approach) {
        throw new IllegalArgumentException(
            "Lane "
                + lane.id()
                + " has approach "
                + lane.approach()
                + " which does not match road approach "
                + approach);
      }
    }

    long uniqueLaneCount = lanes.stream().map(Lane::id).distinct().count();
    if (uniqueLaneCount != lanes.size()) {
      throw new IllegalArgumentException("Lane IDs must be unique within a road");
    }
  }

  //  public List<Lane> getLanes() {
  //    return lanes;
  //  }

  public List<Movement> getAllMovements() {
    return lanes.stream().flatMap(lane -> lane.allowedMovements().stream()).toList();
  }

  public List<Lane> getLanesAllowingMovement(Direction to) {
    return lanes.stream().filter(lane -> lane.allowsMovement(new Movement(approach, to))).toList();
  }
}

package lol.omg.jakubbinieda.sim.geometry;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lol.omg.jakubbinieda.sim.model.Direction;
import lol.omg.jakubbinieda.sim.model.Movement;
import lol.omg.jakubbinieda.sim.signal.SignalGroup;

public class IntersectionLayout {
  private final Map<Direction, Road> roads;
  private final Set<Movement> movements;
  private final List<SignalGroup> signalGroups;

  public IntersectionLayout(Map<Direction, Road> roads, List<SignalGroup> signalGroups) {
    this.roads = Objects.requireNonNull(roads, "roads cannot be null");
    this.signalGroups = Objects.requireNonNull(signalGroups, "signalGroups cannot be null");

    if (signalGroups.isEmpty()) {
      throw new IllegalArgumentException("signalGroups cannot be empty");
    }

    this.movements =
        roads.values().stream()
            .flatMap(road -> road.getAllMovements().stream())
            .collect(Collectors.toSet());

    Set<Movement> covered =
        signalGroups.stream()
            .flatMap(group -> group.movements().stream())
            .collect(Collectors.toSet());

    for (Movement movement : movements) {
      if (!covered.contains(movement)) {
        throw new IllegalArgumentException(
            "Movement " + movement + " is not covered by any signal group");
      }
    }
  }

  public List<Lane> getLanesFor(Movement movement) {
    Road road = roads.get(movement.from());

    return road == null ? List.of() : road.getLanesAllowingMovement(movement.to());
  }

  public Set<Movement> getAllMovements() {
    return movements;
  }

  public List<SignalGroup> getSignalGroups() {
    return signalGroups;
  }

  public List<Direction> getApproachDirections() {
    return roads.keySet().stream().toList();
  }

  public Road getRoad(Direction direction) {
    return roads.get(direction);
  }
}

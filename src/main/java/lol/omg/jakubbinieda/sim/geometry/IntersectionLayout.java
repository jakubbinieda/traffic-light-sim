package lol.omg.jakubbinieda.sim.geometry;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lol.omg.jakubbinieda.sim.model.Direction;
import lol.omg.jakubbinieda.sim.model.Movement;

public class IntersectionLayout {
  private final Map<Direction, Road> roads;
  private final Set<Movement> movements;

  //  private final Map<String, Lane> laneIdMap;

  public IntersectionLayout(Map<Direction, Road> roads) {
    this.roads = Objects.requireNonNull(roads, "roads cannot be null");

    this.movements =
        roads.values().stream()
            .flatMap(road -> road.getAllMovements().stream())
            .collect(java.util.stream.Collectors.toSet());

    //    this.laneIdMap =
    //        roads.values().stream()
    //            .flatMap(road -> road.lanes().stream())
    //            .collect(java.util.stream.Collectors.toMap(Lane::id, lane -> lane));
  }

  public List<Lane> getLanesFor(Movement movement) {
    Road road = roads.get(movement.from());

    return road == null ? List.of() : road.getLanesAllowingMovement(movement.to());
  }

  public Set<Movement> getAllMovements() {
    return movements;
  }

  public List<Direction> getApproachDirections() {
    return roads.keySet().stream().toList();
  }

  public Road getRoad(Direction direction) {
    return roads.get(direction);
  }
}

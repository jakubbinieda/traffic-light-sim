package lol.omg.jakubbinieda.sim.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import lol.omg.jakubbinieda.sim.model.Direction;
import lol.omg.jakubbinieda.sim.model.Movement;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class IntersectionLayoutTest {
  @Test
  @DisplayName("Construction throws NullPointerException when roads is null")
  public void Construction_throws_NullPointerException_when_roads_is_null() {
    Exception e = assertThrows(NullPointerException.class, () -> new IntersectionLayout(null));
    assertEquals("roads cannot be null", e.getMessage());
  }

  @Test
  @DisplayName("Getting lanes returns empty list when movement's from direction is not in roads")
  public void Getting_lanes_returns_empty_list_when_movement_from_direction_not_in_roads() {
    IntersectionLayout layout = new IntersectionLayout(java.util.Map.of());
    List<Lane> lanes = layout.getLanesFor(new Movement(Direction.EAST, Direction.WEST));
    assertEquals(List.of(), lanes);
  }

  @Test
  @DisplayName("Getting lanes returns lanes for movement from correct road")
  public void Getting_lanes_returns_lanes_for_movement_from_correct_road() {
    Lane lane1 =
        new Lane("lane1", Direction.NORTH, Set.of(new Movement(Direction.NORTH, Direction.SOUTH)));
    Lane lane2 =
        new Lane("lane2", Direction.NORTH, Set.of(new Movement(Direction.NORTH, Direction.EAST)));
    Road road = new Road(Direction.NORTH, List.of(lane1, lane2));
    IntersectionLayout layout = new IntersectionLayout(java.util.Map.of(Direction.NORTH, road));

    List<Lane> lanes = layout.getLanesFor(new Movement(Direction.NORTH, Direction.SOUTH));
    assertEquals(List.of(lane1), lanes);
  }

  @Test
  @DisplayName("Getting movements returns all movements from all roads")
  public void Getting_movements_returns_all_movements_from_all_roads() {
    Lane lane1 =
        new Lane("lane1", Direction.NORTH, Set.of(new Movement(Direction.NORTH, Direction.SOUTH)));
    Lane lane2 =
        new Lane("lane2", Direction.NORTH, Set.of(new Movement(Direction.NORTH, Direction.EAST)));
    Road road1 = new Road(Direction.NORTH, List.of(lane1, lane2));

    Lane lane3 =
        new Lane("lane3", Direction.SOUTH, Set.of(new Movement(Direction.SOUTH, Direction.NORTH)));
    Road road2 = new Road(Direction.SOUTH, List.of(lane3));

    IntersectionLayout layout =
        new IntersectionLayout(java.util.Map.of(Direction.NORTH, road1, Direction.SOUTH, road2));

    Set<Movement> movements = layout.getAllMovements();
    assertEquals(
        Set.of(
            new Movement(Direction.NORTH, Direction.SOUTH),
            new Movement(Direction.NORTH, Direction.EAST),
            new Movement(Direction.SOUTH, Direction.NORTH)),
        movements);
  }

  @Test
  @DisplayName("Getting directions returns all approach directions from roads")
  public void Getting_directions_returns_all_approach_directions_from_roads() {
    Road road1 =
        new Road(
            Direction.NORTH,
            List.of(
                new Lane(
                    "lane1",
                    Direction.NORTH,
                    Set.of(new Movement(Direction.NORTH, Direction.SOUTH)))));
    Road road2 =
        new Road(
            Direction.SOUTH,
            List.of(
                new Lane(
                    "lane2",
                    Direction.SOUTH,
                    Set.of(new Movement(Direction.SOUTH, Direction.NORTH)))));

    IntersectionLayout layout =
        new IntersectionLayout(java.util.Map.of(Direction.NORTH, road1, Direction.SOUTH, road2));

    List<Direction> directions = layout.getApproachDirections();
    assertTrue(directions.contains(Direction.NORTH));
    assertTrue(directions.contains(Direction.SOUTH));
    assertEquals(2, directions.size());
  }

  @Test
  @DisplayName("Getting roads returns correct road for given direction")
  public void Getting_roads_returns_correct_road_for_given_direction() {
    Road road1 =
        new Road(
            Direction.NORTH,
            List.of(
                new Lane(
                    "lane1",
                    Direction.NORTH,
                    Set.of(new Movement(Direction.NORTH, Direction.SOUTH)))));
    Road road2 =
        new Road(
            Direction.SOUTH,
            List.of(
                new Lane(
                    "lane2",
                    Direction.SOUTH,
                    Set.of(new Movement(Direction.SOUTH, Direction.NORTH)))));

    IntersectionLayout layout =
        new IntersectionLayout(java.util.Map.of(Direction.NORTH, road1, Direction.SOUTH, road2));

    assertEquals(road1, layout.getRoad(Direction.NORTH));
    assertEquals(road2, layout.getRoad(Direction.SOUTH));
  }
}

package lol.omg.jakubbinieda.sim.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
<<<<<<< feat/io-and-runner
import java.util.Map;
import java.util.Set;
import lol.omg.jakubbinieda.sim.model.Direction;
import lol.omg.jakubbinieda.sim.model.Movement;
import lol.omg.jakubbinieda.sim.signal.SignalGroup;
=======
import java.util.Set;
import lol.omg.jakubbinieda.sim.model.Direction;
import lol.omg.jakubbinieda.sim.model.Movement;
>>>>>>> main
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class IntersectionLayoutTest {
<<<<<<< feat/io-and-runner
  static SignalGroup sg(String id, Movement... movements) {
    return new SignalGroup(id, Set.of(movements));
  }

  @Test
  @DisplayName("Construction throws NullPointerException when roads is null")
  public void Construction_throws_NullPointerException_when_roads_is_null() {
    Exception e =
        assertThrows(
            NullPointerException.class,
            () ->
                new IntersectionLayout(
                    null, List.of(sg("sg", new Movement(Direction.NORTH, Direction.SOUTH)))));
=======
  @Test
  @DisplayName("Construction throws NullPointerException when roads is null")
  public void Construction_throws_NullPointerException_when_roads_is_null() {
    Exception e = assertThrows(NullPointerException.class, () -> new IntersectionLayout(null));
>>>>>>> main
    assertEquals("roads cannot be null", e.getMessage());
  }

  @Test
<<<<<<< feat/io-and-runner
  @DisplayName("Construction throws NullPointerException when signalGroups is null")
  public void Construction_throws_NullPointerException_when_signalGroups_is_null() {
    Exception e =
        assertThrows(NullPointerException.class, () -> new IntersectionLayout(Map.of(), null));
    assertEquals("signalGroups cannot be null", e.getMessage());
  }

  @Test
  @DisplayName("Construction throws IllegalArgumentException when signalGroups is empty")
  public void Construction_throws_IllegalArgumentException_when_signalGroups_is_empty() {
    Exception e =
        assertThrows(
            IllegalArgumentException.class, () -> new IntersectionLayout(Map.of(), List.of()));
    assertEquals("signalGroups cannot be empty", e.getMessage());
  }

  @Test
  @DisplayName(
      "Construction throws IllegalArgumentException when signalGroups does not cover all movements")
  public void Construction_throws_when_signalGroups_does_not_cover_all_movements() {
    Lane lane1 =
        new Lane("lane1", Direction.NORTH, Set.of(new Movement(Direction.NORTH, Direction.SOUTH)));
    Lane lane2 =
        new Lane("lane2", Direction.EAST, Set.of(new Movement(Direction.EAST, Direction.WEST)));
    Road road1 = new Road(Direction.NORTH, List.of(lane1));
    Road road2 = new Road(Direction.EAST, List.of(lane2));

    Exception e =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                new IntersectionLayout(
                    Map.of(Direction.NORTH, road1, Direction.EAST, road2),
                    List.of(sg("sg", new Movement(Direction.NORTH, Direction.SOUTH)))));
    assertEquals(
        "Movement Movement[from=EAST, to=WEST] is not covered by any signal group", e.getMessage());
  }

  @Test
  @DisplayName("Getting lanes returns empty list when movement's from direction is not in roads")
  public void Getting_lanes_returns_empty_list_when_movement_from_direction_not_in_roads() {
    Lane lane =
        new Lane("lane1", Direction.NORTH, Set.of(new Movement(Direction.NORTH, Direction.SOUTH)));
    Road road = new Road(Direction.NORTH, List.of(lane));
    IntersectionLayout layout =
        new IntersectionLayout(
            Map.of(Direction.NORTH, road),
            List.of(sg("sg", new Movement(Direction.NORTH, Direction.SOUTH))));

    assertEquals(List.of(), layout.getLanesFor(new Movement(Direction.EAST, Direction.WEST)));
=======
  @DisplayName("Getting lanes returns empty list when movement's from direction is not in roads")
  public void Getting_lanes_returns_empty_list_when_movement_from_direction_not_in_roads() {
    IntersectionLayout layout = new IntersectionLayout(java.util.Map.of());
    List<Lane> lanes = layout.getLanesFor(new Movement(Direction.EAST, Direction.WEST));
    assertEquals(List.of(), lanes);
>>>>>>> main
  }

  @Test
  @DisplayName("Getting lanes returns lanes for movement from correct road")
  public void Getting_lanes_returns_lanes_for_movement_from_correct_road() {
    Lane lane1 =
        new Lane("lane1", Direction.NORTH, Set.of(new Movement(Direction.NORTH, Direction.SOUTH)));
    Lane lane2 =
        new Lane("lane2", Direction.NORTH, Set.of(new Movement(Direction.NORTH, Direction.EAST)));
    Road road = new Road(Direction.NORTH, List.of(lane1, lane2));
<<<<<<< feat/io-and-runner
    IntersectionLayout layout =
        new IntersectionLayout(
            Map.of(Direction.NORTH, road),
            List.of(
                sg(
                    "sg",
                    new Movement(Direction.NORTH, Direction.SOUTH),
                    new Movement(Direction.NORTH, Direction.EAST))));

    assertEquals(
        List.of(lane1), layout.getLanesFor(new Movement(Direction.NORTH, Direction.SOUTH)));
=======
    IntersectionLayout layout = new IntersectionLayout(java.util.Map.of(Direction.NORTH, road));

    List<Lane> lanes = layout.getLanesFor(new Movement(Direction.NORTH, Direction.SOUTH));
    assertEquals(List.of(lane1), lanes);
>>>>>>> main
  }

  @Test
  @DisplayName("Getting movements returns all movements from all roads")
  public void Getting_movements_returns_all_movements_from_all_roads() {
    Lane lane1 =
        new Lane("lane1", Direction.NORTH, Set.of(new Movement(Direction.NORTH, Direction.SOUTH)));
    Lane lane2 =
        new Lane("lane2", Direction.NORTH, Set.of(new Movement(Direction.NORTH, Direction.EAST)));
<<<<<<< feat/io-and-runner
    Lane lane3 =
        new Lane("lane3", Direction.SOUTH, Set.of(new Movement(Direction.SOUTH, Direction.NORTH)));

    Road road1 = new Road(Direction.NORTH, List.of(lane1, lane2));
    Road road2 = new Road(Direction.SOUTH, List.of(lane3));

    IntersectionLayout layout =
        new IntersectionLayout(
            Map.of(Direction.NORTH, road1, Direction.SOUTH, road2),
            List.of(
                sg(
                    "sg",
                    new Movement(Direction.NORTH, Direction.SOUTH),
                    new Movement(Direction.NORTH, Direction.EAST),
                    new Movement(Direction.SOUTH, Direction.NORTH))));

=======
    Road road1 = new Road(Direction.NORTH, List.of(lane1, lane2));

    Lane lane3 =
        new Lane("lane3", Direction.SOUTH, Set.of(new Movement(Direction.SOUTH, Direction.NORTH)));
    Road road2 = new Road(Direction.SOUTH, List.of(lane3));

    IntersectionLayout layout =
        new IntersectionLayout(java.util.Map.of(Direction.NORTH, road1, Direction.SOUTH, road2));

    Set<Movement> movements = layout.getAllMovements();
>>>>>>> main
    assertEquals(
        Set.of(
            new Movement(Direction.NORTH, Direction.SOUTH),
            new Movement(Direction.NORTH, Direction.EAST),
            new Movement(Direction.SOUTH, Direction.NORTH)),
<<<<<<< feat/io-and-runner
        layout.getAllMovements());
  }

  @Test
  @DisplayName("Getting signal groups returns signal groups passed to constructor")
  public void Getting_signal_groups_returns_signal_groups() {
    Lane lane =
        new Lane("lane1", Direction.NORTH, Set.of(new Movement(Direction.NORTH, Direction.SOUTH)));
    Road road = new Road(Direction.NORTH, List.of(lane));
    SignalGroup group = sg("sg", new Movement(Direction.NORTH, Direction.SOUTH));
    IntersectionLayout layout =
        new IntersectionLayout(Map.of(Direction.NORTH, road), List.of(group));

    assertEquals(List.of(group), layout.getSignalGroups());
=======
        movements);
>>>>>>> main
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
<<<<<<< feat/io-and-runner
        new IntersectionLayout(
            java.util.Map.of(Direction.NORTH, road1, Direction.SOUTH, road2),
            List.of(
                sg(
                    "sg",
                    new Movement(Direction.NORTH, Direction.SOUTH),
                    new Movement(Direction.SOUTH, Direction.NORTH))));
=======
        new IntersectionLayout(java.util.Map.of(Direction.NORTH, road1, Direction.SOUTH, road2));
>>>>>>> main

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

<<<<<<< feat/io-and-runner
    SignalGroup group =
        sg(
            "sg",
            new Movement(Direction.NORTH, Direction.SOUTH),
            new Movement(Direction.SOUTH, Direction.NORTH));
    IntersectionLayout layout =
        new IntersectionLayout(
            Map.of(Direction.NORTH, road1, Direction.SOUTH, road2), List.of(group));
=======
    IntersectionLayout layout =
        new IntersectionLayout(java.util.Map.of(Direction.NORTH, road1, Direction.SOUTH, road2));
>>>>>>> main

    assertEquals(road1, layout.getRoad(Direction.NORTH));
    assertEquals(road2, layout.getRoad(Direction.SOUTH));
  }
}

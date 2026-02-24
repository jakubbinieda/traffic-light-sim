package lol.omg.jakubbinieda.sim.engine.loadbalancer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Random;
import java.util.Set;
import lol.omg.jakubbinieda.sim.geometry.Lane;
import lol.omg.jakubbinieda.sim.model.Direction;
import lol.omg.jakubbinieda.sim.model.Movement;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class RandomBalancerTest {
  @Test
  @DisplayName("Construction throws NullPointerException when random is null")
  public void Construction_throws_NullPointerException_when_random_is_null() {
    Exception e = assertThrows(NullPointerException.class, () -> new RandomBalancer(null));
    assertEquals("random cannot be null", e.getMessage());
  }

  @Test
  @DisplayName("Selecting lanes throws NullPointerException when lanes is null")
  public void Selecting_lanes_throws_NullPointerException_when_lanes_is_null() {
    RandomBalancer balancer = new RandomBalancer();
    Exception e = assertThrows(NullPointerException.class, () -> balancer.selectLane(null));
    assertEquals("lanes cannot be null", e.getMessage());
  }

  @Test
  @DisplayName("Selecting lanes throws IllegalArgumentException when lanes is empty")
  public void Selecting_lanes_throws_IllegalArgumentException_when_lanes_is_empty() {
    RandomBalancer balancer = new RandomBalancer();
    Exception e =
        assertThrows(
            IllegalArgumentException.class,
            () -> balancer.selectLane(java.util.Collections.emptyList()));
    assertEquals("lanes cannot be empty", e.getMessage());
  }

  @Test
  @DisplayName("Selecting lanes returns a lane from the list")
  public void Selecting_lanes_returns_a_lane_from_the_list() {
    Random random = Mockito.mock(Random.class);
    Mockito.when(random.nextInt(3)).thenReturn(1);

    RandomBalancer balancer = new RandomBalancer(random);
    Lane lane1 =
        new Lane("l1", Direction.NORTH, Set.of(new Movement(Direction.NORTH, Direction.EAST)));
    Lane lane2 =
        new Lane("l2", Direction.SOUTH, Set.of(new Movement(Direction.SOUTH, Direction.EAST)));
    Lane lane3 =
        new Lane("l3", Direction.EAST, Set.of(new Movement(Direction.EAST, Direction.NORTH)));
    List<Lane> lanes = java.util.List.of(lane1, lane2, lane3);

    assertEquals(lanes.get(random.nextInt(lanes.size())), balancer.selectLane(lanes));
  }
}

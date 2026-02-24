package lol.omg.jakubbinieda.sim.factories;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.*;
import lol.omg.jakubbinieda.sim.Factories.SimpleIntersectionFactory;
import lol.omg.jakubbinieda.sim.controller.Controller;
import lol.omg.jakubbinieda.sim.engine.Intersection;
import lol.omg.jakubbinieda.sim.engine.loadbalancer.LoadBalancer;
import lol.omg.jakubbinieda.sim.geometry.IntersectionLayout;
import lol.omg.jakubbinieda.sim.geometry.Lane;
import lol.omg.jakubbinieda.sim.geometry.Road;
import lol.omg.jakubbinieda.sim.model.Direction;
import lol.omg.jakubbinieda.sim.model.Movement;
import lol.omg.jakubbinieda.sim.signal.SignalGroup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

public class SimpleIntersectionFactoryTest {
  private Controller controller;
  private LoadBalancer loadBalancer;

  @BeforeEach
  public void setup() {
    controller = Mockito.mock(Controller.class);
    loadBalancer = Mockito.mock(LoadBalancer.class);
  }

  @Test
  @DisplayName("There is only one instance")
  public void There_is_only_one_instance() {
    SimpleIntersectionFactory instance1 = SimpleIntersectionFactory.getInstance();
    SimpleIntersectionFactory instance2 = SimpleIntersectionFactory.getInstance();
    assertSame(instance1, instance2);
  }

  @Test
  @DisplayName("INSTANCE field matches getInstance")
  public void Instance_field_matches_getInstance() {
    assertSame(SimpleIntersectionFactory.INSTANCE, SimpleIntersectionFactory.getInstance());
  }

  @Test
  @DisplayName("Controller is initialized")
  public void Controller_is_initialized() {
    SimpleIntersectionFactory factory = SimpleIntersectionFactory.getInstance();
    factory.supply(controller, loadBalancer);
    Mockito.verify(controller).initialize(Mockito.any());
  }

  @Test
  @DisplayName("Supply returns non-null intersection")
  public void Supply_returns_non_null_intersection() {
    SimpleIntersectionFactory factory = SimpleIntersectionFactory.getInstance();
    Intersection intersection = factory.supply(controller, loadBalancer);
    assertSame(Intersection.class, intersection.getClass());
  }

  @Test
  @DisplayName("Layout has four approach directions")
  public void Layout_has_four_approach_directions() {
    IntersectionLayout layout = captureLayout();
    List<Direction> approaches = layout.getApproachDirections();
    assertEquals(4, approaches.size());
    assertTrue(
        approaches.containsAll(
            List.of(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST)));
  }

  @Test
  @DisplayName("Each direction has a non-null road")
  public void Each_direction_has_a_non_null_road() {
    IntersectionLayout layout = captureLayout();
    for (Direction dir : Direction.values()) {
      assertNotNull(layout.getRoad(dir));
    }
  }

  @Test
  @DisplayName("Each road has 4 movements")
  public void Each_road_has_4_movements() {
    IntersectionLayout layout = captureLayout();
    for (Direction from : Direction.values()) {
      Road road = layout.getRoad(from);
      List<Movement> movements = road.getAllMovements();
      assertEquals(4, movements.size());
      for (Movement m : movements) {
        assertEquals(from, m.from());
      }
    }
  }

  @Test
  @DisplayName("Every movement has a lane")
  public void Every_movement_has_a_lane() {
    IntersectionLayout layout = captureLayout();
    for (Movement m : layout.getAllMovements()) {
      List<Lane> lanes = layout.getLanesFor(m);
      assertFalse(lanes.isEmpty());
    }
  }

  @Test
  @DisplayName("Each lane approach matches its direction")
  public void Each_lane_approach_matches_its_direction() {
    IntersectionLayout layout = captureLayout();
    for (Direction from : Direction.values()) {
      Movement m = new Movement(from, from == Direction.NORTH ? Direction.SOUTH : Direction.NORTH);
      if (from != Direction.NORTH && from != Direction.SOUTH) {
        m = new Movement(from, from == Direction.EAST ? Direction.WEST : Direction.EAST);
      }
      List<Lane> lanes = layout.getLanesFor(m);
      for (Lane lane : lanes) {
        assertEquals(from, lane.approach());
      }
    }
  }

  @Test
  @DisplayName("Each lane allows 4 movements and all from its approach")
  public void Each_lane_allows_4_movements_and_all_from_its_approach() {
    IntersectionLayout layout = captureLayout();
    Set<Lane> seen = new HashSet<>();
    for (Movement m : layout.getAllMovements()) {
      for (Lane lane : layout.getLanesFor(m)) {
        if (seen.add(lane)) {
          assertEquals(4, lane.allowedMovements().size());
          for (Movement am : lane.allowedMovements()) {
            assertEquals(lane.approach(), am.from());
          }
        }
      }
    }
    assertEquals(4, seen.size());
  }

  @Test
  @DisplayName("Layout has four signal groups")
  public void Layout_has_four_signal_groups() {
    IntersectionLayout layout = captureLayout();
    assertEquals(4, layout.getSignalGroups().size());
  }

  @Test
  @DisplayName("Signal group movements match lane movements for same direction")
  public void Signal_group_movements_match_lane_movements_for_same_direction() {
    IntersectionLayout layout = captureLayout();
    for (SignalGroup sg : layout.getSignalGroups()) {
      Movement sample = sg.movements().iterator().next();
      List<Lane> lanes = layout.getLanesFor(sample);
      assertEquals(1, lanes.size());
      assertEquals(lanes.getFirst().allowedMovements(), sg.movements());
    }
  }

  @Test
  @DisplayName("Total of 16 unique movements across layout")
  public void Total_of_16_unique_movements_across_layout() {
    IntersectionLayout layout = captureLayout();
    assertEquals(16, layout.getAllMovements().size());
  }

  @Test
  @DisplayName("Lane allowsMovement returns true for its movements")
  public void Lane_allowsMovement_returns_true_for_its_movements() {
    IntersectionLayout layout = captureLayout();
    for (Movement m : layout.getAllMovements()) {
      List<Lane> lanes = layout.getLanesFor(m);
      for (Lane lane : lanes) {
        assertTrue(lane.allowsMovement(m));
      }
    }
  }

  private IntersectionLayout captureLayout() {
    SimpleIntersectionFactory.getInstance().supply(controller, loadBalancer);
    ArgumentCaptor<IntersectionLayout> captor = ArgumentCaptor.forClass(IntersectionLayout.class);
    Mockito.verify(controller).initialize(captor.capture());
    return captor.getValue();
  }
}

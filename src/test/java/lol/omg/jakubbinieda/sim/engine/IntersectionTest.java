package lol.omg.jakubbinieda.sim.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lol.omg.jakubbinieda.sim.controller.Controller;
import lol.omg.jakubbinieda.sim.engine.loadbalancer.LoadBalancer;
import lol.omg.jakubbinieda.sim.geometry.IntersectionLayout;
import lol.omg.jakubbinieda.sim.geometry.Lane;
import lol.omg.jakubbinieda.sim.geometry.Road;
import lol.omg.jakubbinieda.sim.model.Direction;
import lol.omg.jakubbinieda.sim.model.Movement;
import lol.omg.jakubbinieda.sim.model.Vehicle;
import lol.omg.jakubbinieda.sim.signal.SignalCommand;
import lol.omg.jakubbinieda.sim.signal.SignalGroup;
import lol.omg.jakubbinieda.sim.signal.SignalState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class IntersectionTest {

  static Movement mv(Direction from, Direction to) {
    return new Movement(from, to);
  }

  // Create a simple 4-way intersection
  static Intersection createIntersection(Controller controller) {
    Map<Direction, Lane> lanes = new EnumMap<>(Direction.class);
    Map<Direction, Set<Movement>> movements = new EnumMap<>(Direction.class);
    Set<Movement> allMovements = new HashSet<>();

    for (Direction from : Direction.values()) {
      Set<Movement> laneMovements = new HashSet<>();
      for (Direction to : Direction.values()) {
        if (to != from) {
          Movement m = mv(from, to);
          laneMovements.add(m);
          allMovements.add(m);
        }
      }

      lanes.put(from, new Lane(from.name().toLowerCase() + "-0", from, laneMovements));
      movements.put(from, laneMovements);
    }

    List<SignalGroup> signalGroups = new ArrayList<>();
    for (Direction d : Direction.values()) {
      signalGroups.add(new SignalGroup("sg-" + d.name().toLowerCase(), movements.get(d)));
    }

    IntersectionLayout layout = Mockito.mock(IntersectionLayout.class);
    Mockito.when(layout.getAllMovements()).thenReturn(allMovements);
    Mockito.when(layout.getApproachDirections()).thenReturn(List.of(Direction.values()));
    Mockito.when(layout.getSignalGroups()).thenReturn(signalGroups);

    for (Direction d : Direction.values()) {
      for (Movement m : movements.get(d)) {
        Mockito.when(layout.getLanesFor(m)).thenReturn(List.of(lanes.get(d)));
      }

      Road road = Mockito.mock(Road.class);
      Mockito.when(road.lanes()).thenReturn(List.of(lanes.get(d)));
      Mockito.when(layout.getRoad(d)).thenReturn(road);
    }

    LoadBalancer loadBalancer = Mockito.mock(LoadBalancer.class);
    Mockito.when(loadBalancer.selectLane(Mockito.any()))
        .thenAnswer(inv -> ((List<Lane>) inv.getArgument(0)).getFirst());

    return new Intersection(layout, controller, loadBalancer);
  }

  static Controller fixedController(SignalCommand... commands) {
    Controller ctrl = Mockito.mock(Controller.class);
    Mockito.when(ctrl.decide(Mockito.any())).thenReturn(List.of(commands));
    return ctrl;
  }

  static Controller greenController(String... sgIds) {
    SignalCommand[] cmds = new SignalCommand[sgIds.length];
    for (int i = 0; i < sgIds.length; i++) {
      cmds[i] = new SignalCommand(sgIds[i], SignalState.GREEN);
    }
    return fixedController(cmds);
  }

  @Nested
  public class ConstructorTest {
    @Test
    @DisplayName("Construction throws NullPointerException when layout is null")
    public void Construction_throws_NullPointerException_when_layout_is_null() {
      Controller controller = Mockito.mock(Controller.class);
      LoadBalancer loadBalancer = Mockito.mock(LoadBalancer.class);

      Exception e =
          assertThrows(
              NullPointerException.class, () -> new Intersection(null, controller, loadBalancer));
      assertEquals("layout cannot be null", e.getMessage());
    }

    @Test
    @DisplayName("Construction throws NullPointerException when controller is null")
    public void Construction_throws_NullPointerException_when_controller_is_null() {
      IntersectionLayout layout = Mockito.mock(IntersectionLayout.class);
      LoadBalancer loadBalancer = Mockito.mock(LoadBalancer.class);

      Exception e =
          assertThrows(
              NullPointerException.class, () -> new Intersection(layout, null, loadBalancer));
      assertEquals("controller cannot be null", e.getMessage());
    }

    @Test
    @DisplayName("Construction throws NullPointerException when loadBalancer is null")
    public void Construction_throws_NullPointerException_when_loadBalancer_is_null() {
      IntersectionLayout layout = Mockito.mock(IntersectionLayout.class);
      Controller controller = Mockito.mock(Controller.class);

      Exception e =
          assertThrows(
              NullPointerException.class, () -> new Intersection(layout, controller, null));
      assertEquals("loadBalancer cannot be null", e.getMessage());
    }

    @Test
    @DisplayName("Construction succeeds when all parameters are valid")
    public void Construction_succeeds_when_all_parameters_are_valid() {
      IntersectionLayout layout = Mockito.mock(IntersectionLayout.class);
      Mockito.when(layout.getSignalGroups())
          .thenReturn(
              List.of(new SignalGroup("sg-1", Set.of(mv(Direction.NORTH, Direction.SOUTH)))));
      Controller controller = Mockito.mock(Controller.class);
      LoadBalancer loadBalancer = Mockito.mock(LoadBalancer.class);

      new Intersection(layout, controller, loadBalancer);
    }
  }

  @Nested
  public class AddVehicleTest {
    private Intersection intersection;

    @BeforeEach
    public void setup() {
      IntersectionLayout layout = Mockito.mock(IntersectionLayout.class);

      Mockito.when(layout.getAllMovements())
          .thenReturn(Set.of(new Movement(Direction.NORTH, Direction.SOUTH)));

      Lane lane1 =
          new Lane(
              "lane1", Direction.NORTH, Set.of(new Movement(Direction.NORTH, Direction.SOUTH)));

      Mockito.when(layout.getLanesFor(Mockito.eq(new Movement(Direction.NORTH, Direction.SOUTH))))
          .thenReturn(List.of(lane1));
      Mockito.when(layout.getLanesFor(Mockito.eq(new Movement(Direction.EAST, Direction.WEST))))
          .thenReturn(List.of());

      Controller controller = Mockito.mock(Controller.class);
      LoadBalancer loadBalancer = Mockito.mock(LoadBalancer.class);
      Mockito.when(loadBalancer.selectLane(Mockito.any())).thenReturn(lane1);

      SignalGroup signalGroup1 = Mockito.mock(SignalGroup.class);
      Mockito.when(signalGroup1.movements())
          .thenReturn(Set.of(new Movement(Direction.NORTH, Direction.SOUTH)));
      Mockito.when(signalGroup1.id()).thenReturn("sg-1");

      intersection = new Intersection(layout, controller, loadBalancer);
    }

    @Test
    @DisplayName("Adding vehicle throws NullPointerException when vehicle is null")
    public void Adding_vehicle_throws_NullPointerException_when_vehicle_is_null() {
      Exception e = assertThrows(NullPointerException.class, () -> intersection.addVehicle(null));
      assertEquals("vehicle cannot be null", e.getMessage());
    }

    @Test
    @DisplayName("Adding vehicle throws IllegalArgumentException when vehicle already exists")
    public void Adding_vehicle_throws_IllegalArgumentException_when_vehicle_already_exists() {
      Vehicle vehicle = new Vehicle("v1", new Movement(Direction.NORTH, Direction.SOUTH));
      intersection.addVehicle(vehicle);

      Exception e =
          assertThrows(IllegalArgumentException.class, () -> intersection.addVehicle(vehicle));
      assertEquals("Vehicle with ID v1 already exists", e.getMessage());
    }

    @Test
    @DisplayName(
        "Adding vehicle throws IllegalArgumentException when no lanes are available for movement")
    public void
        Adding_vehicle_throws_IllegalArgumentException_when_no_lanes_are_available_for_movement() {
      Vehicle vehicle = new Vehicle("v2", new Movement(Direction.EAST, Direction.WEST));
      assertThrows(IllegalArgumentException.class, () -> intersection.addVehicle(vehicle));
    }

    @Test
    @DisplayName("Adding vehicle succeeds when vehicle is valid")
    public void Adding_vehicle_succeeds_when_vehicle_is_valid() {
      Vehicle vehicle = new Vehicle("v1", new Movement(Direction.NORTH, Direction.SOUTH));
      intersection.addVehicle(vehicle);
    }
  }

  @Nested
  class StepTest {

    @Test
    @DisplayName("Step increments step count")
    void Step_increments_step_count() {
      Intersection intersection = createIntersection(fixedController());
      assertEquals(1, intersection.step().step());
      assertEquals(2, intersection.step().step());
      assertEquals(3, intersection.step().step());
    }

    @Test
    @DisplayName("Step returns correct signal states")
    void Step_returns_correct_signal_states() {
      Intersection intersection = createIntersection(greenController("sg-south"));
      StepResult result = intersection.step();
      assertEquals(SignalState.GREEN, result.signalStates().get("sg-south"));
      assertEquals(SignalState.RED, result.signalStates().get("sg-north"));
    }

    @Test
    @DisplayName("Step with all RED and vehicles exit")
    void Step_with_all_RED_and_no_vehicles_exit() {
      Intersection intersection = createIntersection(fixedController());
      intersection.addVehicle(new Vehicle("v1", mv(Direction.SOUTH, Direction.NORTH)));
      StepResult result = intersection.step();
      assertTrue(result.leftIntersection().isEmpty());
    }

    @Test
    @DisplayName("Step with no vehicles returns empty results")
    void Step_with_no_vehicles_returns_empty_results() {
      Intersection intersection = createIntersection(greenController("sg-south"));
      StepResult result = intersection.step();
      assertTrue(result.leftIntersection().isEmpty());
    }

    @Test
    @DisplayName("Step returns queue lengths")
    void Step_returns_queue_lengths() {
      Intersection intersection = createIntersection(fixedController());
      intersection.addVehicle(new Vehicle("v1", mv(Direction.SOUTH, Direction.NORTH)));
      intersection.addVehicle(new Vehicle("v2", mv(Direction.SOUTH, Direction.EAST)));
      StepResult result = intersection.step();
      assertEquals(2, result.queueLengths().get("south-0"));
    }
  }

  @Nested
  class ApplyCommandsTest {
    @Test
    @DisplayName("Correct signal group command is applied")
    void Correct_signal_group_command_is_applied() {
      Intersection intersection =
          createIntersection(fixedController(new SignalCommand("sg-south", SignalState.GREEN)));
      StepResult result = intersection.step();
      assertEquals(SignalState.GREEN, result.signalStates().get("sg-south"));
    }

    @Test
    @DisplayName("Unknown signal group command is ignored")
    void Unknown_signal_group_command_is_ignored() {
      Intersection intersection =
          createIntersection(
              fixedController(new SignalCommand("sg-nfkjashgfk", SignalState.GREEN)));
      StepResult result = intersection.step();
      assertEquals(SignalState.RED, result.signalStates().get("sg-south"));
    }

    @Test
    @DisplayName("Empty command list changes nothing")
    void Empty_command_list_changes_nothing() {
      Intersection intersection = createIntersection(fixedController());
      StepResult result = intersection.step();
      for (SignalState state : result.signalStates().values()) {
        assertEquals(SignalState.RED, state);
      }
    }

    @Test
    @DisplayName("Multiple commands applied in same step")
    void Multiple_commands_applied_in_same_step() {
      Intersection intersection =
          createIntersection(
              fixedController(
                  new SignalCommand("sg-south", SignalState.GREEN),
                  new SignalCommand("sg-north", SignalState.GREEN),
                  new SignalCommand("sg-east", SignalState.YELLOW)));
      StepResult result = intersection.step();
      assertEquals(SignalState.GREEN, result.signalStates().get("sg-south"));
      assertEquals(SignalState.GREEN, result.signalStates().get("sg-north"));
      assertEquals(SignalState.YELLOW, result.signalStates().get("sg-east"));
      assertEquals(SignalState.RED, result.signalStates().get("sg-west"));
    }
  }

  @Nested
  class CrossingTimeTest {
    @Test
    @DisplayName("STRAIGHT crosses in 1 step")
    void STRAIGHT_crosses_in_1_step() {
      Intersection intersection = createIntersection(greenController("sg-south"));
      intersection.addVehicle(new Vehicle("v1", mv(Direction.SOUTH, Direction.NORTH)));
      intersection.step();
      StepResult result = intersection.step();
      assertTrue(result.leftIntersection().contains("v1"));
    }

    @Test
    @DisplayName("RIGHT crosses in 1 step")
    void RIGHT_crosses_in_1_step() {
      Intersection intersection = createIntersection(greenController("sg-south"));
      intersection.addVehicle(new Vehicle("v1", mv(Direction.SOUTH, Direction.EAST)));
      intersection.step();
      StepResult result = intersection.step();
      assertTrue(result.leftIntersection().contains("v1"));
    }

    @Test
    @DisplayName("LEFT crosses in 1 step")
    void LEFT_crosses_in_1_step() {
      Intersection intersection = createIntersection(greenController("sg-south"));
      intersection.addVehicle(new Vehicle("v1", mv(Direction.SOUTH, Direction.WEST)));
      intersection.step();
      assertTrue(intersection.step().leftIntersection().contains("v1"));
    }
  }

  @Nested
  class ExitVehiclesTest {
    @Test
    @DisplayName("Vehicle exits after crossing time expires")
    void Vehicle_exits_after_crossing_time_expires() {
      Intersection intersection = createIntersection(greenController("sg-south"));
      intersection.addVehicle(new Vehicle("v1", mv(Direction.SOUTH, Direction.NORTH)));
      intersection.step();
      assertTrue(intersection.step().leftIntersection().contains("v1"));
    }

    @Test
    @DisplayName("Multiple vehicles can exit in same step")
    void Multiple_vehicles_can_exit_in_the_same_step() {
      Intersection intersection = createIntersection(greenController("sg-south", "sg-north"));
      intersection.addVehicle(new Vehicle("v1", mv(Direction.SOUTH, Direction.NORTH)));
      intersection.addVehicle(new Vehicle("v2", mv(Direction.NORTH, Direction.SOUTH)));
      intersection.step();
      StepResult result = intersection.step();
      assertTrue(result.leftIntersection().contains("v1"));
      assertTrue(result.leftIntersection().contains("v2"));
    }
  }

  @Nested
  class EnterVehiclesTest {
    @Test
    @DisplayName("Vehicle with RED does not enter")
    void Vehicle_with_RED_signal_does_not_enter() {
      Intersection intersection = createIntersection(fixedController());
      intersection.addVehicle(new Vehicle("v1", mv(Direction.SOUTH, Direction.NORTH)));
      StepResult result = intersection.step();
      assertEquals(1, result.queueLengths().get("south-0"));
    }

    @Test
    @DisplayName("Vehicle with YELLOW does not enter")
    void Vehicle_with_YELLOW_signal_does_not_enter() {
      Intersection intersection =
          createIntersection(fixedController(new SignalCommand("sg-south", SignalState.YELLOW)));
      intersection.addVehicle(new Vehicle("v1", mv(Direction.SOUTH, Direction.NORTH)));
      StepResult result = intersection.step();
      assertEquals(1, result.queueLengths().get("south-0"));
    }

    @Test
    @DisplayName("Vehicle with RED_YELLOW does not enter")
    void Vehicle_with_RED_YELLOW_does_not_enter() {
      Intersection intersection =
          createIntersection(
              fixedController(new SignalCommand("sg-south", SignalState.RED_YELLOW)));
      intersection.addVehicle(new Vehicle("v1", mv(Direction.SOUTH, Direction.NORTH)));
      StepResult result = intersection.step();
      assertEquals(1, result.queueLengths().get("south-0"));
    }

    @Test
    @DisplayName("Vehicle with GREEN enters")
    void Vehicle_with_GREEN_enters() {
      Intersection intersection = createIntersection(greenController("sg-south"));
      intersection.addVehicle(new Vehicle("v1", mv(Direction.SOUTH, Direction.NORTH)));
      StepResult result = intersection.step();
      assertEquals(0, result.queueLengths().get("south-0"));
    }

    @Test
    @DisplayName("Vehicle with GREEN_ARROW enters")
    void Vehicle_with_GREEN_ARROW_enters() {
      Intersection intersection =
          createIntersection(
              fixedController(new SignalCommand("sg-south", SignalState.GREEN_ARROW)));
      intersection.addVehicle(new Vehicle("v1", mv(Direction.SOUTH, Direction.EAST)));
      StepResult result = intersection.step();
      assertEquals(0, result.queueLengths().get("south-0"));
    }

    @Test
    @DisplayName("Only front of queue enters")
    void Only_front_of_queue_enters() {
      Intersection intersection = createIntersection(greenController("sg-south"));
      intersection.addVehicle(new Vehicle("v1", mv(Direction.SOUTH, Direction.NORTH)));
      intersection.addVehicle(new Vehicle("v2", mv(Direction.SOUTH, Direction.NORTH)));
      StepResult result = intersection.step();
      assertEquals(1, result.queueLengths().get("south-0"));
    }

    @Test
    @DisplayName("First vehicle exits before second enters")
    void First_vehicle_exits_before_second_enters() {
      Intersection intersection = createIntersection(greenController("sg-south"));
      intersection.addVehicle(new Vehicle("v1", mv(Direction.SOUTH, Direction.NORTH)));
      intersection.addVehicle(new Vehicle("v2", mv(Direction.SOUTH, Direction.NORTH)));

      intersection.step();
      assertTrue(intersection.step().leftIntersection().contains("v1"));
      assertTrue(intersection.step().leftIntersection().contains("v2"));
    }

    @Test
    @DisplayName("Wait time increments for queued vehicles")
    void Wait_time_increments_for_queued_vehicles() {
      Intersection intersection = createIntersection(fixedController());
      Vehicle vehicle = new Vehicle("v1", mv(Direction.SOUTH, Direction.NORTH));
      intersection.addVehicle(vehicle);
      intersection.step();
      intersection.step();
      intersection.step();
      assertEquals(3, vehicle.getWaitTime());
    }

    @Test
    @DisplayName("Empty lane does nothing")
    void Empty_lane_does_nothing() {
      Intersection intersection = createIntersection(greenController("sg-south"));
      StepResult result = intersection.step();
      assertTrue(result.leftIntersection().isEmpty());
    }
  }

  @Nested
  class ConflictTest {
    @Test
    @DisplayName("Opposing straights do not conflict")
    void Opposing_straights_do_not_conflict() {
      Intersection intersection = createIntersection(greenController("sg-south", "sg-north"));
      intersection.addVehicle(new Vehicle("v1", mv(Direction.SOUTH, Direction.NORTH)));
      intersection.addVehicle(new Vehicle("v2", mv(Direction.NORTH, Direction.SOUTH)));
      intersection.step();
      StepResult result = intersection.step();
      assertTrue(result.leftIntersection().contains("v1"));
      assertTrue(result.leftIntersection().contains("v2"));
    }

    @Test
    @DisplayName("Opposing rights do not conflict")
    void Opposing_rights_do_not_conflict() {
      Intersection intersection = createIntersection(greenController("sg-south", "sg-north"));
      intersection.addVehicle(new Vehicle("v1", mv(Direction.SOUTH, Direction.EAST)));
      intersection.addVehicle(new Vehicle("v2", mv(Direction.NORTH, Direction.WEST)));
      intersection.step();
      StepResult result = intersection.step();
      assertTrue(result.leftIntersection().contains("v1"));
      assertTrue(result.leftIntersection().contains("v2"));
    }

    @Test
    @DisplayName("Opposing lefts do not conflict")
    void Opposing_lefts_do_not_conflict() {
      Intersection intersection = createIntersection(greenController("sg-south", "sg-north"));
      intersection.addVehicle(new Vehicle("left-s", mv(Direction.SOUTH, Direction.WEST)));
      intersection.addVehicle(new Vehicle("left-n", mv(Direction.NORTH, Direction.EAST)));
      intersection.step();
      StepResult result = intersection.step();
      assertTrue(result.leftIntersection().contains("left-s"));
      assertTrue(result.leftIntersection().contains("left-n"));
    }

    @Test
    @DisplayName("Straight and opposing right do not conflict")
    void Straight_and_opposing_right_do_not_conflict() {
      Intersection intersection = createIntersection(greenController("sg-south", "sg-north"));
      intersection.addVehicle(new Vehicle("straight", mv(Direction.SOUTH, Direction.NORTH)));
      intersection.addVehicle(new Vehicle("right", mv(Direction.NORTH, Direction.WEST)));
      intersection.step();
      StepResult result = intersection.step();
      assertTrue(result.leftIntersection().contains("straight"));
      assertTrue(result.leftIntersection().contains("right"));
    }

    @Test
    @DisplayName("Left yields to opposing straight")
    void Left_yields_to_opposing_straight() {
      Intersection intersection = createIntersection(greenController("sg-south", "sg-north"));
      intersection.addVehicle(new Vehicle("straight", mv(Direction.NORTH, Direction.SOUTH)));
      intersection.addVehicle(new Vehicle("left", mv(Direction.SOUTH, Direction.WEST)));

      intersection.step();
      StepResult result = intersection.step();
      assertTrue(result.leftIntersection().contains("straight"));
      assertFalse(result.leftIntersection().contains("left"));
      assertTrue(intersection.step().leftIntersection().contains("left"));
    }

    @Test
    @DisplayName("Left yields to opposing right")
    void Left_yields_to_opposing_right() {
      Intersection intersection = createIntersection(greenController("sg-south", "sg-north"));
      intersection.addVehicle(new Vehicle("left", mv(Direction.SOUTH, Direction.WEST)));
      intersection.addVehicle(new Vehicle("right", mv(Direction.NORTH, Direction.WEST)));

      intersection.step();
      assertTrue(intersection.step().leftIntersection().contains("right"));
      assertTrue(intersection.step().leftIntersection().contains("left"));
    }

    @Test
    @DisplayName("Perpendicular rights do not conflict")
    void Perpendicular_right_do_not_conflict() {
      Intersection intersection = createIntersection(greenController("sg-south", "sg-east"));
      intersection.addVehicle(new Vehicle("right-s", mv(Direction.SOUTH, Direction.EAST)));
      intersection.addVehicle(new Vehicle("right-e", mv(Direction.EAST, Direction.NORTH)));
      intersection.step();
      StepResult result = intersection.step();
      assertTrue(result.leftIntersection().contains("right-s"));
      assertTrue(result.leftIntersection().contains("right-e"));
    }

    @Test
    @DisplayName("Perpendicular straight and right conflicts")
    void Perpendicular_straight_and_right_conflicts() {
      Intersection intersection = createIntersection(greenController("sg-south", "sg-east"));
      intersection.addVehicle(new Vehicle("straight", mv(Direction.SOUTH, Direction.NORTH)));
      intersection.addVehicle(new Vehicle("right", mv(Direction.EAST, Direction.NORTH)));

      intersection.step();
      assertTrue(intersection.step().leftIntersection().contains("straight"));
      intersection.step();
      assertFalse(intersection.step().leftIntersection().contains("right"));
    }

    @Test
    @DisplayName("Perpendicular straight and left conflicts")
    void Perpendicular_straight_and_left_conflicts() {
      Intersection intersection = createIntersection(greenController("sg-south", "sg-east"));
      intersection.addVehicle(new Vehicle("straight", mv(Direction.SOUTH, Direction.NORTH)));
      intersection.addVehicle(new Vehicle("left", mv(Direction.EAST, Direction.SOUTH)));

      intersection.step();
      assertTrue(intersection.step().leftIntersection().contains("straight"));
      assertTrue(intersection.step().leftIntersection().contains("left"));
    }

    @Test
    @DisplayName("Perpendicular left and right conflicts")
    void Perpendicular_left_and_right_conflicts() {
      Intersection intersection = createIntersection(greenController("sg-south", "sg-east"));
      intersection.addVehicle(new Vehicle("right", mv(Direction.SOUTH, Direction.EAST)));
      intersection.addVehicle(new Vehicle("left", mv(Direction.EAST, Direction.SOUTH)));

      intersection.step();
      assertTrue(intersection.step().leftIntersection().contains("right"));
      assertTrue(intersection.step().leftIntersection().contains("left"));
    }

    @Test
    @DisplayName("Same approach different destination does not conflict")
    void Same_approach_different_destination_does_not_conflict() {
      Intersection intersection = createIntersection(greenController("sg-south"));
      intersection.addVehicle(new Vehicle("v1", mv(Direction.SOUTH, Direction.NORTH)));
      intersection.addVehicle(new Vehicle("v2", mv(Direction.SOUTH, Direction.EAST)));

      intersection.step();
      assertTrue(intersection.step().leftIntersection().contains("v1"));
      assertTrue(intersection.step().leftIntersection().contains("v2"));
    }

    @Test
    @DisplayName("Same approach same destination does not conflict")
    void Same_approach_same_destination_does_not_conflict() {
      Intersection intersection = createIntersection(greenController("sg-south"));
      intersection.addVehicle(new Vehicle("v1", mv(Direction.SOUTH, Direction.NORTH)));
      intersection.addVehicle(new Vehicle("v2", mv(Direction.SOUTH, Direction.NORTH)));

      intersection.step();
      assertTrue(intersection.step().leftIntersection().contains("v1"));
      assertTrue(intersection.step().leftIntersection().contains("v2"));
    }

    @Test
    @DisplayName("Exited vehicle is not reported as crossing in next step")
    void Exited_vehicle_is_not_reported_as_crossing_in_next_step() {
      Controller controller = Mockito.mock(Controller.class);
      List<IntersectionState> capturedStates = new ArrayList<>();
      Mockito.when(controller.decide(Mockito.any()))
          .thenAnswer(
              inv -> {
                capturedStates.add(inv.getArgument(0));
                return List.of(new SignalCommand("sg-south", SignalState.GREEN));
              });

      Intersection intersection = createIntersection(controller);
      intersection.addVehicle(new Vehicle("v1", mv(Direction.SOUTH, Direction.NORTH)));

      intersection.step();
      intersection.step();
      intersection.step();

      IntersectionState state = capturedStates.get(2);
      assertFalse(state.vehiclesOnIntersection().contains(mv(Direction.SOUTH, Direction.NORTH)));
    }

    @Test
    @DisplayName("Same approach movements do not conflict multi-lane")
    void Same_approach_movements_do_not_conflict_multi_lane() {
      Lane laneSouth0 =
          new Lane("south-0", Direction.SOUTH, Set.of(mv(Direction.SOUTH, Direction.NORTH)));
      Lane laneSouth1 =
          new Lane("south-1", Direction.SOUTH, Set.of(mv(Direction.SOUTH, Direction.EAST)));

      Set<Movement> allMovements =
          Set.of(mv(Direction.SOUTH, Direction.NORTH), mv(Direction.SOUTH, Direction.EAST));

      SignalGroup sg = new SignalGroup("sg-south", allMovements);

      IntersectionLayout layout = Mockito.mock(IntersectionLayout.class);
      Mockito.when(layout.getAllMovements()).thenReturn(allMovements);
      Mockito.when(layout.getApproachDirections()).thenReturn(List.of(Direction.SOUTH));
      Mockito.when(layout.getLanesFor(mv(Direction.SOUTH, Direction.NORTH)))
          .thenReturn(List.of(laneSouth0));
      Mockito.when(layout.getLanesFor(mv(Direction.SOUTH, Direction.EAST)))
          .thenReturn(List.of(laneSouth1));
      Mockito.when(layout.getSignalGroups()).thenReturn(List.of(sg));

      Road road = Mockito.mock(Road.class);
      Mockito.when(road.lanes()).thenReturn(List.of(laneSouth0, laneSouth1));
      Mockito.when(layout.getRoad(Direction.SOUTH)).thenReturn(road);

      LoadBalancer loadBalancer = Mockito.mock(LoadBalancer.class);
      Mockito.when(loadBalancer.selectLane(Mockito.any()))
          .thenAnswer(inv -> ((List<Lane>) inv.getArgument(0)).getFirst());

      Controller controller = fixedController(new SignalCommand("sg-south", SignalState.GREEN));

      Intersection intersection = new Intersection(layout, controller, loadBalancer);
      intersection.addVehicle(new Vehicle("v1", mv(Direction.SOUTH, Direction.NORTH)));
      intersection.addVehicle(new Vehicle("v2", mv(Direction.SOUTH, Direction.EAST)));

      StepResult result = intersection.step();
      assertEquals(0, result.queueLengths().get("south-0"));
      assertEquals(0, result.queueLengths().get("south-1"));
    }
  }

  @Nested
  class GreenArrowTest {
    @Test
    @DisplayName("Right turn proceeds when way is clear")
    void Right_turn_proceeds_when_way_is_clear() {
      Intersection intersection =
          createIntersection(
              fixedController(new SignalCommand("sg-south", SignalState.GREEN_ARROW)));
      intersection.addVehicle(new Vehicle("right", mv(Direction.SOUTH, Direction.EAST)));
      intersection.step();
      assertTrue(intersection.step().leftIntersection().contains("right"));
    }

    @Test
    @DisplayName("Right turn yields to perpendicular straight")
    void Right_turn_yields_to_perpendicular_straight() {
      Intersection intersection =
          createIntersection(
              fixedController(
                  new SignalCommand("sg-south", SignalState.GREEN_ARROW),
                  new SignalCommand("sg-east", SignalState.GREEN)));
      intersection.addVehicle(new Vehicle("right", mv(Direction.SOUTH, Direction.EAST)));
      intersection.addVehicle(new Vehicle("straight", mv(Direction.EAST, Direction.WEST)));

      intersection.step();

      StepResult result = intersection.step();
      assertTrue(result.leftIntersection().contains("straight"));
      assertFalse(result.leftIntersection().contains("right"));

      assertTrue(intersection.step().leftIntersection().contains("right"));
    }

    @Test
    @DisplayName("Right turn does not yield to perpendicular left")
    void Right_turn_does_not_yield_to_perpendicular_left() {
      Intersection intersection =
          createIntersection(
              fixedController(
                  new SignalCommand("sg-south", SignalState.GREEN_ARROW),
                  new SignalCommand("sg-east", SignalState.GREEN)));
      intersection.addVehicle(new Vehicle("right", mv(Direction.SOUTH, Direction.EAST)));
      intersection.addVehicle(new Vehicle("left", mv(Direction.EAST, Direction.SOUTH)));

      intersection.step();
      assertTrue(intersection.step().leftIntersection().contains("right"));
      StepResult result = intersection.step();
      assertTrue(result.leftIntersection().contains("left"));
      assertFalse(result.leftIntersection().contains("right"));
    }

    @Test
    @DisplayName("Perpendicular rights are compatible")
    void Perpendicular_rights_are_compatible() {
      Intersection iintersection =
          createIntersection(
              fixedController(
                  new SignalCommand("sg-south", SignalState.GREEN_ARROW),
                  new SignalCommand("sg-east", SignalState.GREEN_ARROW)));
      iintersection.addVehicle(new Vehicle("right-s", mv(Direction.SOUTH, Direction.EAST)));
      iintersection.addVehicle(new Vehicle("right-e", mv(Direction.EAST, Direction.NORTH)));
      iintersection.step();
      StepResult result = iintersection.step();
      assertTrue(result.leftIntersection().contains("right-s"));
      assertTrue(result.leftIntersection().contains("right-e"));
    }
  }

  @Nested
  class MultiStepTest {
    @Test
    @DisplayName("Vehicles added mid-simulation work")
    void Vehicles_added_mid_simulation_work() {
      Intersection intersection = createIntersection(greenController("sg-south"));
      intersection.addVehicle(new Vehicle("v1", mv(Direction.SOUTH, Direction.NORTH)));
      intersection.step();
      intersection.addVehicle(new Vehicle("v2", mv(Direction.SOUTH, Direction.NORTH)));
      assertTrue(intersection.step().leftIntersection().contains("v1"));
      assertTrue(intersection.step().leftIntersection().contains("v2"));
    }

    @Test
    @DisplayName("Signal changes between steps works")
    void Signal_changes_between_steps_works() {
      Controller controller = Mockito.mock(Controller.class);
      Mockito.when(controller.decide(Mockito.any()))
          .thenReturn(List.of(new SignalCommand("sg-south", SignalState.GREEN)))
          .thenReturn(List.of())
          .thenReturn(
              List.of(
                  new SignalCommand("sg-south", SignalState.RED),
                  new SignalCommand("sg-east", SignalState.GREEN)))
          .thenReturn(List.of());

      Intersection intersection = createIntersection(controller);
      intersection.addVehicle(new Vehicle("v-south", mv(Direction.SOUTH, Direction.NORTH)));
      intersection.addVehicle(new Vehicle("v-east", mv(Direction.EAST, Direction.WEST)));

      intersection.step();
      assertTrue(intersection.step().leftIntersection().contains("v-south"));
      intersection.step();
      assertTrue(intersection.step().leftIntersection().contains("v-east"));
    }

    @Test
    @DisplayName("Left turn waits for all opposing straights to clear")
    void Left_turn_waits_fors_all_opposing_straights_to_clear() {
      Intersection intersection = createIntersection(greenController("sg-south", "sg-north"));
      intersection.addVehicle(new Vehicle("left", mv(Direction.SOUTH, Direction.WEST)));
      intersection.addVehicle(new Vehicle("straight1", mv(Direction.NORTH, Direction.SOUTH)));

      intersection.step();

      intersection.addVehicle(new Vehicle("straight2", mv(Direction.NORTH, Direction.SOUTH)));

      StepResult result = intersection.step();
      assertTrue(result.leftIntersection().contains("straight1"));

      result = intersection.step();
      assertTrue(result.leftIntersection().contains("straight2"));
      assertTrue(intersection.step().leftIntersection().contains("left"));
    }
  }

  @Nested
  class CurrentIntersectionStateTest {
    @Test
    @DisplayName("State includes vehicles on intersection")
    void State_includes_vehicles_on_intersection() {
      Controller controller = Mockito.mock(Controller.class);

      List<IntersectionState> capturedStates = new ArrayList<>();
      Mockito.when(controller.decide(Mockito.any()))
          .thenAnswer(
              inv -> {
                capturedStates.add(inv.getArgument(0));
                return List.of(new SignalCommand("sg-south", SignalState.GREEN));
              });

      Intersection intersection = createIntersection(controller);
      intersection.addVehicle(new Vehicle("v1", mv(Direction.SOUTH, Direction.NORTH)));

      intersection.step();

      intersection.step();
      IntersectionState state = capturedStates.get(1);
      assertTrue(state.vehiclesOnIntersection().contains(mv(Direction.SOUTH, Direction.NORTH)));
    }

    @Test
    @DisplayName("State includes waiting per road counts")
    void State_includes_waiting_per_road_counts() {
      Controller controller = Mockito.mock(Controller.class);
      List<IntersectionState> capturedStates = new ArrayList<>();
      Mockito.when(controller.decide(Mockito.any()))
          .thenAnswer(
              inv -> {
                capturedStates.add(inv.getArgument(0));
                return List.of();
              });

      Intersection intersection = createIntersection(controller);
      intersection.addVehicle(new Vehicle("v1", mv(Direction.SOUTH, Direction.NORTH)));
      intersection.addVehicle(new Vehicle("v2", mv(Direction.SOUTH, Direction.EAST)));
      intersection.addVehicle(new Vehicle("v3", mv(Direction.EAST, Direction.WEST)));

      intersection.step();
      IntersectionState state = capturedStates.getFirst();
      assertEquals(2, state.waitingPerRoad().get(Direction.SOUTH));
      assertEquals(1, state.waitingPerRoad().get(Direction.EAST));
      assertEquals(0, state.waitingPerRoad().get(Direction.NORTH));
    }

    @Test
    @DisplayName("State includes queue lengths per lane")
    void State_includes_queue_lengths_per_lane() {
      Controller controller = Mockito.mock(Controller.class);
      List<IntersectionState> capturedStates = new ArrayList<>();
      Mockito.when(controller.decide(Mockito.any()))
          .thenAnswer(
              inv -> {
                capturedStates.add(inv.getArgument(0));
                return List.of();
              });

      Intersection intersection = createIntersection(controller);
      intersection.addVehicle(new Vehicle("v1", mv(Direction.SOUTH, Direction.NORTH)));

      intersection.step();
      IntersectionState state = capturedStates.getFirst();
      assertEquals(1, state.queueLengths().get("south-0"));
    }

    @Test
    @DisplayName("State does not include queued vehicles as crossing")
    void State_does_not_include_queued_vehicles_as_crossing() {
      Controller controller = Mockito.mock(Controller.class);
      List<IntersectionState> capturedStates = new ArrayList<>();
      Mockito.when(controller.decide(Mockito.any()))
          .thenAnswer(
              inv -> {
                capturedStates.add(inv.getArgument(0));
                return List.of(new SignalCommand("sg-south", SignalState.GREEN));
              });

      Intersection intersection = createIntersection(controller);
      intersection.addVehicle(new Vehicle("v1", mv(Direction.SOUTH, Direction.NORTH)));
      intersection.addVehicle(new Vehicle("v2", mv(Direction.SOUTH, Direction.EAST)));

      intersection.step();
      intersection.step();
      IntersectionState state = capturedStates.get(1);

      assertTrue(state.vehiclesOnIntersection().contains(mv(Direction.SOUTH, Direction.NORTH)));
      assertFalse(state.vehiclesOnIntersection().contains(mv(Direction.SOUTH, Direction.EAST)));
    }
  }
}

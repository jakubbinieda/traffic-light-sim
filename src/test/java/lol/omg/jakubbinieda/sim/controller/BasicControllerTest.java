package lol.omg.jakubbinieda.sim.controller;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import lol.omg.jakubbinieda.sim.engine.IntersectionState;
import lol.omg.jakubbinieda.sim.geometry.IntersectionLayout;
import lol.omg.jakubbinieda.sim.geometry.Lane;
import lol.omg.jakubbinieda.sim.geometry.Road;
import lol.omg.jakubbinieda.sim.model.Direction;
import lol.omg.jakubbinieda.sim.model.Movement;
import lol.omg.jakubbinieda.sim.signal.SignalCommand;
import lol.omg.jakubbinieda.sim.signal.SignalGroup;
import lol.omg.jakubbinieda.sim.signal.SignalState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class BasicControllerTest {

  static Movement mv(Direction from, Direction to) {
    return new Movement(from, to);
  }

  static SignalGroup sg(String id, Movement... movements) {
    return new SignalGroup(id, Set.of(movements));
  }

  static IntersectionLayout twoPhaseLayout() {
    SignalGroup sgNorth = sg("sg-north", mv(Direction.NORTH, Direction.SOUTH));
    SignalGroup sgSouth = sg("sg-south", mv(Direction.SOUTH, Direction.NORTH));
    SignalGroup sgEast = sg("sg-east", mv(Direction.EAST, Direction.WEST));
    SignalGroup sgWest = sg("sg-west", mv(Direction.WEST, Direction.EAST));

    Lane laneN = new Lane("north-0", Direction.NORTH, Set.of(mv(Direction.NORTH, Direction.SOUTH)));
    Lane laneS = new Lane("south-0", Direction.SOUTH, Set.of(mv(Direction.SOUTH, Direction.NORTH)));
    Lane laneE = new Lane("east-0", Direction.EAST, Set.of(mv(Direction.EAST, Direction.WEST)));
    Lane laneW = new Lane("west-0", Direction.WEST, Set.of(mv(Direction.WEST, Direction.EAST)));

    Road roadN = new Road(Direction.NORTH, List.of(laneN));
    Road roadS = new Road(Direction.SOUTH, List.of(laneS));
    Road roadE = new Road(Direction.EAST, List.of(laneE));
    Road roadW = new Road(Direction.WEST, List.of(laneW));

    return new IntersectionLayout(
        Map.of(
            Direction.NORTH,
            roadN,
            Direction.SOUTH,
            roadS,
            Direction.EAST,
            roadE,
            Direction.WEST,
            roadW),
        List.of(sgNorth, sgSouth, sgEast, sgWest));
  }

  static IntersectionLayout threePhaseLayout() {
    SignalGroup sgA = sg("sg-a", mv(Direction.SOUTH, Direction.NORTH));
    SignalGroup sgB = sg("sg-b", mv(Direction.EAST, Direction.WEST));
    SignalGroup sgC = sg("sg-c", mv(Direction.NORTH, Direction.EAST)); // left turn

    Lane laneS = new Lane("south-0", Direction.SOUTH, Set.of(mv(Direction.SOUTH, Direction.NORTH)));
    Lane laneE = new Lane("east-0", Direction.EAST, Set.of(mv(Direction.EAST, Direction.WEST)));
    Lane laneN = new Lane("north-0", Direction.NORTH, Set.of(mv(Direction.NORTH, Direction.EAST)));

    return new IntersectionLayout(
        Map.of(
            Direction.SOUTH, new Road(Direction.SOUTH, List.of(laneS)),
            Direction.EAST, new Road(Direction.EAST, List.of(laneE)),
            Direction.NORTH, new Road(Direction.NORTH, List.of(laneN))),
        List.of(sgA, sgB, sgC));
  }

  static Map<Direction, Integer> waiting3(int south, int east, int north) {
    Map<Direction, Integer> map = new EnumMap<>(Direction.class);
    map.put(Direction.SOUTH, south);
    map.put(Direction.EAST, east);
    map.put(Direction.NORTH, north);
    return map;
  }

  static IntersectionLayout singlePhaseLayout() {
    SignalGroup sgNorth = sg("sg-north", mv(Direction.NORTH, Direction.SOUTH));
    SignalGroup sgSouth = sg("sg-south", mv(Direction.SOUTH, Direction.NORTH));

    Lane laneN = new Lane("north-0", Direction.NORTH, Set.of(mv(Direction.NORTH, Direction.SOUTH)));
    Lane laneS = new Lane("south-0", Direction.SOUTH, Set.of(mv(Direction.SOUTH, Direction.NORTH)));

    Road roadN = new Road(Direction.NORTH, List.of(laneN));
    Road roadS = new Road(Direction.SOUTH, List.of(laneS));

    return new IntersectionLayout(
        Map.of(Direction.NORTH, roadN, Direction.SOUTH, roadS), List.of(sgNorth, sgSouth));
  }

  static IntersectionState state(int step, Map<Direction, Integer> waiting) {
    return new IntersectionState(step, Map.of(), Map.of(), waiting, Set.of());
  }

  static Map<Direction, Integer> waiting(int north, int south, int east, int west) {
    Map<Direction, Integer> map = new EnumMap<>(Direction.class);
    map.put(Direction.NORTH, north);
    map.put(Direction.SOUTH, south);
    map.put(Direction.EAST, east);
    map.put(Direction.WEST, west);
    return map;
  }

  static SignalState stateOf(List<SignalCommand> cmds, String sgId) {
    return cmds.stream()
        .filter(c -> c.signalGroupId().equals(sgId))
        .map(SignalCommand::newState)
        .findFirst()
        .orElse(null);
  }

  @Nested
  class ConstructorTest {
    @ParameterizedTest
    @ValueSource(ints = {0, -1, -5, Integer.MIN_VALUE})
    @DisplayName("Construction throws when minGreen is less than 1")
    void Construction_throws_when_minGreen_is_less_than_1(int minGreen) {
      Exception e =
          assertThrows(IllegalArgumentException.class, () -> new BasicController(minGreen, 5));
      assertEquals("minGreen must be positive", e.getMessage());
    }

    @ParameterizedTest
    @ValueSource(ints = {4, 3, 1})
    @DisplayName("Construction throws when maxGreen is less than minGreen")
    void Construction_throws_when_maxGreen_is_less_than_minGreen(int maxGreen) {
      Exception e =
          assertThrows(IllegalArgumentException.class, () -> new BasicController(5, maxGreen));
      assertEquals("maxGreen must be >= minGreen", e.getMessage());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 5, 10})
    @DisplayName("Construction succeeds with minGreen and maxGreen equal and valid")
    void Construction_succeeds_with_valid_min_and_max_green(int green) {
      assertDoesNotThrow(() -> new BasicController(green, green));
    }
  }

  @Nested
  class initializeTest {
    @Test
    @DisplayName("Throws when layout is null")
    void Throws_when_layout_is_null() {
      BasicController controller = new BasicController(3, 10);
      assertThrows(NullPointerException.class, () -> controller.initialize(null));
    }

    @Test
    @DisplayName("Deciding without initializing throws")
    void Deciding_without_initializing_throws() {
      BasicController controller = new BasicController(3, 10);
      assertThrows(
          IllegalStateException.class, () -> controller.decide(state(1, waiting(0, 0, 0, 0))));
    }
  }

  @Nested
  class SignalTransitionTest {
    private BasicController controller;

    @BeforeEach
    void setup() {
      controller = new BasicController(2, 5);
      controller.initialize(twoPhaseLayout());
    }

    @Test
    @DisplayName("First decide produces RED_YELLOW for selected phase, RED for others")
    void First_decide_produces_RED_YELLOW_for_selected_phase_RED_for_others() {
      List<SignalCommand> cmds = controller.decide(state(1, waiting(5, 5, 0, 0)));

      assertEquals(SignalState.RED_YELLOW, stateOf(cmds, "sg-north"));
      assertEquals(SignalState.RED_YELLOW, stateOf(cmds, "sg-south"));
      assertEquals(SignalState.RED, stateOf(cmds, "sg-east"));
      assertEquals(SignalState.RED, stateOf(cmds, "sg-west"));
    }

    @Test
    @DisplayName("Second decide produces GREEN for selected phase")
    void Second_decide_produces_GREEN_for_selected_phase() {
      controller.decide(state(1, waiting(5, 5, 0, 0)));
      List<SignalCommand> cmds = controller.decide(state(2, waiting(5, 5, 0, 0)));

      assertEquals(SignalState.GREEN, stateOf(cmds, "sg-north"));
      assertEquals(SignalState.GREEN, stateOf(cmds, "sg-south"));
      assertEquals(SignalState.RED, stateOf(cmds, "sg-east"));
      assertEquals(SignalState.RED, stateOf(cmds, "sg-west"));
    }

    @Test
    @DisplayName("Full cycle works")
    void Full_cycle_works() {
      int step = 1;

      List<SignalCommand> cmds = controller.decide(state(step++, waiting(5, 5, 0, 0)));
      assertEquals(SignalState.RED_YELLOW, stateOf(cmds, "sg-north"));

      cmds = controller.decide(state(step++, waiting(4, 4, 0, 0)));
      assertEquals(SignalState.GREEN, stateOf(cmds, "sg-north"));

      cmds = controller.decide(state(step++, waiting(0, 0, 10, 10)));
      assertTrue(cmds.isEmpty());

      cmds = controller.decide(state(step++, waiting(0, 0, 10, 10)));
      assertEquals(SignalState.YELLOW, stateOf(cmds, "sg-north"));
      assertEquals(SignalState.YELLOW, stateOf(cmds, "sg-south"));
      assertEquals(SignalState.RED, stateOf(cmds, "sg-east"));

      cmds = controller.decide(state(step++, waiting(0, 0, 10, 10)));
      assertEquals(SignalState.RED, stateOf(cmds, "sg-north"));
      assertEquals(SignalState.RED, stateOf(cmds, "sg-east"));

      cmds = controller.decide(state(step++, waiting(0, 0, 10, 10)));
      assertEquals(SignalState.RED, stateOf(cmds, "sg-north"));
      assertEquals(SignalState.RED_YELLOW, stateOf(cmds, "sg-east"));
      assertEquals(SignalState.RED_YELLOW, stateOf(cmds, "sg-west"));

      cmds = controller.decide(state(step++, waiting(0, 0, 8, 8)));
      assertEquals(SignalState.GREEN, stateOf(cmds, "sg-east"));
      assertEquals(SignalState.GREEN, stateOf(cmds, "sg-west"));
      assertEquals(SignalState.RED, stateOf(cmds, "sg-north"));
    }
  }

  @Nested
  class DemandSelectionTest {
    private BasicController controller;

    @BeforeEach
    void setup() {
      controller = new BasicController(2, 5);
      controller.initialize(twoPhaseLayout());
    }

    @Test
    @DisplayName("Selects phase with higher demand")
    void Selects_phase_with_higher_demand() {
      List<SignalCommand> cmds = controller.decide(state(1, waiting(1, 1, 10, 10)));

      assertEquals(SignalState.RED_YELLOW, stateOf(cmds, "sg-east"));
      assertEquals(SignalState.RED_YELLOW, stateOf(cmds, "sg-west"));
      assertEquals(SignalState.RED, stateOf(cmds, "sg-north"));
      assertEquals(SignalState.RED, stateOf(cmds, "sg-south"));
    }

    @Test
    @DisplayName("No switch before minGreen with higher demand")
    void No_switch_before_minGreen_with_higher_demand() {
      controller.decide(state(1, waiting(5, 5, 0, 0)));
      controller.decide(state(2, waiting(5, 5, 0, 0)));

      List<SignalCommand> cmds = controller.decide(state(3, waiting(0, 0, 100, 100)));
      assertTrue(cmds.isEmpty());
    }

    @Test
    @DisplayName("Switch after minGreen with higher demand")
    void Switch_after_minGreen_with_higher_demand() {
      controller.decide(state(1, waiting(5, 5, 0, 0)));
      controller.decide(state(2, waiting(5, 5, 0, 0)));

      controller.decide(state(3, waiting(0, 0, 10, 10)));

      List<SignalCommand> cmds = controller.decide(state(4, waiting(0, 0, 10, 10)));
      assertEquals(SignalState.YELLOW, stateOf(cmds, "sg-north"));
    }

    @Test
    @DisplayName("No switch after minGreen without higher demand")
    void No_switch_after_minGreen_without_higher_demand() {
      controller.decide(state(1, waiting(5, 5, 0, 0)));
      controller.decide(state(2, waiting(5, 5, 0, 0)));

      controller.decide(state(3, waiting(10, 10, 0, 0)));
      List<SignalCommand> cmds = controller.decide(state(4, waiting(10, 10, 1, 1)));
      assertTrue(cmds.isEmpty());
    }

    @Test
    @DisplayName("Equal demand does not trigger switch")
    void Equal_demand_does_not_trigger_switch() {
      controller.decide(state(1, waiting(5, 5, 0, 0)));
      controller.decide(state(2, waiting(5, 5, 0, 0)));

      controller.decide(state(3, waiting(5, 5, 5, 5)));

      List<SignalCommand> cmds = controller.decide(state(4, waiting(5, 5, 5, 5)));
      assertTrue(cmds.isEmpty());
    }
  }

  @Nested
  class MaxGreenTest {
    private BasicController controller;

    @BeforeEach
    void setup() {
      controller = new BasicController(2, 5);
      controller.initialize(twoPhaseLayout());
    }

    @Test
    @DisplayName("Switch at maxGreen without demand")
    void Switch_at_maxGreen_without_demand() {
      int step = 1;

      controller.decide(state(step++, waiting(10, 10, 0, 0)));
      controller.decide(state(step++, waiting(10, 10, 0, 0)));

      for (int i = 0; i < 4; i++) {
        controller.decide(state(step++, waiting(10, 10, 0, 0)));
      }

      List<SignalCommand> cmds = controller.decide(state(step, waiting(10, 10, 0, 0)));
      assertEquals(SignalState.YELLOW, stateOf(cmds, "sg-north"));
    }

    @Test
    @DisplayName("Round-robin when forced switch with no competing demand")
    void Round_robin_when_forced_switch_with_no_competing_demand() {
      int step = 1;

      controller.decide(state(step++, waiting(10, 10, 0, 0)));
      controller.decide(state(step++, waiting(10, 10, 0, 0)));

      for (int i = 0; i < 4; i++) {
        controller.decide(state(step++, waiting(10, 10, 0, 0)));
      }

      controller.decide(state(step++, waiting(10, 10, 0, 0)));
      controller.decide(state(step++, waiting(10, 10, 0, 0)));

      List<SignalCommand> cmds = controller.decide(state(step, waiting(10, 10, 0, 0)));
      assertEquals(SignalState.RED_YELLOW, stateOf(cmds, "sg-east"));
      assertEquals(SignalState.RED_YELLOW, stateOf(cmds, "sg-west"));
    }

    @Test
    @DisplayName("Switch at maxGreen with demand elsewhere")
    void Switch_at_maxGreen_with_demand_elswhere() {
      int step = 1;

      controller.decide(state(step++, waiting(10, 10, 0, 0)));
      controller.decide(state(step++, waiting(10, 10, 0, 0)));

      for (int i = 0; i < 4; i++) {
        controller.decide(state(step++, waiting(10, 10, 1, 1)));
      }

      List<SignalCommand> cmds = controller.decide(state(step++, waiting(0, 0, 10, 10)));
      assertEquals(SignalState.YELLOW, stateOf(cmds, "sg-north"));

      controller.decide(state(step++, waiting(0, 0, 10, 10)));
      cmds = controller.decide(state(step, waiting(0, 0, 10, 10)));
      assertEquals(SignalState.RED_YELLOW, stateOf(cmds, "sg-east"));
    }
  }

  @Nested
  class SinglePhaseTest {
    @Test
    @DisplayName("Single_phase_layout_stays_green")
    void Single_phase_layout_stays_green() {
      BasicController controller = new BasicController(2, 5);
      controller.initialize(singlePhaseLayout());

      int step = 1;

      List<SignalCommand> cmds = controller.decide(state(step++, waiting(5, 5, 0, 0)));
      assertEquals(SignalState.RED_YELLOW, stateOf(cmds, "sg-north"));

      cmds = controller.decide(state(step++, waiting(5, 5, 0, 0)));
      assertEquals(SignalState.GREEN, stateOf(cmds, "sg-north"));

      for (int i = 0; i < 20; i++) {
        cmds = controller.decide(state(step++, waiting(5, 5, 0, 0)));
        assertTrue(cmds.isEmpty());
      }
    }
  }

  @Nested
  class PhaseComputationTest {
    @Test
    @DisplayName("Opposing_straights_are_in_same_phase")
    void Opposing_straights_are_in_same_phase() {
      BasicController controller = new BasicController(3, 10);
      controller.initialize(twoPhaseLayout());

      List<SignalCommand> cmds = controller.decide(state(1, waiting(5, 5, 0, 0)));

      assertEquals(stateOf(cmds, "sg-north"), stateOf(cmds, "sg-south"));
      assertEquals(stateOf(cmds, "sg-east"), stateOf(cmds, "sg-west"));
      assertNotEquals(stateOf(cmds, "sg-north"), stateOf(cmds, "sg-east"));
    }

    @Test
    @DisplayName("Conflicting_straights_are_in_different_phases")
    void Conflicting_straights_are_in_different_phases() {
      BasicController controller = new BasicController(3, 10);
      controller.initialize(twoPhaseLayout());

      List<SignalCommand> cmds = controller.decide(state(1, waiting(0, 0, 5, 5)));

      assertEquals(SignalState.RED_YELLOW, stateOf(cmds, "sg-east"));
      assertEquals(SignalState.RED, stateOf(cmds, "sg-north"));
    }

    @Test
    @DisplayName("Phase computation handles turns")
    void Phase_computation_handles_turns() {
      SignalGroup sgSouthStraight = sg("sg-s-str", mv(Direction.SOUTH, Direction.NORTH));
      SignalGroup sgSouthRight = sg("sg-s-right", mv(Direction.SOUTH, Direction.EAST));
      SignalGroup sgSouthLeft = sg("sg-s-left", mv(Direction.SOUTH, Direction.WEST));
      SignalGroup sgNorthStraight = sg("sg-n-str", mv(Direction.NORTH, Direction.SOUTH));
      SignalGroup sgNorthLeft = sg("sg-n-left", mv(Direction.NORTH, Direction.EAST));
      SignalGroup sgEastRight = sg("sg-e-right", mv(Direction.EAST, Direction.NORTH));

      Lane laneSStr =
          new Lane("s-str", Direction.SOUTH, Set.of(mv(Direction.SOUTH, Direction.NORTH)));
      Lane laneSRight =
          new Lane("s-right", Direction.SOUTH, Set.of(mv(Direction.SOUTH, Direction.EAST)));
      Lane laneSLeft =
          new Lane("s-left", Direction.SOUTH, Set.of(mv(Direction.SOUTH, Direction.WEST)));
      Lane laneNStr =
          new Lane("n-str", Direction.NORTH, Set.of(mv(Direction.NORTH, Direction.SOUTH)));
      Lane laneNLeft =
          new Lane("n-left", Direction.NORTH, Set.of(mv(Direction.NORTH, Direction.EAST)));
      Lane laneERight =
          new Lane("e-right", Direction.EAST, Set.of(mv(Direction.EAST, Direction.NORTH)));

      Road roadSouth = new Road(Direction.SOUTH, List.of(laneSStr, laneSRight, laneSLeft));
      Road roadNorth = new Road(Direction.NORTH, List.of(laneNStr, laneNLeft));
      Road roadEast = new Road(Direction.EAST, List.of(laneERight));

      IntersectionLayout layout =
          new IntersectionLayout(
              Map.of(
                  Direction.SOUTH, roadSouth, Direction.NORTH, roadNorth, Direction.EAST, roadEast),
              List.of(
                  sgSouthStraight,
                  sgSouthRight,
                  sgSouthLeft,
                  sgNorthStraight,
                  sgNorthLeft,
                  sgEastRight));

      BasicController controller = new BasicController(2, 5);
      controller.initialize(layout);

      Map<Direction, Integer> w = new EnumMap<>(Direction.class);
      w.put(Direction.SOUTH, 5);
      w.put(Direction.NORTH, 3);
      w.put(Direction.EAST, 1);
      List<SignalCommand> cmds =
          controller.decide(new IntersectionState(1, Map.of(), Map.of(), w, Set.of()));

      assertEquals(6, cmds.size());

      SignalState sStr = stateOf(cmds, "sg-s-str");
      SignalState sRight = stateOf(cmds, "sg-s-right");
      SignalState sLeft = stateOf(cmds, "sg-s-left");
      assertEquals(sStr, sRight);
      assertEquals(sStr, sLeft);

      SignalState nStr = stateOf(cmds, "sg-n-str");
      assertNotEquals(sLeft, nStr);

      SignalState eRight = stateOf(cmds, "sg-e-right");
      assertNotEquals(sStr, eRight);
    }

    @Test
    @DisplayName("Perpendicular rights are grouped into same phase")
    void Perpendicular_rights_are_grouped_into_same_phase() {
      SignalGroup sgSR = sg("sg-s-right", mv(Direction.SOUTH, Direction.EAST));
      SignalGroup sgER = sg("sg-e-right", mv(Direction.EAST, Direction.NORTH));

      Lane laneS =
          new Lane("south-0", Direction.SOUTH, Set.of(mv(Direction.SOUTH, Direction.EAST)));
      Lane laneE = new Lane("east-0", Direction.EAST, Set.of(mv(Direction.EAST, Direction.NORTH)));

      IntersectionLayout layout =
          new IntersectionLayout(
              Map.of(
                  Direction.SOUTH, new Road(Direction.SOUTH, List.of(laneS)),
                  Direction.EAST, new Road(Direction.EAST, List.of(laneE))),
              List.of(sgSR, sgER));

      BasicController controller = new BasicController(2, 5);
      controller.initialize(layout);

      Map<Direction, Integer> w = new EnumMap<>(Direction.class);
      w.put(Direction.SOUTH, 5);
      w.put(Direction.EAST, 5);

      List<SignalCommand> cmds = controller.decide(state(1, w));

      assertEquals(stateOf(cmds, "sg-s-right"), stateOf(cmds, "sg-e-right"));
      assertEquals(SignalState.RED_YELLOW, stateOf(cmds, "sg-s-right"));
    }
  }

  @Nested
  class ZeroDemandTest {
    @Test
    @DisplayName("No demand activates a phase")
    void No_demand_activates_a_phase() {
      BasicController controller = new BasicController(2, 5);
      controller.initialize(twoPhaseLayout());

      List<SignalCommand> cmds = controller.decide(state(1, waiting(0, 0, 0, 0)));

      long redYellowCount =
          cmds.stream().filter(c -> c.newState() == SignalState.RED_YELLOW).count();
      assertTrue(redYellowCount > 0);
    }

    @Test
    @DisplayName("Equal demand is deterministic")
    void Equal_demand_is_deterministic() {
      BasicController c1 = new BasicController(2, 5);
      c1.initialize(twoPhaseLayout());
      BasicController c2 = new BasicController(2, 5);
      c2.initialize(twoPhaseLayout());

      List<SignalCommand> cmds1 = c1.decide(state(1, waiting(3, 3, 3, 3)));
      List<SignalCommand> cmds2 = c2.decide(state(1, waiting(3, 3, 3, 3)));

      for (int i = 0; i < cmds1.size(); i++) {
        assertEquals(cmds1.get(i).signalGroupId(), cmds2.get(i).signalGroupId());
        assertEquals(cmds1.get(i).newState(), cmds2.get(i).newState());
      }
    }
  }

  @Nested
  class CommandCompletenessTest {
    @Test
    @DisplayName("Non-empty commands always cover all signal groups")
    void Non_empty_commands_always_cover_all_signal_groups() {
      BasicController controller = new BasicController(2, 5);
      controller.initialize(twoPhaseLayout());

      Set<String> allIds = Set.of("sg-north", "sg-south", "sg-east", "sg-west");

      for (int step = 1; step <= 20; step++) {
        List<SignalCommand> cmds =
            controller.decide(
                state(step, waiting(step % 2 == 0 ? 5 : 0, 0, step % 2 == 1 ? 5 : 0, 0)));

        if (!cmds.isEmpty()) {
          Set<String> commandedIds = new HashSet<>();
          cmds.forEach(c -> commandedIds.add(c.signalGroupId()));
          assertEquals(allIds, commandedIds, "Step " + step + " missing signal groups");
        }
      }
    }
  }

  @Nested
  class ThreePhaseTest {
    private BasicController controller;

    @BeforeEach
    void setup() {
      controller = new BasicController(2, 5);
      controller.initialize(threePhaseLayout());
    }

    @Test
    @DisplayName("No round-robin at maxGreen with demand elsewhere")
    void No_round_robin_at_maxGreen_with_demand_elsewhere() {
      int step = 1;

      controller.decide(state(step++, waiting3(10, 0, 0)));
      controller.decide(state(step++, waiting3(10, 0, 0)));

      for (int i = 0; i < 4; i++) {
        controller.decide(state(step++, waiting3(10, 0, 0)));
      }

      controller.decide(state(step++, waiting3(0, 0, 10)));
      controller.decide(state(step++, waiting3(0, 0, 10)));

      List<SignalCommand> cmds = controller.decide(state(step, waiting3(0, 0, 10)));
      assertEquals(SignalState.RED_YELLOW, stateOf(cmds, "sg-c"));
      assertEquals(SignalState.RED, stateOf(cmds, "sg-b"));
    }

    @Test
    @DisplayName("With equal non-active scores select first phase")
    void With_equal_non_active_scores_select_first_phase() {
      List<SignalCommand> cmds = controller.decide(state(1, waiting3(0, 5, 5)));

      assertEquals(SignalState.RED_YELLOW, stateOf(cmds, "sg-b"));
      assertEquals(SignalState.RED, stateOf(cmds, "sg-c"));
    }

    @Test
    @DisplayName("Round-robin at maxGreen without demand")
    void Round_robin_at_maxGreen_without_demand() {
      int step = 1;

      controller.decide(state(step++, waiting3(10, 0, 0)));
      controller.decide(state(step++, waiting3(10, 0, 0)));

      for (int i = 0; i < 4; i++) {
        controller.decide(state(step++, waiting3(10, 1, 2)));
      }

      controller.decide(state(step++, waiting3(10, 1, 2)));
      controller.decide(state(step++, waiting3(10, 1, 2)));

      List<SignalCommand> cmds = controller.decide(state(step, waiting3(10, 1, 2)));
      assertEquals(SignalState.RED_YELLOW, stateOf(cmds, "sg-b"));
      assertEquals(SignalState.RED, stateOf(cmds, "sg-c"));
    }
  }

  @Nested
  class ResolvedPhasesTest {
    @Test
    @DisplayName("Null phase definitions falls back to auto-compute")
    void Null_phase_definitions_falls_back_to_auto_compute() {
      BasicController controller = new BasicController(2, 5, null);
      controller.initialize(twoPhaseLayout());

      List<SignalCommand> cmds = controller.decide(state(1, waiting(5, 5, 0, 0)));
      assertEquals(stateOf(cmds, "sg-north"), stateOf(cmds, "sg-south"));
    }

    @Test
    @DisplayName("No auto computing with phases supplied")
    void No_auto_computing_with_phases_supplied() {
      List<List<String>> phases =
          List.of(List.of("sg-north"), List.of("sg-south"), List.of("sg-east", "sg-west"));

      BasicController controller = new BasicController(2, 5, phases);
      controller.initialize(twoPhaseLayout());

      List<SignalCommand> cmds = controller.decide(state(1, waiting(10, 0, 0, 0)));

      assertEquals(SignalState.RED_YELLOW, stateOf(cmds, "sg-north"));
      assertEquals(SignalState.RED, stateOf(cmds, "sg-south"));
      assertEquals(SignalState.RED, stateOf(cmds, "sg-east"));
    }

    @Test
    @DisplayName("Unknown signal group in phase definition throws")
    void Unknown_signal_group_in_phase_definition_throws() {
      List<List<String>> phases =
          List.of(List.of("sg-north", "sg-nonexistent"), List.of("sg-east", "sg-west"));

      BasicController controller = new BasicController(2, 5, phases);

      Exception e =
          assertThrows(
              IllegalArgumentException.class, () -> controller.initialize(twoPhaseLayout()));
      assertTrue(e.getMessage().contains("sg-nonexistent"));
    }

    @Test
    @DisplayName("Supplied phases control demand-based switching")
    void Supplied_phases_control_demand_based_switching() {
      List<List<String>> phases =
          List.of(List.of("sg-north", "sg-south"), List.of("sg-east", "sg-west"));

      BasicController controller = new BasicController(2, 5, phases);
      controller.initialize(twoPhaseLayout());

      int step = 1;

      controller.decide(state(step++, waiting(5, 5, 0, 0)));
      controller.decide(state(step++, waiting(5, 5, 0, 0)));

      controller.decide(state(step++, waiting(0, 0, 10, 10)));

      List<SignalCommand> cmds = controller.decide(state(step++, waiting(0, 0, 10, 10)));
      assertEquals(SignalState.YELLOW, stateOf(cmds, "sg-north"));
      assertEquals(SignalState.YELLOW, stateOf(cmds, "sg-south"));

      controller.decide(state(step++, waiting(0, 0, 10, 10)));
      cmds = controller.decide(state(step++, waiting(0, 0, 10, 10)));

      assertEquals(SignalState.RED_YELLOW, stateOf(cmds, "sg-east"));
      assertEquals(SignalState.RED_YELLOW, stateOf(cmds, "sg-west"));
      assertEquals(SignalState.RED, stateOf(cmds, "sg-north"));

      cmds = controller.decide(state(step, waiting(0, 0, 10, 10)));
      assertEquals(SignalState.GREEN, stateOf(cmds, "sg-east"));
      assertEquals(SignalState.GREEN, stateOf(cmds, "sg-west"));
    }

    @Test
    @DisplayName("Can force all groups into a single phase")
    void Can_force_all_groups_into_a_single_phase() {
      List<List<String>> phases = List.of(List.of("sg-north", "sg-south", "sg-east", "sg-west"));

      BasicController controller = new BasicController(2, 5, phases);
      controller.initialize(twoPhaseLayout());

      controller.decide(state(1, waiting(5, 5, 5, 5)));
      List<SignalCommand> cmds = controller.decide(state(2, waiting(5, 5, 5, 5)));

      assertEquals(SignalState.GREEN, stateOf(cmds, "sg-north"));
      assertEquals(SignalState.GREEN, stateOf(cmds, "sg-south"));
      assertEquals(SignalState.GREEN, stateOf(cmds, "sg-east"));
      assertEquals(SignalState.GREEN, stateOf(cmds, "sg-west"));

      for (int step = 3; step <= 20; step++) {
        cmds = controller.decide(state(step, waiting(5, 5, 5, 5)));
        assertTrue(cmds.isEmpty());
      }
    }

    @Test
    @DisplayName("Phase order determines round-robin")
    void Phase_order_determines_round_robin() {
      List<List<String>> phases =
          List.of(List.of("sg-east", "sg-west"), List.of("sg-north", "sg-south"));

      BasicController controller = new BasicController(2, 5, phases);
      controller.initialize(twoPhaseLayout());

      List<SignalCommand> cmds = controller.decide(state(1, waiting(5, 5, 5, 5)));

      assertEquals(SignalState.RED_YELLOW, stateOf(cmds, "sg-east"));
      assertEquals(SignalState.RED_YELLOW, stateOf(cmds, "sg-west"));
      assertEquals(SignalState.RED, stateOf(cmds, "sg-north"));
      assertEquals(SignalState.RED, stateOf(cmds, "sg-south"));
    }

    @Test
    @DisplayName("Three supplied phases cycle correctly")
    void Three_supplied_phases_cycle_correctly() {
      List<List<String>> phases =
          List.of(List.of("sg-north"), List.of("sg-south"), List.of("sg-east", "sg-west"));

      BasicController controller = new BasicController(1, 3, phases);
      controller.initialize(twoPhaseLayout());

      int step = 1;

      controller.decide(state(step++, waiting(0, 0, 10, 10)));
      List<SignalCommand> cmds = controller.decide(state(step++, waiting(0, 0, 10, 10)));
      assertEquals(SignalState.GREEN, stateOf(cmds, "sg-east"));
      assertEquals(SignalState.GREEN, stateOf(cmds, "sg-west"));
      assertEquals(SignalState.RED, stateOf(cmds, "sg-north"));
      assertEquals(SignalState.RED, stateOf(cmds, "sg-south"));

      cmds = controller.decide(state(step++, waiting(10, 0, 0, 0)));
      assertEquals(SignalState.YELLOW, stateOf(cmds, "sg-east"));

      controller.decide(state(step++, waiting(10, 0, 0, 0)));
      cmds = controller.decide(state(step++, waiting(10, 0, 0, 0)));
      assertEquals(SignalState.RED_YELLOW, stateOf(cmds, "sg-north"));
      assertEquals(SignalState.RED, stateOf(cmds, "sg-south"));
      assertEquals(SignalState.RED, stateOf(cmds, "sg-east"));
    }

    @Test
    @DisplayName("MaxGreen round-robin respects supplied phase order")
    void MaxGreen_round_robin_respects_supplied_phase_order() {
      List<List<String>> phases =
          List.of(List.of("sg-north", "sg-south"), List.of("sg-east", "sg-west"));

      BasicController controller = new BasicController(2, 3, phases);
      controller.initialize(twoPhaseLayout());

      int step = 1;

      controller.decide(state(step++, waiting(10, 10, 0, 0)));
      controller.decide(state(step++, waiting(10, 10, 0, 0)));

      controller.decide(state(step++, waiting(10, 10, 0, 0)));
      controller.decide(state(step++, waiting(10, 10, 0, 0)));

      List<SignalCommand> cmds = controller.decide(state(step++, waiting(10, 10, 0, 0)));
      assertEquals(SignalState.YELLOW, stateOf(cmds, "sg-north"));

      controller.decide(state(step++, waiting(10, 10, 0, 0)));
      cmds = controller.decide(state(step, waiting(10, 10, 0, 0)));
      assertEquals(SignalState.RED_YELLOW, stateOf(cmds, "sg-east"));
      assertEquals(SignalState.RED_YELLOW, stateOf(cmds, "sg-west"));
    }
  }
}

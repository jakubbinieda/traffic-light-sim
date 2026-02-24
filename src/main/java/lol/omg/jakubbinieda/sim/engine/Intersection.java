package lol.omg.jakubbinieda.sim.engine;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lol.omg.jakubbinieda.sim.controller.Controller;
import lol.omg.jakubbinieda.sim.engine.loadbalancer.LoadBalancer;
import lol.omg.jakubbinieda.sim.geometry.IntersectionLayout;
import lol.omg.jakubbinieda.sim.geometry.Lane;
import lol.omg.jakubbinieda.sim.model.Direction;
import lol.omg.jakubbinieda.sim.model.Movement;
import lol.omg.jakubbinieda.sim.model.TurnType;
import lol.omg.jakubbinieda.sim.model.Vehicle;
import lol.omg.jakubbinieda.sim.model.Vehicle.State;
import lol.omg.jakubbinieda.sim.signal.SignalCommand;
import lol.omg.jakubbinieda.sim.signal.SignalGroup;
import lol.omg.jakubbinieda.sim.signal.SignalState;

public class Intersection {
  private final IntersectionLayout layout;
  private final Controller controller;
  private final LoadBalancer loadBalancer;

  private final Map<String, Vehicle> allVehicles;
  private final Map<Lane, Deque<Vehicle>> laneQueues;
  private final Map<String, SignalState> signalStates;
  private final Set<Vehicle> vehiclesOnIntersection;

  private int stepCount;

  public Intersection(IntersectionLayout layout, Controller controller, LoadBalancer loadBalancer) {
    this.layout = Objects.requireNonNull(layout, "layout cannot be null");
    this.controller = Objects.requireNonNull(controller, "controller cannot be null");
    this.loadBalancer = Objects.requireNonNull(loadBalancer, "loadBalancer cannot be null");

    this.allVehicles = new LinkedHashMap<>();
    this.laneQueues = new LinkedHashMap<>();
    this.signalStates = new LinkedHashMap<>();
    this.vehiclesOnIntersection = new LinkedHashSet<>();
    this.stepCount = 0;

    for (SignalGroup sg : layout.getSignalGroups()) {
      signalStates.put(sg.id(), SignalState.RED);
    }
  }

  public void addVehicle(Vehicle vehicle) {
    Objects.requireNonNull(vehicle, "vehicle cannot be null");

    if (allVehicles.containsKey(vehicle.getId())) {
      throw new IllegalArgumentException("Vehicle with ID " + vehicle.getId() + " already exists");
    }

    if (layout.getLanesFor(vehicle.getMovement()).isEmpty()) {
      throw new IllegalArgumentException(
          "No lanes available for movement " + vehicle.getMovement());
    }

    Lane assignedLane = loadBalancer.selectLane(layout.getLanesFor(vehicle.getMovement()));
    laneQueues.computeIfAbsent(assignedLane, lane -> new ArrayDeque<>()).add(vehicle);
    allVehicles.put(vehicle.getId(), vehicle);
  }

  public StepResult step() {
    stepCount++;

    IntersectionState state = getCurrentIntersectionState();
    List<SignalCommand> commands = controller.decide(state);

    applyCommands(commands);

    List<Vehicle> vehiclesExited = exitVehicles();

    enterVehicles();

    for (Vehicle vehicle : allVehicles.values()) {
      if (vehicle.getState() == State.QUEUED) {
        vehicle.incrementWaitTime();
      }
    }

    return new StepResult(
        stepCount,
        Map.copyOf(signalStates),
        vehiclesExited.stream().map(Vehicle::getId).toList(),
        laneQueues.entrySet().stream()
            .collect(toMap(e -> e.getKey().id(), e -> e.getValue().size())));
  }

  private void enterVehicles() {
    Set<Movement> enteringThisStep = new HashSet<>();

    List<Map.Entry<Lane, Vehicle>> candidates = new ArrayList<>();

    for (var entry : laneQueues.entrySet()) {
      Deque<Vehicle> queue = entry.getValue();
      if (queue.isEmpty()) {
        continue;
      }

      Vehicle vehicle = queue.peek();
      Movement movement = vehicle.getMovement();

      for (SignalGroup signalGroup : layout.getSignalGroups()) {
        SignalState state = signalStates.get(signalGroup.id());
        if ((state == SignalState.GREEN || state == SignalState.GREEN_ARROW)
            && signalGroup.movements().contains(movement)) {
          candidates.add(Map.entry(entry.getKey(), vehicle));
          break;
        }
      }
    }

    candidates.sort(
        Comparator.comparingInt(e -> e.getValue().getMovement().getTurnType().ordinal()));

    for (var candidate : candidates) {
      Vehicle vehicle = candidate.getValue();
      Movement movement = vehicle.getMovement();

      if (canEnter(movement, enteringThisStep)) {
        laneQueues.get(candidate.getKey()).poll();
        vehicle.startCrossing(1);
        vehiclesOnIntersection.add(vehicle);
        enteringThisStep.add(movement);
      }
    }
  }

  private boolean canEnter(Movement movement, Set<Movement> enteringThisStep) {

    for (Movement entering : enteringThisStep) {
      if (conflicts(movement, entering)) {
        return false;
      }
    }

    return true;
  }

  private boolean conflicts(Movement a, Movement b) {
    if (a.from() == b.from()) {
      return false;
    }

    if (a.from().equals(b.from().opposite())) {
      return opposingConflict(a.getTurnType(), b.getTurnType());
    }

    return perpendicularConflict(a.getTurnType(), b.getTurnType());
  }

  private boolean opposingConflict(TurnType a, TurnType b) {
    return (a == TurnType.LEFT) != (b == TurnType.LEFT);
  }

  private boolean perpendicularConflict(TurnType a, TurnType b) {
    return !(a == TurnType.RIGHT && b == TurnType.RIGHT);
  }

  private List<Vehicle> exitVehicles() {
    List<Vehicle> exited = new ArrayList<>(vehiclesOnIntersection);
    exited.forEach(Vehicle::tickCrossing);
    vehiclesOnIntersection.clear();
    return exited;
  }

  private void applyCommands(List<SignalCommand> commands) {
    if (commands.isEmpty()) {
      return;
    }

    Set<String> knownIds = layout.getSignalGroups().stream().map(SignalGroup::id).collect(toSet());

    for (SignalCommand command : commands) {
      if (knownIds.contains(command.signalGroupId())) {
        signalStates.put(command.signalGroupId(), command.newState());
      }
    }
  }

  private IntersectionState getCurrentIntersectionState() {
    Map<Direction, Integer> waitingPerRoad = new EnumMap<>(Direction.class);

    for (Direction direction : layout.getApproachDirections()) {
      int total = 0;
      for (Lane lane : layout.getRoad(direction).lanes()) {
        Deque<Vehicle> queue = laneQueues.get(lane);
        if (queue != null) {
          total += queue.size();
        }
      }
      waitingPerRoad.put(direction, total);
    }

    Set<Movement> vehiclesOnIntersection =
        allVehicles.values().stream()
            .filter(vehicle -> vehicle.getState() == State.CROSSING)
            .map(Vehicle::getMovement)
            .collect(toSet());

    return new IntersectionState(
        stepCount,
        signalStates,
        laneQueues.entrySet().stream()
            .collect(toMap(entry -> entry.getKey().id(), entry -> entry.getValue().size())),
        waitingPerRoad,
        vehiclesOnIntersection);
  }
}

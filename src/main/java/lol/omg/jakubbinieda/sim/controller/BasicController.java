package lol.omg.jakubbinieda.sim.controller;

import java.util.*;
import lol.omg.jakubbinieda.sim.engine.IntersectionState;
import lol.omg.jakubbinieda.sim.geometry.IntersectionLayout;
import lol.omg.jakubbinieda.sim.model.Direction;
import lol.omg.jakubbinieda.sim.model.Movement;
import lol.omg.jakubbinieda.sim.model.TurnType;
import lol.omg.jakubbinieda.sim.signal.SignalCommand;
import lol.omg.jakubbinieda.sim.signal.SignalGroup;
import lol.omg.jakubbinieda.sim.signal.SignalState;

public class BasicController implements Controller {
  private final int minGreen;
  private final int maxGreen;
  private final List<List<String>> phaseDefinitions;

  private List<Phase> phases;
  private List<String> allGroupIds;
  private int activePhase;
  private int nextPhase;
  private Stage stage;
  private int greenStartStep;

  public BasicController(int minGreen, int maxGreen, List<List<String>> phaseDefinitions) {
    if (minGreen < 1) {
      throw new IllegalArgumentException("minGreen must be positive");
    }
    if (maxGreen < minGreen) {
      throw new IllegalArgumentException("maxGreen must be >= minGreen");
    }

    this.minGreen = minGreen;
    this.maxGreen = maxGreen;
    this.phaseDefinitions = phaseDefinitions;
  }

  public BasicController(int minGreen, int maxGreen) {
    this(minGreen, maxGreen, null);
  }

  @Override
  public void initialize(IntersectionLayout layout) {
    Objects.requireNonNull(layout, "layout cannot be null");

    List<SignalGroup> groups = layout.getSignalGroups();
    allGroupIds = groups.stream().map(SignalGroup::id).toList();

    if (phaseDefinitions != null) {
      phases = resolvePhases(phaseDefinitions, groups);
    } else {
      phases = computePhases(groups);
    }

    activePhase = -1;
    nextPhase = -1;
    stage = Stage.INITIAL;
  }

  @Override
  public List<SignalCommand> decide(IntersectionState state) {
    if (phases == null) {
      throw new IllegalStateException("Controller not initialized");
    }

    return switch (stage) {
      case INITIAL -> handleInitial(state);
      case GREEN -> handleGreen(state);
      case YELLOW -> handleYellow();
      case ALL_RED -> handleAllRed();
      case RED_YELLOW -> handleRedYellow(state);
    };
  }

  private List<Phase> resolvePhases(List<List<String>> definitions, List<SignalGroup> groups) {
    Map<String, SignalGroup> groupMap = new HashMap<>();
    for (SignalGroup g : groups) {
      groupMap.put(g.id(), g);
    }

    List<Phase> result = new ArrayList<>();
    for (List<String> def : definitions) {
      Set<Direction> directions = new HashSet<>();
      for (String id : def) {
        SignalGroup g = groupMap.get(id);
        if (g == null) {
          throw new IllegalArgumentException("Unknown signal group: " + id);
        }
        g.movements().forEach(m -> directions.add(m.from()));
      }
      result.add(new Phase(List.copyOf(def), directions));
    }
    return result;
  }

  private List<SignalCommand> handleInitial(IntersectionState state) {
    nextPhase = selectBestPhase(state, -1);
    stage = Stage.RED_YELLOW;
    return commandsForAll(phaseOverrides(nextPhase, SignalState.RED_YELLOW));
  }

  private List<SignalCommand> handleGreen(IntersectionState state) {
    int elapsed = state.step() - greenStartStep;

    if (elapsed < minGreen || phases.size() <= 1) {
      return List.of();
    }

    int bestPhase = selectBestPhase(state, activePhase);
    int currentScore = scorePhase(phases.get(activePhase), state);
    int bestScore = scorePhase(phases.get(bestPhase), state);

    boolean demandElsewhere = bestScore > currentScore;
    boolean maxReached = elapsed >= maxGreen;

    if (!demandElsewhere && !maxReached) {
      return List.of();
    }

    if (maxReached && !demandElsewhere) {
      nextPhase = (activePhase + 1) % phases.size();
    } else {
      nextPhase = bestPhase;
    }

    stage = Stage.YELLOW;
    return commandsForAll(phaseOverrides(activePhase, SignalState.YELLOW));
  }

  private List<SignalCommand> handleYellow() {
    stage = Stage.ALL_RED;
    return commandsForAll(Map.of());
  }

  private List<SignalCommand> handleAllRed() {
    stage = Stage.RED_YELLOW;
    return commandsForAll(phaseOverrides(nextPhase, SignalState.RED_YELLOW));
  }

  private List<SignalCommand> handleRedYellow(IntersectionState state) {
    activePhase = nextPhase;
    greenStartStep = state.step();
    stage = Stage.GREEN;
    return commandsForAll(phaseOverrides(activePhase, SignalState.GREEN));
  }

  private List<Phase> computePhases(List<SignalGroup> groups) {
    List<Phase> result = new ArrayList<>();
    Set<Integer> assigned = new HashSet<>();

    while (assigned.size() < groups.size()) {
      List<String> phaseGroupIds = new ArrayList<>();
      List<SignalGroup> phaseGroups = new ArrayList<>();
      Set<Direction> phaseDirections = new HashSet<>();

      for (int i = 0; i < groups.size(); i++) {
        if (assigned.contains(i)) {
          continue;
        }

        SignalGroup candidate = groups.get(i);
        boolean compatible =
            phaseGroups.stream().noneMatch(existing -> groupsConflict(candidate, existing));

        if (compatible) {
          assigned.add(i);
          phaseGroupIds.add(candidate.id());
          phaseGroups.add(candidate);
          candidate.movements().forEach(m -> phaseDirections.add(m.from()));
        }
      }

      result.add(new Phase(phaseGroupIds, phaseDirections));
    }

    return result;
  }

  private boolean groupsConflict(SignalGroup a, SignalGroup b) {
    for (Movement ma : a.movements()) {
      for (Movement mb : b.movements()) {
        if (movementsConflict(ma, mb)) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean movementsConflict(Movement a, Movement b) {
    if (a.from() == b.from()) {
      return false;
    }

    if (a.from() == b.from().opposite()) {
      return (a.getTurnType() == TurnType.LEFT) != (b.getTurnType() == TurnType.LEFT);
    }

    return !(a.getTurnType() == TurnType.RIGHT && b.getTurnType() == TurnType.RIGHT);
  }

  private int selectBestPhase(IntersectionState state, int excludePhase) {
    int bestIndex = 0;
    int bestScore = -1;
    for (int i = 0; i < phases.size(); i++) {
      if (i == excludePhase) continue;
      int score = scorePhase(phases.get(i), state);
      if (score > bestScore) {
        bestScore = score;
        bestIndex = i;
      }
    }
    return bestIndex;
  }

  private int scorePhase(Phase phase, IntersectionState state) {
    return phase.approachDirections().stream()
        .mapToInt(d -> state.waitingPerRoad().getOrDefault(d, 0))
        .sum();
  }

  private Map<String, SignalState> phaseOverrides(int phaseIndex, SignalState state) {
    Map<String, SignalState> overrides = new HashMap<>();
    for (String id : phases.get(phaseIndex).signalGroupIds()) {
      overrides.put(id, state);
    }
    return overrides;
  }

  private List<SignalCommand> commandsForAll(Map<String, SignalState> overrides) {
    List<SignalCommand> commands = new ArrayList<>();
    for (String id : allGroupIds) {
      commands.add(new SignalCommand(id, overrides.getOrDefault(id, SignalState.RED)));
    }
    return commands;
  }

  private enum Stage {
    INITIAL,
    GREEN,
    YELLOW,
    ALL_RED,
    RED_YELLOW
  }

  private record Phase(List<String> signalGroupIds, Set<Direction> approachDirections) {}
}

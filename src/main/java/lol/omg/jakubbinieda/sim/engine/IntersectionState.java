package lol.omg.jakubbinieda.sim.engine;

import java.util.Map;
import java.util.Set;
import lol.omg.jakubbinieda.sim.model.Direction;
import lol.omg.jakubbinieda.sim.model.Movement;
import lol.omg.jakubbinieda.sim.signal.SignalState;

public record IntersectionState(
    int step,
    Map<String, SignalState> signalStates,
    Map<String, Integer> queueLengths,
    Map<Direction, Integer> waitingPerRoad,
    Set<Movement> vehiclesOnIntersection) {}

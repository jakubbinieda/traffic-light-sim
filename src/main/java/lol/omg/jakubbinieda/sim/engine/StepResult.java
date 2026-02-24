package lol.omg.jakubbinieda.sim.engine;

import java.util.List;
import java.util.Map;
import lol.omg.jakubbinieda.sim.signal.SignalState;

public record StepResult(
    int step,
    Map<String, SignalState> signalStates,
    List<String> leftIntersection,
    Map<String, Integer> queueLengths) {}

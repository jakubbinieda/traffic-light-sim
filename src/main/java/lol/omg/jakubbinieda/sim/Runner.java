package lol.omg.jakubbinieda.sim;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import lol.omg.jakubbinieda.sim.controller.BasicController;
import lol.omg.jakubbinieda.sim.controller.Controller;
import lol.omg.jakubbinieda.sim.engine.Intersection;
import lol.omg.jakubbinieda.sim.engine.StepResult;
import lol.omg.jakubbinieda.sim.engine.loadbalancer.LoadBalancer;
import lol.omg.jakubbinieda.sim.engine.loadbalancer.RandomBalancer;
import lol.omg.jakubbinieda.sim.factories.SimpleIntersectionFactory;
import lol.omg.jakubbinieda.sim.io.JsonParser;
import lol.omg.jakubbinieda.sim.io.SimulationInput;
import lol.omg.jakubbinieda.sim.io.SimulationOutput;
import lol.omg.jakubbinieda.sim.io.StepStatus;
import lol.omg.jakubbinieda.sim.io.commands.AddVehicleCommand;
import lol.omg.jakubbinieda.sim.io.commands.Command;
import lol.omg.jakubbinieda.sim.io.commands.StepCommand;
import lol.omg.jakubbinieda.sim.model.Vehicle;

public final class Runner {
  private Runner() {}

  public static void main(String[] args) {
    if (args.length != 2) {
      System.err.println("Invalid number of arguments");
      return;
    }

    Path inputPath = Path.of(args[0]);
    Path outputPath = Path.of(args[1]);

    SimulationInput input;
    try {
      input = JsonParser.getInstance().readInput(inputPath);
    } catch (Exception e) {
      System.err.println("Error reading input file: " + e.getMessage());
      return;
    }

    List<StepStatus> results = new ArrayList<>();

    LoadBalancer loadBalancer = new RandomBalancer();

    List<List<String>> phases =
        List.of(List.of("sg-north", "sg-south"), List.of("sg-east", "sg-west"));
    Controller controller = new BasicController(3, 5, phases);
    Intersection intersection =
        SimpleIntersectionFactory.getInstance().supply(controller, loadBalancer);

    for (Command cmd : input.commands()) {
      switch (cmd) {
        case StepCommand ignore -> {
          StepResult result = intersection.step();
          results.add(new StepStatus(result.leftIntersection()));
        }
        case AddVehicleCommand addVehicleCmd -> {
          intersection.addVehicle(new Vehicle(addVehicleCmd.vehicleId(), addVehicleCmd.movement()));
        }
      }
    }

    try {
      JsonParser.getInstance().writeOutput(outputPath, new SimulationOutput(results));
    } catch (Exception e) {
      System.err.println("Error writing output file: " + e.getMessage());
    }
  }
}

package lol.omg.jakubbinieda.sim.io.commands;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lol.omg.jakubbinieda.sim.model.Direction;
import lol.omg.jakubbinieda.sim.model.Movement;

public record AddVehicleCommand(String vehicleId, Movement movement) implements Command {
  @JsonCreator
  public AddVehicleCommand(
      @JsonProperty("vehicleId") String vehicleId,
      @JsonProperty("startRoad") String startRoad,
      @JsonProperty("endRoad") String endRoad) {
    this(
        vehicleId,
        new Movement(
            Direction.valueOf(startRoad.toUpperCase()), Direction.valueOf(endRoad.toUpperCase())));
  }
}

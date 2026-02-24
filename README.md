# Smart Traffic Light Simulator

![CI](../../actions/workflows/ci.yml/badge.svg)

This project is a simple but extensible traffic light simulator built using Java.

## Quick Start

```bash
./gradlew shadowJar
java -jar build/libs/traffic-light-sim-all.jar input.json output.json
```

## Input Format (input.json)

```json
{
  "commands": [
    {
      "type": "addVehicle",
      "vehicleId": "vehicle1",
      "startRoad": "south",
      "endRoad": "north"
    },
    {
      "type": "addVehicle",
      "vehicleId": "vehicle2",
      "startRoad": "north",
      "endRoad": "south"
    },
    {
      "type": "step"
    },
    {
      "type": "step"
    },
    {
      "type": "step"
    }
  ]
}
```

- **addVehicle** - queues a vehicle on `startRoad` with the intention of eventually reaching
  `endRoad`. For now, valid roads are `north`, `south`, `east`, and `west`.
- **step** - advances the simulation by one time step.

## Output Format (output.json)

```json
{
  "stepStatuses": [
    {
      "leftVehicles": []
    },
    {
      "leftVehicles": []
    },
    {
      "leftVehicles": [
        "vehicle1",
        "vehicle2"
      ]
    }
  ]
}
```

Each entry in `stepStatuses` corresponds to a `step` command in the input. The `leftVehicles` array
lists the IDs of vehicles that successfully left the intersection during that step.

## How it works

### IntersectionLayout

The `IntersectionLayout` class defines the layout of the intersection, including the roads and
signal groups that describe which movements are allowed when a particular signal group is green.

### Intersection

The `Intersection` class manages the state of the intersection, including adding vehicles and
advancing the simulation.

### Controller

Classes implementing the `Controller` interface are responsible for controlling the traffic signals.
The `BasicController` is a simple implementation that changes the lights according to the queue
lengths.

### LoadBalancer

Classes implementing the `LoadBalancer` interface are responsible for determining which vehicles
should go on which lane. The `RandomLoadBalancer` is a simple implementation that randomly assigns
vehicles to lanes.

### JsonParser

The `JsonParser` class is responsible for parsing the input JSON file and generating the output
JSON.

### SimpleIntersectionFactory

The `SimpleIntersectionFactory` class is a factory that creates a 4-way one lane per road
intersection with a basic controller and random load balancer.

## Architecture

```
lol.omg.jakubbinieda.sim
├── controller/             Traffic controllers
├── engine/                 Simulation's brain   
│   ├── loadbalancer/       Load balancers
├── factories/              Factories
├── geometry/               Intersection's geometry
├── io/                     JSON parsing and generation
├── model/                  Data models for vehicles, roads, etc.
├── signal/                 Traffic signal classes
└── Runner.java             CLI entry point
```

## Extensibility and design decisions

- **Movement as a core abstraction** - every vehicle performs one movement, every signal controls a
  set of movements, every conflict is between movements.
- **Controller and LoadBalancer are a pure functions of state** - their decisions are based solely
  on the current state of the intersection, making them
  easier to test and extend with different control and balancing algorithms.
- **Factory pattern for intersection creation** - allows for easy creation of different types of
  intersections with varying layouts, controllers, and load balancers.
- **Conflict resolution at vehicle level** - even if two signal groups are green, a vehicle will
  only proceed if its movement does not conflict with any other
  vehicle's movement, ensuring safety and realism in the simulation.

## Building & Testing

```bash
# Build
./gradlew build

# Clean code formatting
./gradlew spotlessCheck

# Run tests
./gradlew test

# Generate code coverage report
./gradlew jacocoTestReport

# Run mutation tests
./gradlew pitest

# Generate JAR
./gradlew shadowJar

# Run
java -jar  build/libs/traffic-light-sim-all.jar
```

## Full walkthrough of a simple simulation

### input.json:

*(if you want to use this, change the comment into steps)*

```
{
  "commands": [
    {
      "type": "addVehicle",
      "vehicleId": "vehicle1",
      "startRoad": "south",
      "endRoad": "north"
    },
    {
      "type": "addVehicle",
      "vehicleId": "vehicle2",
      "startRoad": "north",
      "endRoad": "south"
    },
    {
      "type": "step"
    },
    {
      "type": "step"
    },
    {
      "type": "addVehicle",
      "vehicleId": "vehicle3",
      "startRoad": "west",
      "endRoad": "south"
    },
    {
      "type": "addVehicle",
      "vehicleId": "vehicle4",
      "startRoad": "west",
      "endRoad": "south"
    },
    {
      "type": "step"
    },
    
    ... six more steps

    {
      "type": "step"
    }
  ]
}
```

### output.json:

```
{
  "stepStatuses": [
    {
      "leftVehicles": [] <- NS RED_YELLOW
    },
    {
      "leftVehicles": [] <- NS GREEN, vehicle1 and vehicle2 enter
    },
    {
      "leftVehicles": [
        "vehicle1",
        "vehicle2"
      ] <- both vehicles leave
    },
    {
      "leftVehicles": [] <- NS still needs one more step to reach minimum green time
    },
    {
      "leftVehicles": [] <- NS YELLOW
    },
    {
      "leftVehicles": [] <- NS and EW RED, safety buffer
    },
    {
      "leftVehicles": [] <- EW RED_YELLOW
    },
    {
      "leftVehicles": [] <- vehicle3 enters
    },
    {
      "leftVehicles": [
        "vehicle3"
      ] <- vehicle3 exits, vehicle4 enters
    },
    {
      "leftVehicles": [
        "vehicle4"
      ] <- vehicle4 exits
    }
  ]
}
```

## Tests and Code Coverage

### Unit tests

Latest
report: [Unit test report](../../actions/runs/22367861855/artifacts/5641955057)

- Total tests: **239**
- Failures: **0**
- Skipped: **0**
- Success rate: **100%**

### Mutation testing (PiTest)

Latest
report: [PiTest report](../../actions/runs/22367861855/artifacts/5641955471)

- Line coverage: **422/422**
- Mutation coverage: **167/167**
- Test Strength: **167/167**

### Coverage (JaCoCo)

Latest
report: [JaCoCo report](../../actions/runs/22367861855/artifacts/5641955268)

- Missed instructions: **0/2237**
- Missed branches: **0/197**

## TODO

- Add more complex intersections
- Add more complex controllers and load balancers
- Add support for pedestrians and cyclists
- Add support for different vehicle types (e.g. trucks, buses)


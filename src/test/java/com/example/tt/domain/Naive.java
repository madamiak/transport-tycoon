package com.example.tt.domain;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

public class Naive {

    @Test
    public void returns5() {
        int totalDuration = 0;

        // init state
        String factory = "F";
        String port = "P";
        String warehouseA = "A";
        String warehouseB = "B";
        String onRoad = "R";

        List<Object[]> cargos = new ArrayList<>();
        // destination, availableToLoad, delivered
        Object[] cargo1 = {warehouseA, true, false, "C1"};
        Object[] cargo2 = {warehouseB, true, false, "C2"};
        cargos.add(cargo1);
        cargos.add(cargo2);

        List<Object[]> shipmentUnits = new ArrayList<>();
        // id, home, currentLocation, destination, cargo, timeLeft
        shipmentUnits.add(new Object[]{"T1", factory, factory, factory, null, 0});
        shipmentUnits.add(new Object[]{"T2", factory, factory, factory, null, 0});
        shipmentUnits.add(new Object[]{"S1", port, port, port, null, 0});

        List<Object[]> legs = new ArrayList<>();
        // loc1, loc2, duration
        legs.add(new Object[]{factory, warehouseA, 5});
        legs.add(new Object[]{factory, port, 1});
        legs.add(new Object[]{port, warehouseB, 4});

        while (true) {
            // elapse time for shipment units
            List<Object[]> shipmentUnitsOnRoad = shipmentUnits.stream()
                    .filter(shipmentUnit -> shipmentUnit[2].equals(onRoad))
                    .collect(toList());
            for (Object[] shipmentUnit : shipmentUnitsOnRoad) {
                shipmentUnits = shipmentUnits.stream()
                        .map(e -> {
                            if (e[0].equals(shipmentUnit[0])) { // if id same
                                int timeLeft = (int) e[5];
                                return new Object[]{e[0], e[1], e[2], e[3], e[4], --timeLeft}; // emulate passed time for shipments on road
                            }
                            return e;
                        })
                        .collect(toList());
            }

            // check if destination reached
            List<Object[]> shipmentUnitsAtDestination = shipmentUnits.stream()
                    .filter(shipmentUnit -> shipmentUnit[2].equals(onRoad) && shipmentUnit[5].equals(0))
                    .collect(toList());
            for (Object[] shipmentUnit : shipmentUnitsAtDestination) {
                shipmentUnits = shipmentUnits.stream()
                        .map(e -> {
                            if (e[0].equals(shipmentUnit[0])) { // if id same
                                int timeLeft = 5;// TODO: port and B
                                return new Object[]{e[0], e[1], onRoad, e[1], null, timeLeft}; // go back home
                            }
                            return e;
                        })
                        .collect(toList());
                cargos = cargos.stream() // set cargo as not available to load
                        .map(e -> {
                            if (e[3].equals(((Object[]) shipmentUnit[4])[3])) {
                                return new Object[]{e[0], false, true, e[3]};
                            }
                            return e;
                        })
                        .collect(toList());
            }

            // load cargo on available shipment units
            List<Object[]> shipmentUnitsAtFactory = shipmentUnits.stream()
                    .filter(shipmentUnit -> shipmentUnit[2].equals(factory))
                    .collect(toList());
            List<Object[]> cargosAvailableToLoad = cargos.stream()
                    .filter(cargo -> cargo[1].equals(true))
                    .collect(toList());
            for (Object[] cargo : cargosAvailableToLoad) {
                for (Object[] shipmentUnit : shipmentUnitsAtFactory) {
                    int timeLeft = 5;// TODO: port and B
                    shipmentUnits = shipmentUnits.stream() // load cargo to shipment unit
                            .map(e -> {
                                if (e[0].equals(shipmentUnit[0])) { // if id same
                                    return new Object[]{e[0], e[1], onRoad, cargo[0], cargo, timeLeft};
                                }
                                return e;
                            })
                            .collect(toList());
                    cargos = cargos.stream() // set cargo as not available to load
                            .map(e -> {
                                if (e[3].equals(cargo[3])) {
                                    return new Object[]{e[0], false, e[2], e[3]};
                                }
                                return e;
                            })
                            .collect(toList());
                    break;
                }
            }

            // check all cargo delivered
            if (cargos.stream().allMatch(e -> e[2].equals(true))) {
                break;
            }

            // increase time
            totalDuration++;
        }
        assertThat(totalDuration).isEqualTo(5);
    }
}

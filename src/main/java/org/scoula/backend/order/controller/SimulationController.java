package org.scoula.backend.order.controller;

import org.scoula.backend.order.service.simulator.OrderSimulationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

// 테스트용 컨트롤러
@RestController
@RequestMapping("/api/simulation")
@RequiredArgsConstructor
public class SimulationController {

	private final OrderSimulationService orderSimulationService;

	@PostMapping("single/start")
	public ResponseEntity<String> startSingleSimulation() {
		orderSimulationService.startSingleSimulation();
		return ResponseEntity.ok("Single simulation started");
	}

	@PostMapping("single/stop")
	public ResponseEntity<String> stopSingleSimulation() {
		orderSimulationService.stopSingleSimulation();
		return ResponseEntity.ok("Single simulation stopped");
	}

	@PostMapping("multi/start")
	public ResponseEntity<String> startMultiSimulation() {
		orderSimulationService.startMultiSimulation();
		return ResponseEntity.ok("Multi simulation started");
	}

	// @PostMapping("multi/stop")
	// public ResponseEntity<String> stopMultiSimulation() {
	// 	orderSimulationService.stopSingleSimulation();
	// 	return ResponseEntity.ok("Multi simulation stopped");
	// }
}

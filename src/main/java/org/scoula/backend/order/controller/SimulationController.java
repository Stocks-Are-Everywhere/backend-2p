package org.scoula.backend.order.controller;

import org.scoula.backend.order.service.SingleOrderSimulator;
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

	// private final MultiOrderSimulator multiOrderSimulator;
	private final SingleOrderSimulator singleOrderSimulator;

	@PostMapping("/start")
	public ResponseEntity<String> startSimulation() {
		singleOrderSimulator.startSimulation();
		return ResponseEntity.ok("Simulation started");
	}

	@PostMapping("/stop")
	public ResponseEntity<String> stopSimulation() {
		singleOrderSimulator.stopSimulation();
		return ResponseEntity.ok("Simulation stopped");
	}
}

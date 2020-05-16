package com.adapter.cppadapter.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.adapter.cppadapter.service.Query;

@CrossOrigin
@org.springframework.web.bind.annotation.RestController
@RequestMapping
public class RestController {

	@Autowired
	Query query;
	
	@GetMapping(path = "/startJob")
	public String startJob() {
		query.query();
		return "Job Started. Check logs";
	}

}
package org.aperteworkflow.webapi.main.processes.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller(value = "BaseWidgetsController")  
@RequestMapping("VIEW")  
public class BaseWidgetsController
{
	 public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) 
	 {
		 ModelAndView mvc = new ModelAndView("test2");
		 return mvc;
	 }
}

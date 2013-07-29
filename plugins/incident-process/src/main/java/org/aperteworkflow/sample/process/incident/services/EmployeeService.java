package org.aperteworkflow.sample.process.incident.services;

import java.util.List;

import org.aperteworkflow.sample.process.incident.model.Employee;

public interface EmployeeService {
	List<Employee> getAvailableEmployees(String issueType, String incidentType);
}

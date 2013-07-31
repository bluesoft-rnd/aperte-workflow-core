package org.aperteworkflow.sample.process.incident.services;

import java.util.ArrayList;
import java.util.List;
import org.aperteworkflow.sample.process.incident.model.Employee;

public class EmployeeServiceMock implements EmployeeService {

	private static final String[][] employeeData = new String[][] {
		{"1","Adam","Hardware","123","adam.hardware@it.support"},
		{"2","Bernie","Software","456","bernie.software@it.support"},
		{"3","Cindy","Mobile","789","cindy.mobile@it.support"},
		{"4","Donnie","Allrounder","112","donnie.allrounder@it.support"},
		{"5","Eve","Purchase","113","eve.purchase@it.support"},
		{"6","Frederic","Hardware","123","frederic.hardware@it.support"},
		{"7","Gustav","Software","456","gustav.software@it.support"},
		{"8","Holy","Mobile","456","holy.software@it.support"}
	};

	public List<Employee> getAvailableEmployees(String issueType, String incidentType) {
		List<Employee> result = new ArrayList<Employee>();

		if (issueType!=null) {
			issueType = issueType.trim().toLowerCase();
			
			if ("incident".equals(issueType)) {
				if (incidentType!=null) {
					incidentType = incidentType.trim().toLowerCase();
					if ("hardware".equals(incidentType)) {
						result.add(new Employee(employeeData[0]));
						result.add(new Employee(employeeData[5]));
					} else if ("software".equals(incidentType)) {
						result.add(new Employee(employeeData[1]));
						result.add(new Employee(employeeData[6]));
					} else if ("mobile".equals(incidentType)) {
						result.add(new Employee(employeeData[2]));
						result.add(new Employee(employeeData[7]));
					} else if ("other".equals(incidentType)) {
						result.add(new Employee(employeeData[3]));
					}
				}
			} else if ("purchase".equals(issueType)) {
				result.add(new Employee(employeeData[4]));
			} else if ("other".equals(issueType)) {
				result.add(new Employee(employeeData[3]));
			}
		}
		
		
		return result;
	}

}

package com.learn.issuetracker.repository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import com.learn.issuetracker.model.Employee;
import com.learn.issuetracker.model.Issue;

/*
 * This class has methods for parsing the String read from the files in to corresponding Model Objects
*/
public class Utility {
	
	private Utility() {
		//Private Constructor to prevent object creation
	}

	/*
	 * parseEmployee takes a string with employee details as input parameter and parses it in to an Employee Object 
	*/
	
	public static Employee parseEmployee(String employeeDetail) {
		String[] empDetails=employeeDetail.split(",");
		return new Employee(Integer.parseInt(empDetails[0]),empDetails[1],empDetails[2]);
	}

	/*
	 * parseIssue takes a string with issue details and parses it in to an Issue Object. The employee id in the 
	 * Issue details is used to search for an an Employee, using EmployeeRepository class. If the employee is found
	 * then it is set in the Issue object. If Employee is not found, employee is set as null in Issue Object  
	*/
	
	public static Issue parseIssue(String issueDetail) {
		if(null!=issueDetail && !issueDetail.isEmpty()) {
			try {
				
			
			String[] issue = issueDetail.split(",");
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
			int empId = Integer.parseInt(issue[6]);
			Optional<Employee> emp = EmployeeRepository.getEmployee(empId);
			Employee employee=null;
			if(emp.isPresent()) {
				employee=emp.get();
			}
			return new Issue(issue[0],issue[1],LocalDate.parse(issue[2],formatter),
				LocalDate.parse(issue[3],formatter),issue[4],issue[5],employee);	
			}catch(Exception ex) {
				return null;
			}
		}
		return null;
	}
}

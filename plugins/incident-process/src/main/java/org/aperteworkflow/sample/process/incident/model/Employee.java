package org.aperteworkflow.sample.process.incident.model;

public class Employee {
	String id;

	String name;
	String lastname;
	String phone;
	String email;
	
	public Employee() {
	}

	public Employee(String[] data) {
		super();
		if (data!=null && data.length>=5) {
			this.id = data[0];
			this.name = data[1];
			this.lastname = data[2];
			this.phone = data[3];
			this.email = data[4];
		}
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	
}

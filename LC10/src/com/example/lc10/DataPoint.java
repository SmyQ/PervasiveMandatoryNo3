package com.example.lc10;

import java.util.Date;

public class DataPoint {
	public String x;
	public String y;
	public String z;
	public Date d;
		
	public DataPoint(Date d, float x, float y, float z){
		this.x = String.valueOf(x);
		this.y = String.valueOf(y);
		this.z = String.valueOf(z);
		this.d = d;
	}
	
	public String getX(){
		return x;
	}
	
	public String getY(){
		return y;
	}
	
	public String getZ(){
		return z;
	}
	
	public Date getDate(){
		return d;
	}
}

package com.example.lc10;

import java.util.Date;

public class DataPoint {
	public float x;
	public float y;
	public float z;
	public Date d;
		
	public DataPoint(Date d, float x, float y, float z){
		this.x = x;
		this.y = y;
		this.z = z;
		this.d = d;
	}
	
	public float getX(){
		return x;
	}
	
	public float getY(){
		return y;
	}
	
	public float getZ(){
		return z;
	}
	
	public Date getDate(){
		return d;
	}
}

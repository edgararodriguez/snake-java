package org.edgar;

//class time is responsible for tracking the number of cycles that have elapse over time

public class Time {
	
	//the number of milliseconds that make up one cylce 
	private float millisPerCycle;
	
	//the last time the calculator was updated 
	private long lastUpdate;
	
	//the number of cycles that have been elapsed and not been polled 
	private int elapsedCycles;
	
	//Amount of excess time towards the next elapsed cycle
	private float excessCycles;
	
	//whether or not the clock is paused
	private boolean isGamePaused;
	
	//creates new clock and sets it cycle per seconds
	public Time(float cyclesPerSecond) {
		setCyclesPerSecond(cyclesPerSecond);
		reset();
	}
	
	//Sets the number of cycles that elapse per second.
	public void setCyclesPerSecond(float cyclesPerSecond) {
		this.millisPerCycle = (1.0f / cyclesPerSecond) * 1000;
	}
	
	//Resets the clock stats.
	public void reset() {
		this.elapsedCycles = 0;
		this.excessCycles = 0.0f;
		this.lastUpdate = getCurrentTime();
		this.isGamePaused = false;
	}
	//Updates the clock stats.
	public void update() {
		//Get the current time and calculate the delta time.
		long currUpdate = getCurrentTime();
		float delta = (float)(currUpdate - lastUpdate) + excessCycles;
			
		//Update the number of elapsed and excess ticks if we're not paused.
		if(!isGamePaused) {
			this.elapsedCycles += (int)Math.floor(delta / millisPerCycle);
			this.excessCycles = delta % millisPerCycle;
		}
			
		//Set the last update time for the next update cycle.
		this.lastUpdate = currUpdate;
		}
	
	//pauses or unpauses the clock
	public void setPaused(boolean paused) {
		this.isGamePaused = paused;
	}
	
	//checks to see if the clock is paused or not
	public boolean isGamePaused() {
		return isGamePaused;
	}
	
	//Checks to see if a cycle has elapsed for this clock yet.
	public boolean hasElapsedCycle() {
		if(elapsedCycles > 0) {
			this.elapsedCycles--;
			return true;
		}
		return false;
	}
	
	//Checks to see if a cycle has elapsed for this clock yet.
	public boolean peekElapsedCycle() {
		return (elapsedCycles > 0);
	}
	
	//Calculates the current time in milliseconds 
	private static final long getCurrentTime() {
		return (System.nanoTime() / 1000000L);
	}
}
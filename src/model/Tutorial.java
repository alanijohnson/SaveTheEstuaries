package model;

public class Tutorial extends MainModel{
	String mode;
	boolean accumulateTrash = false;
	boolean accumulateFood = true;
	int foodCollect = 10;
	
	public Tutorial() {
		super();
		mode = "collectFood";
		getStuffSet().setAccumulationValue(30);
		//getStuffSet().getFood().clear();
		//getStuffSet().getTrash().clear();
		//setStartingTrash(1);
		//setStartingFood(1);
//		accumulateAll();
//		contactedTrash = false;
//		contactedFood = false;
//		inMiniGame = true;
	}
	
	public void accumulate() {
		// assume not added
		boolean trashAdded = false;
		boolean foodAdded = false;
		
		// get locations (initialize x)
		
		if (accumulateTrash){
		int[] trashLoc = {accumulationDist, 0};
		// add trash first
		while (!trashAdded) {
			trashLoc[1] = randint(0, getMap().getHeight()); 		// random y location
			trashAdded = everyThing.add(trashLoc, "trash");			// try to add
		}
		}
		
		if (accumulateFood){
		int[] foodLoc = {accumulationDist, 0};
		// add the food
		while (!foodAdded) {
			foodLoc[1] = randint(0,getMap().getHeight());			// random y location
			foodAdded = everyThing.add(foodLoc, "food"); 			// try to add
		}
		}
	}
	
	
//	public void accumulateAll() {
//		int[] trashLoc = {2*getMap().getLength()/3,2*getMap().getHeight()/3};
//		int[] foodLoc = {2*getMap().getLength()/3,getMap().getHeight()/3};
//		getStuffSet().add(trashLoc,"trash");
//		getStuffSet().add(foodLoc, "food");
//	}
	
	public void update(int newSpeed, int deltaTheta) {
		//set mode
		if (mode == "collectFood"){
			if (foodCollect == 0){
				mode = "accumulateTrash";
				accumulateTrash = true;
			}
		} else if (mode == "accumulateTrash"){
			
		}
				
		
		if (!getInMiniGame()) { 			 		// in the main game
			
			// setup			getMainCharacter().setSpeed(newSpeed);
			getMainCharacter().setAngle(deltaTheta);
			
			// if move allowed
			//if (getMap().moveMap(getMainCharacter())) {
				getMap().moveMap(getMainCharacter());
				System.out.println("Valid move");
				
				// move everything and fish
				getStuffSet().move(getMainCharacter());
				
				if (newSpeed > minSpeed) {
					getMainCharacter().setSpeed(newSpeed/5000);
					getMainCharacter().move();
				}
			//}
			
			// move not allowed
			/*else {
				System.out.println("Invalid move, not moving");
			}*/
			
			// time to accumulate
			if (getStuffSet().shouldAccumulate()) {
				accumulate();
			}
			
			// display the state
			System.out.println(this);
			
			// collision checking
			String collision = everyThing.whatCollided(fishy);
			System.out.println("Checking collisions");
			System.out.println("Collisions: " + collision);
			
			// collision with trash
			if (collision.equals("trash")) {
				decreaseScore(); 				// lose points
				miniGame = new MiniGame(miniWidth,miniHeight); 		// start minigame
				setInMiniGame(true);
			}
			// collision with food
			else if (collision.equals("food")) {
				increaseScore();// gain points
				remainingTime += foodTime;
				foodCollect -= 1;
			}
			
			// check if game over
			setGameOver(-getMap().getOrigin().getX() >= getMap().getLength());
			if (getGameOver()) {
				setHasWon(true);
				getStuffSet().clearAll();
			}
		}
		// in the minigame
		else {
			// update the mini game
			getMiniGame().update(newSpeed, deltaTheta);
			
			// check if we should still be in the minigame
			setInMiniGame(!getMiniGame().getMiniGameOver());
			
			// minigame is over
			if (!getInMiniGame()) {
				System.out.println("Mini game over");
				getStuffSet().removeAllTrash(); 			// eliminate all trash in the main game
			} 
		}
		// time updating
		timeIncr();
		if (!getGameOver()) {
			setGameOver(getRemainingTime() <= 0);
			if (getGameOver()) {
				setHasWon(false);
			}
		}
	}
	
//	public void setContact(String s) {
//		if (s.equals("trash")) {
//			contactedTrash = true;
//		}else if (s.equals("food")) {
//			contactedFood = true;
//		}
//	}
//	
//	public void setContactedFood(boolean b) {
//		contactedFood = b;
//	}
//	
//	public void setContactedTrash(boolean b) {
//		contactedTrash = b;
//	}
//	
//	public boolean getContactedTrash() {
//		return contactedTrash;
//	}
//	
//	public boolean getContactedFood() {
//		return contactedFood;
//	}

}
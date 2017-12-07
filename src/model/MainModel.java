package model;

import java.util.Random;

/**
 * @author jerome
 * MainModel class, simplified using flyWeight
 */
public class MainModel {

	// Game stuff
	protected MainCharacter fishy;		// main character 	
	protected StuffSet everyThing; 		// all the trash and food
	protected Map map; 					// map
	protected MiniGame miniGame;			// mini game
	
	protected final int minSpeed = 5;
	
	// value for adding trash (distance away)
	// TODO: verify
	protected int accumulationDist = 3000;
	
	// flags for game control
	private boolean gameOver; 			// game still going on
	protected boolean inMiniGame; 		// activate minigame
	private boolean hasWon;				// winning the game
	
	// scoring
	private int playerScore = 0; 		// the player's score
	private int foodScore = 10; 		// change in score from eating food
	private int trashScore = 0; 		// change in score form eating trash
	protected int foodTime = 10;			// time addition from eating food
	
	// timing (all in ms)
	private int tickLength = 30; 						// time period of a single tick
	private double timeMin = 5;
	private int maxAllowedTime = (int) (timeMin*60*1000); 		// maximum allowed time for the game
	protected int remainingTime;							// remaining time
	private int startingTrash = 0;
	private int startingFood = 0;
	protected int miniHeight;
	protected int miniWidth;
	
	protected double trashAccumulateMultiplier = 0.05;
	
	
	// constructors
	
	
	// default
	/**
	 * Constructor
	 */
	public MainModel() {
		setGameOver(false);
		setInMiniGame(false);
		everyThing = new StuffSet();
		map = new Map();
		fishy = new MainCharacter(map);
		remainingTime = maxAllowedTime;

		
	}
	
	public MainModel(int mapL) {
		this();
		map.setLength(mapL);
	}
	
	public static void setup(MainModel m, int mainCharRad, int foodSize, int trashSize, 
			int mapHeight, int mapLength, int mapUnique) {
		
		m.getMainCharacter().setRadius(mainCharRad);
		m.getStuffSet().setFoodSize(foodSize);
		m.getStuffSet().setTrashSize(trashSize);
		m.getMap().setHeight(mapHeight);
		m.getMap().setLength(mapLength);
		m.getMap().setUniqueLength(mapUnique);
		m.setAccumulationDistance(mapUnique);
		m.accumulateAll();
	}
	
	public static void setup(MainModel m, int mainCharRad, int foodSize, int trashSize, 
			int mapHeight, int mapLength, int mapUnique, int miniW, int miniH) {
		
		m.getMainCharacter().setRadius(mainCharRad);
		m.getStuffSet().setFoodSize(foodSize);
		m.getStuffSet().setTrashSize(trashSize);
		m.getMap().setHeight(mapHeight);
		m.getMap().setLength(mapLength);
		m.getMap().setUniqueLength(mapUnique);
	}
	
		
	// map length and height
	/**
	 * Constructor
	 * @param length 		length of the map
	 * @param height		height of the map
	 */
	public MainModel(int length, int height) {
		this();
		map.setHeight(height);
		map.setLength(length);
		
	}
	
	
	// all map parameters
	/**
	 * Constructor
	 * @param length		length of the map
	 * @param height 		height of the map
	 * @param unlen			unique length of the map
	 */
	public MainModel(int length, int height, int unlen) {
		this(length,height);
		map.setUniqueLength(unlen);
		
	}
	
	public void update(){
		
	}
	
	// updating
	/**
	 * Updating model by rotating fish and changing its speed
	 * @param newSpeed 			new speed for the fish
	 * @param deltaTheta		angle to rotate the fish
	 */
	public void update(int newSpeed, int deltaTheta) {
		if (!gameOver){
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
			}
			
			// check if game over
			setGameOver(-getMap().getOrigin().getX() >= getMap().getLength());
			if (getGameOver()) {
				setHasWon(true);
				gameOver();
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
				gameOver();
			}
		}
		}
	}
	
	
	// trash accumulation
	/**
	 * accumulation of trash and food
	 */
	public void accumulate() {
		// assume not added
		
		boolean foodAdded = false;
		
		// get locations (initialize x)
		
		int[] foodLoc = {accumulationDist, 0};
		
		// add trash first
		int trashAmount = everyThing.getTrashSize();
		for (int i=0; i<(1+(trashAmount*trashAccumulateMultiplier)); i++){
			boolean trashAdded = false;
			int[] trashLoc = {accumulationDist, 0};
			while (!trashAdded) {
				trashLoc[1] = randint(0, getMap().getHeight()); 		// random y location
				trashAdded = everyThing.add(trashLoc, "trash");			// try to add
			}
		}
		
		// add the food
		while (!foodAdded) {
			foodLoc[1] = randint(0,getMap().getHeight());			// random y location
			foodAdded = everyThing.add(foodLoc, "food"); 			// try to add
		}
	}
	
	public void accumulateAll() {
		for (int i=0; i<getStartingTrash(); i++) {
			boolean trashAdded = false;
			int[] trashLoc = {0, 0};
			while (!trashAdded) {
				trashLoc[0] = MainModel.randint(75, getMap().getLength());
				trashLoc[1] = MainModel.randint(75, getMap().getHeight());
				trashAdded = getStuffSet().add(trashLoc,"trash");
				//System.out.println("trash"+trashLoc[0]+"/"+getMap().getLength());
			}
		}
		
		for (int i=0; i<getStartingFood(); i++) {
			boolean foodAdded = false;
			int[] foodLoc = {0, 0};
			while (!foodAdded) {
				foodLoc[0] = MainModel.randint(75, getMap().getLength()-75);
				foodLoc[1] = MainModel.randint(75, getMap().getHeight()-75);
				foodAdded = getStuffSet().add(foodLoc,"food");
				//System.out.println("trash"+trashLoc[0]+"/"+getMap().getLength());
			}
		}
	}
	
	public void gameOver(){
		inMiniGame = false;
		getStuffSet().clearAll();
	}
	
	
	// helper function (random numbers)
	/**
	 * Determine a random integer between 2 numbers
	 * @param min 		minimum value
	 * @param max 		maximum value
	 * @return 			number between min and max
	 */
	protected static int randint(int min, int max) {
		Random rn = new Random();
		int val = min + Math.abs(rn.nextInt()%(max-min));
		return val;
	}
	
	
	// printing
	/**
	 * Printing
	 *  (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String str = fishy.toString();
		str += "\n" + everyThing.toString();
		str += "\n" + map.toString();
		str += "\n" + timeString();
		if (getGameOver()) {
			str += "\nGame Over :-(";
		}
		return str;
	}
	
	public String timeString() {
		String str = "Time remaining: ";
		//long ms = remainingTime % 1000 / 10;
		long second = (remainingTime / 1000) % 60;
		long minute = (remainingTime / (1000 * 60)) % 60;
		//String time = String.format("%02d:%02d:%02d", minute, second, ms);
		String time = String.format("%02d:%02d", minute, second);
		str += time;
		return str;
	}
	
	// scoring
	
	
	// inc. score
	protected void increaseScore() {
		setPlayerScore(getPlayerScore() + getFoodScore());
	}
	
	
	// dec. score
	protected void decreaseScore() {
		setPlayerScore(getPlayerScore() - getTrashScore());
	}
	
	
	// timing
	protected void timeIncr() {
		remainingTime -= tickLength;
	}
	
	// getters
	
	
	// game over
	/**
	 * The controller will use this to determine if the game should still be played
	 * @return
	 */
	public boolean getGameOver() {
		return gameOver;
	}
	
	
	// in the mini game
	public boolean getInMiniGame() {
		return inMiniGame;
	}
	
	
	// map
	public Map getMap() {
		return map;
	}
	
	
	// main character
	public MainCharacter getMainCharacter() {
		return fishy;
	}
	
	
	// food and trash
	public StuffSet getStuffSet() {
		return everyThing;
	}
	
	
	// score change from food
	public int getFoodScore() {
		return foodScore;
	}
	
	
	// score change from trash
	public int getTrashScore() {
		return trashScore;
	}
	
	
	// current score
	public int getPlayerScore() {
		return playerScore;
	}
	
	
	// the mini game
	public MiniGame getMiniGame() {
		return miniGame;
	}
	
	
	public int getTickLength() {
		return tickLength;
	}
	
	public int getRemainingTime() {
		return remainingTime;
	}
	
	
	public boolean getHasWon() {
		return hasWon;
	}
	
	
	// setters
	
	
	// distance for accumulation
	/**
	 * Used to set the distance at which food can accumulate from view
	 * @param dist
	 */
	public void setAccumulationDistance(int dist) {
		accumulationDist = dist;
	}
	
	
	// game over
	public void setGameOver(boolean b) {
		gameOver = b;
	}
	
	
	// in the mini game
	public void setInMiniGame(boolean b) {
		inMiniGame = b;
	}
	
	
	// current score
	private void setPlayerScore(int a) {
		playerScore = a;
	}
	
	public void setTickLength(int a) {
		tickLength = a;
	}
	
	protected void setHasWon(boolean b) {
		hasWon = b;
	}

	public int getMiniHeight() {
		return miniHeight;
	}

	public void setMiniHeight(int miniHeight) {
		this.miniHeight = miniHeight;
	}

	public int getMiniWidth() {
		return miniWidth;
	}

	public void setMiniWidth(int miniWidth) {
		this.miniWidth = miniWidth;
	}

	public int getStartingTrash() {
		return startingTrash;
	}

	public void setStartingTrash(int startingTrash) {
		this.startingTrash = startingTrash;
	}

	public int getStartingFood() {
		return startingFood;
	}

	public void setStartingFood(int startingFood) {
		this.startingFood = startingFood;
	}
	
	
	
	
}

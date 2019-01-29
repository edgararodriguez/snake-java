package org.edgar;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.LinkedList;
import java.util.Random;

import javax.swing.JFrame;

import org.edgar.Direction;
import org.edgar.Board;
import org.edgar.Time;
import org.edgar.SnakeGameplay;
import org.edgar.TileType;


public class SnakeGameplay extends JFrame{
	
	//serial version UID
	private static final long serialVersionUID = 6678292058307426314L;
	
	//The number of milliseconds that pass between each frame
	private static final long FRAME_TIME = 1000L / 50L;
	
	//The start length of the snake
	private static final int MIN_SNAKE_LENGTH = 5;
	
	//The maximum number of directions we can have on the list
	private static final int MAX_DIRECTIONS = 3;
	
	//The Board instance
	private Board board;
	
	//random number generator for spawning food
	private Random random;
	
	//The clock Instance for handling the games logic
	private Time logicTimer;
	
	//Whether or not the user is running a new game
	private boolean isNewGame;
	
	//Whether or not the game is over.
	private boolean isGameOver;
	
	//Whether or not the game is paused 
	private boolean isGamePaused;
	
	//the list that contains the points for the snake
	private LinkedList<Point> snake;
	
	//The list that contains the queued directions
	private LinkedList<Direction> directions;
	
	//Creates Snake Game in a new window and sets controls
	private SnakeGameplay() {
		super("Snake");
		setLayout(new BorderLayout());
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setResizable(false);
				
		// Initialize the game's panels and add them to the window.
		this.board = new Board(this);
		
		add(board, BorderLayout.CENTER);
		
		//Adds a new key listener to the frame to process input. 
		addKeyListener(new KeyAdapter() {
			
			@Override
			public void keyPressed(KeyEvent e) {
				switch(e.getKeyCode()) {

				/*
				 * If the game is not paused, and the game is not over...
				 * 
				 * Ensure that the direction list is not full, and that the most
				 * recent direction is adjacent to North before adding the
				 * direction to the list.
				 */
				case KeyEvent.VK_W:
				case KeyEvent.VK_UP:
					if(!isGamePaused && !isGameOver) {
						if(directions.size() < MAX_DIRECTIONS) {
							Direction last = directions.peekLast();
							if(last != Direction.South && last != Direction.North) {
								directions.addLast(Direction.North);
							}
						}
					}
					break;

				/*
				 * If the game is not paused, and the game is not over...
				 * 
				 * Ensure that the direction list is not full, and that the most
				 * recent direction is adjacent to South before adding the
				 * direction to the list.
				 */	
				case KeyEvent.VK_S:
				case KeyEvent.VK_DOWN:
					if(!isGamePaused && !isGameOver) {
						if(directions.size() < MAX_DIRECTIONS) {
							Direction last = directions.peekLast();
							if(last != Direction.North && last != Direction.South) {
								directions.addLast(Direction.South);
							}
						}
					}
					break;
				
				/*
				 * If the game is not paused, and the game is not over...
				 * 
				 * Ensure that the direction list is not full, and that the most
				 * recent direction is adjacent to West before adding the
				 * direction to the list.
				 */						
				case KeyEvent.VK_A:
				case KeyEvent.VK_LEFT:
					if(!isGamePaused && !isGameOver) {
						if(directions.size() < MAX_DIRECTIONS) {
							Direction last = directions.peekLast();
							if(last != Direction.East && last != Direction.West) {
								directions.addLast(Direction.West);
							}
						}
					}
					break;
			
				/*
				 * If the game is not paused, and the game is not over...
				 * 
				 * Ensure that the direction list is not full, and that the most
				 * recent direction is adjacent to East before adding the
				 * direction to the list.
				 */		
				case KeyEvent.VK_D:
				case KeyEvent.VK_RIGHT:
					if(!isGamePaused && !isGameOver) {
						if(directions.size() < MAX_DIRECTIONS) {
							Direction last = directions.peekLast();
							if(last != Direction.West && last != Direction.East) {
								directions.addLast(Direction.East);
							}
						}
					}
					break;
				
				//pauses the time along with the game
				case KeyEvent.VK_P:
					if(!isGameOver) {
						isGamePaused = !isGamePaused;
						logicTimer.setPaused(isGamePaused);
					}
					break;
				
				//resets the game if its not in progress
				case KeyEvent.VK_ENTER:
					if(isNewGame || isGameOver) {
						resetGame();
					}
					break;
				}
			}
			
		});
		
		//Resize the window to the appropriate size, center it on the screen and display it.

		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}
	
	//starts the game
	private void startGame() {
		//Initializes everything that is going to be used in the game
		this.random = new Random();
		this.snake = new LinkedList<>();
		this.directions = new LinkedList<>();
		this.logicTimer = new Time(9.0f);
		this.isNewGame = true;
		
		//Set the timer to paused initially.
		logicTimer.setPaused(true);

		//this is the game loop it will update and continue to run the game until it is closed
		while(true) {
			//Get the current frame's start time.
			long start = System.nanoTime();
			
			//Update the logic timer.
			logicTimer.update();
			
			//If a cycle has elapsed on the logic timer, then update the game.
			if(logicTimer.hasElapsedCycle()) {
				updateGame();
			}
			
			//Repaint the board and side panel with the new content.
			board.repaint();
			
			/*
			 * Calculate the delta time between since the start of the frame
			 * and sleep for the excess time to cap the frame rate. 
			 */
			long delta = (System.nanoTime() - start) / 1000000L;
			if(delta < FRAME_TIME) {
				try {
					Thread.sleep(FRAME_TIME - delta);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	//updates the games logic
	private void updateGame() {
		/*
		 * Gets the type of tile that the head of the snake collided with. If 
		 * the snake hit a wall, SnakeBody will be returned, as both conditions
		 * are handled identically.
		 */
		TileType collision = updateSnake();
		
		// Here we handle the different possible collisions.
		if(collision == TileType.Fruit) {
			spawnFruit();
		} else if(collision == TileType.SnakeBody) {
			isGameOver = true;
			logicTimer.setPaused(true);
		}
	}
	
//Update the snakes position and size
	private TileType updateSnake() {

		/*
		 * Here we peek at the next direction rather than polling it. While
		 * not game breaking, polling the direction here causes a small bug
		 * where the snake's direction will change after a game over (though
		 * it will not move).
		 */
		Direction direction = directions.peekFirst();
				
//calculate the snakes head position
		Point head = new Point(snake.peekFirst());
		switch(direction) {
		case North:
			head.y--;
			break;
			
		case South:
			head.y++;
			break;
			
		case West:
			head.x--;
			break;
			
		case East:
			head.x++;
			break;
		}
		
		// will indicate that you lost the game if you hit a wall
		if(head.x < 0 || head.x >= Board.COL_COUNT || head.y < 0 || head.y >= Board.ROW_COUNT) {
			return TileType.SnakeBody; //Pretend we collided with our body.
		}
		
		/*
		 * Here we get the tile that was located at the new head position and
		 * remove the tail from of the snake and the board if the snake is
		 * long enough, and the tile it moved onto is not a fruit.
		 * 
		 * If the tail was removed, we need to retrieve the old tile again
		 * incase the tile we hit was the tail piece that was just removed
		 * to prevent a false game over.
		 */
		TileType old = board.getTile(head.x, head.y);
		if(old != TileType.Fruit && snake.size() > MIN_SNAKE_LENGTH) {
			Point tail = snake.removeLast();
			board.setTile(tail, null);
			old = board.getTile(head.x, head.y);
		}
		
		//update the snakes position if we dont crash with our tail
		if(old != TileType.SnakeBody) {
			board.setTile(snake.peekFirst(), TileType.SnakeBody);
			snake.push(head);
			board.setTile(head, TileType.SnakeHead);
			if(directions.size() > 1) {
				directions.poll();
			}
		}
				
		return old;
	}
	
	// resets the game and starts a new game
	private void resetGame() {
		
		//resets the new  game and game over so it can be repeated
		this.isNewGame = false;
		this.isGameOver = false;
		
		//create the new head at the center of the board
		Point head = new Point(Board.COL_COUNT / 2, Board.ROW_COUNT / 2);

		/*
		 * Clear the snake list and add the head.
		 */
		snake.clear();
		snake.add(head);
		
		/*
		 * Clear the board and add the head.
		 */
		board.clearBoard();
		board.setTile(head, TileType.SnakeHead);
		
		/*
		 * Clear the directions and add north as the
		 * default direction.
		 */
		directions.clear();
		directions.add(Direction.North);
		
		//resets the logic timer
		logicTimer.reset();
		
//spawns a new fruit
		spawnFruit();
	}
	
	//indicates whether we started a new game or not
	public boolean isNewGame() {
		return isNewGame;
	}
	
	//indicates whether the game is over or not
	public boolean isGameOver() {
		return isGameOver;
	}
	
	//indicates whether the game is paused or not
	public boolean isGamePaused() {
		return isGamePaused;
	}
	
	//Spawns a new fruit onto the board.
	private void spawnFruit() {


		//Get a random index based on the number of free spaces left on the board.

		int index = random.nextInt(Board.COL_COUNT * Board.ROW_COUNT - snake.size());
		
		//loops through until it finds the nth free index and selects uses that.
		int freeFound = -1;
		for(int x = 0; x < Board.COL_COUNT; x++) {
			for(int y = 0; y < Board.ROW_COUNT; y++) {
				TileType type = board.getTile(x, y);
				if(type == null || type == TileType.Fruit) {
					if(++freeFound == index) {
						board.setTile(x, y, TileType.Fruit);
						break;
					}
				}
			}
		}
	}
	
	//Gets the current point of the snake and gives back direction
	public Direction getDirection() {
		return directions.peek();
	}
	
	//starts the game
	public static void main(String[] args) {
		SnakeGameplay snake = new SnakeGameplay();
		snake.startGame();
	}

}
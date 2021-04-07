* Collaborators
  1. Samantha Anderson
  2. Ethan Lam
  3. An Nguyen

# Design
## The Game Loop
Because our game is an interactive program that updates in real time, we will need a sort of abstraction that handles updating the game in real time while also receiving user input. It achieve this goal we will introduce the idea of a **game loop**. At a high level a game loop will involve the following steps

1. Respond to user input
2. Update the game state
3. Render the game state
4. Go to step 1.

where the "game state" is any sort of abstract representation of the game in the moment. The game state could for example involve the positions and velocities of the players and ball. 

When the game responds to user input, it could for example update the velocities of the players. During the update phase, the game could then move all the players and ball in the direction of their velocity or update them in accordance with physical laws. Finally, with the updated state, the game is rendered to the screen, and the process starts over. This overview forms the basis of an interactive video game.

With the game loop covered at a high level, we will make an interface called `GameScene` that captures each stage of the game loop so that it can inevitably be driven by a sort of looping construction. In the `GameScene`, there will be methods that handle keyboard events as follow

```java
/**
 * The method called when it is detected that a key on the keyboard 
 * has been depressed
 * @param e a description of the keystroke that occurred
 */
void onKeyPressed(KeyEvent e);
/**
 * The method called when it is detected that a key on the keyboard 
 * has been released
 * @param e a description of the keystroke that occurred
 */
void onKeyReleased(KeyEvent e);
```

The `KeyEvent` object is from `javafx.scene.input.KeyEvent`, and it is no coincidence that the method signatures matches that of JavaFX's key event listeners. Whenever JavaFX detects a key press, we will pass that key event to these methods for the game scenes implementing `GameScene` to handle. Implementing these methods allow for custom game logic to execute in response to keyboard events.

Next, any game scene implementing `GameScene` needs to specify how the game state should be updated. Thus we will include the following method in the interface 

```java
/**
 * The method called to progress the game state
 * @param currentTime the time in nanoseconds relative to an arbitrary 
 * start time.
 */ 
void updateState(long currentTime);
```

This method will be called by the game loop to advance the game state which typically involves updating the position of game objects. We need to to pass in a `currentTime: long` to decouple the game updates from the computer hardware's power. Consider two computers with two different CPUs. On the faster CPU the game loop will execute at a higher frequency than the slower computer which means the faster CPU will update the game more often. If implemented without care, the game may run at a higher speed on the faster computer giving an inconsistent experience across different hardwares. To remedy this issue, every time the game is updated, the time in nanoseconds must be supplied so that the game can determine the proper way to update the state so that the game state is tied to time that has passed and not hardware power. Thus even if one were to play the game on different computers, the experience will feel relatively similar as one's experience with time does not often change. 

Finally the `GameScene` needs a way to render the scene which means the following method must implemented

```java
/**
 * The method called to render the game scene to a canvas
 * @param canvas the canvas to draw the scene on
 */ 
void render(Canvas canvas);
```

The `Canvas` object is from `javafx.scene.canvas.Canvas` and any sort of custom render code for the `GameScene` should perform all draw operations on this `Canvas`.

Finally, as a convenience method `GameScene` should have a default method called `generateRenderableComponent(int, int) -> Canvas`. This method essentially creates a `Canvas` object for us and configures it to handle keyboard inputs. It will take two integers which parametrizes the returned `Canvas`'s width and height, respectively. A `Canvas` will first be created and its key pressed handler will be attached to the `onKeyPressed` method in `GameScene`. Similarly, the key released handler will be attached to the `onKeyReleased` method in `GameScene`. Thus all keyboard events on the `Canvas` will be dispatched to class implementing `GameScene`. However in the current state, the `Canvas` will not accept keyboard inputs as it is not "focused." The `Canvas` can received keyboard inputs by notifying JavaFX that the `Canvas` can be "focused" on. Then we just request the focus be on the `Canvas` and return the `Canvas` object. In code the method would resemble

```java
/**
 * Creates a Canvas object configured to be compatible with the GameScene
 * interface
 * @param width the Canvas's width in pixels
 * @param height the Canvas's height in pixels
 */
default Canvas generateRenderableComponent(int width, int height) {
    Canvas canvas = new Canvas(width, height);
    canvas.setOnKeyPressed(this::onKeyPressed);
    canvas.setOnKeyReleased(this::onKeyReleased);
    canvas.setFocusTraversable(true);
    canvas.requestFocus(true);
    return canvas;
}
```

Finally, `GameScene` should extend `Serializable`. We will impose this constraint so that later in the development of the game, we have the capability of saving game scenes on the drive and even transmit game scenes over the network.

In whole the interface should look as such

```java
public interface GameScene extends Serializable {
    void updateState(long currentTime);
    void onKeyPressed(KeyEvent e);
    void onKeyReleased(KeyEvent e);
    void render(Canvas canvas);

    default Canvas generateRenderableComponent(int width, int height) {
        Canvas canvas = new Canvas(width, height);
        canvas.setOnKeyPressed(this::onKeyPressed);
        canvas.setOnKeyReleased(this::onKeyReleased);
        canvas.setFocusTraversable(true);
        canvas.requestFocus();

        return canvas;
    }
}
```

The `GameScene` sets the foundation for the different phases a game goes through. We now need to drive it with a looping construct. Rather than use a naive `while` loop, we will make use of the `AnimationTimer` in JavaFX so as to not lock up the main thread of our program with the game loop. We will make a class called `GameLoop` which extends `AnimationTimer`. In order to extend `AnimationTimer` the class must override the `handle` method which is called every time the `AnimationTimer` "ticks" which occurs at a fairly high frequency. First, however, the class `GameLoop` has two fields: a `gameScene: GameScene` and a `canvas: Canvas`. The `GameScene` informs the `GameLoop` what scene it needs to drive, and the `Canvas` informs the `GameLoop` where the rendering should take place. The canonical constructor should be used to fill these fields. As for the `handle` from `AnimationTimer`, we simply need to call `updateState` and then `render` on `gameScene` every time the `AnimationTimer` pulses/ticks. In code the `handle` function would resemble

```java
public class GameLoop extends AnimationTimer {
    private GameScene gameScene;
    private Canvas canvas;
    // --snip--

    /**
    * The method called periodically by AnimationTimer
    * @param now the time in nanoseconds
    */
    public void handle(long now) {
        gameScene.updateState(now);
        gameScene.render(canvas);
    }
}
```

Now, whenever we would like to render a `GameScene`, we simply instantiate an instance of some `GameScene`, create a `Canvas` from it using `generateRenderableComponent`, pass both objects into the `GameLoop`, and run the `start` method on the `AnimationTimer` which `GameLoop` inherits.

The work up to this point is represented diagrammatically below.

![Game Loop UML](document_assets/gameloop_uml_img.png)


<script src="https://polyfill.io/v3/polyfill.min.js?features=es6"></script>
<script id="MathJax-script" async src="https://cdn.jsdelivr.net/npm/mathjax@3/es5/tex-mml-chtml.js"></script>


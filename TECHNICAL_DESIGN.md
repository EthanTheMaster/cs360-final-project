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

## The Physics Engine
To make computation easier we will develop a helper class called `Vec2d`. This class represents 2 dimensional vectors and has methods that can execute common vector operations. In this class there are 2 fields, both of which are doubles: `x` and `y`. For brevity assume that the canonical constructor is used and setters and getters for `x` and `y` are implemented. Mathematically, an instance of `Vec2d` will encode the vector $\left\langle x, y \right\rangle$. This class should also implement `Serializable`.

First, we will define a method `scale` which takes a `double` `c` and returns a new vector that is a scaled version of the current vector. To do this, we simply multiply each component of the vector by `c`.

```java
/**
 * Scales a vector by a constant multiple
 * @param c the constant multiple
 * @return a scaled vector
 */
public Vec2d scale(double c) {
    return new Vec2d(x*c, y*c);
}
```

We will also define a method `add` which adds two vectors and returns the result. It takes a `Vec2d` called `other`, and adds `this` with `other`, component-wise.

```java
/**
 * Adds the current vector the another vector
 * @param other the vector to add with
 * @return the vector sum
 */
public Vec2d add(Vec2d other) {
    return new Vec2d(
        this.x + other.x, this.y + other.y
    );
}
```

Similarly subtraction is defined but the method is called `sub` and the components are subtracted as opposed to added.

```java
/**
 * Computes the difference between the current vector with another vector
 * @param other the vector to subtract with
 * @return the vector difference
 */
public Vec2d sub(Vec2d other) {
    return new Vec2d(
            this.x - other.x, this.y - other.y
    );
}
```

It will also be convenient to compute dot products, so we will also create a method `dot` that takes a `Vec2d` called `other` and returns a `double`. Given two vectors $\left\langle x, y \right\rangle$ and $\left\langle x', y' \right\rangle$ the dot product is $x \cdot x' + y \cdot y'$.

```java
/**
  * Computes the dot product between the current vector and another vector
  * @param other the other vector
  * @return the dot product
  */
public double dot(Vec2d other) {
    return this.x * other.x + this.y * other.y;
}
```

The method `mag` should simply return a `double` representing the magnitude or length of the current vector. From linear algebra the magnitude of vector $\mathbf{v}$ is $\sqrt{\mathbf{v} \cdot \mathbf{v}}$ which conveniently can be implementing using `dot`.

```java
/**
 * Computes the length of the current vector
 * @return the length of the vector
 */
public double mag() {
    return Math.sqrt(this.dot(this));
}
```

We will also implement a method `projectOnto` which takes another `Vec2d` and returns a `Vec2d` representing the projection. Thus `a.projectOnto(b)` in mathematical notation would be $\text{proj}_\mathbf{b}\mathbf{a}$. Using linear algebra, this can be computed with 

$$
    \text{proj}_{\mathbf{b}}\mathbf{a} = \left(\mathbf{a} \cdot \frac{\mathbf{b}}{\lVert \mathbf{b} \rVert}\right) \mathbf{b}
$$

```java
/**
 * Computes the projection of the current vector onto another vector
 * @param other the vector to project onto
 * @return the projection vector
 */
public Vec2d projectOnto(Vec2d other) {
    Vec2d unit = other.normalize();
    return unit.scale(unit.dot(this));
}
```

Finally, the last method to be implemented is `rotate` which takes a `double` called `angle` in radians. It returns a `Vec2d` that is the rotation of the current vector, rotated about the origin by `angle`. This action can be done using a rotation matrix

$$
\begin{bmatrix}
\cos \theta & -\sin \theta \\\\  
\sin \theta & \cos \theta 
\end{bmatrix} 
\begin{bmatrix} x \\\\ y \end{bmatrix}
\\ = \\ 
\begin{bmatrix} 
x\cos\theta-y\sin\theta \\\\
x\sin\theta+y\cos\theta
\end{bmatrix}.
$$

For more information see [https://en.wikipedia.org/wiki/Rotation_matrix](https://en.wikipedia.org/wiki/Rotation_matrix) which is where the above formula came from.


```java
/**
 * Rotates the current vector about the origin
 * @param angle the angle to rotate by in radians
 * @return a rotated vector
 */
public Vec2d rotate(double angle) {
    double c = Math.cos(angle);
    double s = Math.sin(angle);
    return new Vec2d(x*c - y*s, x*s + y*c);
}
```

Now with two dimensional vectors implemented, we will use mathematical notation for vectors when convenient with the understanding that they can easily converted into an instance of `Vec2d`.

To detect collisions between different types of shapes, we will make an interface `Collider` with the following form.

```java
public interface Collider extends Serializable {
    /**
     * Determines whether two Colliders intersect each other
     * @param other the other collider with which to check for an 
     * intersection
     * @return a boolean that determines if the current Collider intersects 
     * with other
     */
    boolean collide(Collider other);
    /**
     * Gets the position of the Collider
     * @return the position of the Collider
     */
    Vec2d getPosition();
    /**
     * Sets the position of the Collider
     * @param position the new location of the Collider
     */
    void setPosition(Vec2d position);
}
```
The interface should extend `Serialiable` so that it can be converted into bytes for storage and transmission.

Notice that we are making use of `Vec2d` to describe a physical property like position. In any event, the method of particular interest is `collide` which every class implementing `Collider` must create. This method is used to check collision between two `Collider` objects.

The first `Collider` we will create will be a `RectangleCollider`. A `RectangleCollider` is defined by the position of its top-left corner which we will call the `origin: Vec2d`. The collider is also defined by the following doubles: `width`, `height`, and `angle`. Note that `angle` should be in radians and it describes how much the rectangle is rotated about its origin. See the figure below for an illustration. In the image $(x, y)$ is `origin` and $\theta$ is `angle`.

![Rectangle Collider](document_assets/rect_def.png)

The canonical construction should be made, and setters and getters for the attributes should be made. For `setPosition` and `getPosition` from `Collider`, the `origin` should be taken as the `RectangleCollider`'s position.

The `RectangleCollider` class should also have a helper method `computeVerticesAndBasis` which returns an array of `Vec2d`s. The returned array will be composed of 6 `Vec2d`s. The 4 vertices of the rectangle, starting at the `origin` and moving clockwise, will be the first 4 entries and the last 2 entries will be "basis" vectors of the rectangle. See the figure below to visualize the basis vectors which in essence form a local coordinate system on the rectangle relative to `origin`. The basis vectors in the figure are $\mathbf{b}_1$ and $\mathbf{b}_2$, and note that for convenience purposes these vector will have unit length.

![Rectangle Collider Basis](document_assets/rect_basis.png)

We can easily compute the 6 values using basic linear algebra. Let

$$
\mathbf{u} = \left\langle \text{width}, 0 \right\rangle 
$$

and 

$$
\mathbf{v} = \left\langle 0, \text{height} \right\rangle.
$$
As of now these two vectors form the axis-aligned version of the rectangle. To get the rotated version, we should rotate both of these vectors by `angle` using the `rotate` method in `Vec2d`. We will now denote the rotated version of $\mathbf{u}$ and $\mathbf{v}$ as $\mathbf{u}'$ and $\mathbf{v}'$ respectively. We can now form the array.

$$
\begin{bmatrix}
    \mathtt{origin} \\\\
    \mathtt{origin} + \mathbf{u}' \\\\
    \mathtt{origin} + \mathbf{u}' + \mathbf{v}' \\\\
    \mathtt{origin} + \mathbf{v}' \\\\
    \mathbf{u}'\mathtt{.normalize()} \\\\
    \mathbf{v}'\mathtt{.normalize()} \\\\
\end{bmatrix}
$$

The code would resemble 

```java
/**
 * Computes the vertices of the rotated rectangle along and also returns 
 * the local coordinate space
 * @return a list of vectors where the first 4 vectors are the vertices 
 * and the last two are the basis vectors of the local coordinate space
 */
public Vec2d[] computeVerticesAndBasis() {
    Vec2d b1 = new Vec2d(width, 0).rotate(angle);
    Vec2d b2 = new Vec2d(0, height).rotate(angle);
    Vec2d sum = b1.add(b2);

    return new Vec2d[] {
            origin,
            origin.add(b1),
            origin.add(sum),
            origin.add(b2),
            b1.normalize(),
            b2.normalize()
    };
}
```

Before we implement `collide` from `Collider` for `RectangleCollider`, we should introduce another collider: `CircleCollider`. A `CircleCollider` is parameterized by the following fields: `center: Vec2d` and `radius: double`. Both of these fields should have setters and getters, and the canonical constructor should be used to populate these fields. Furthermore `getPosition` and `setPosition` from `Collider` should access and mutate `center` as the position. 

We now introduce the collision detection for `RectangleCollider`. The `collide` method from the `Collider` interface has as parameter an `other` object of type `Collider`. Thus it is sensible to break up the collision detection depending on the type `Collider` of collider passed.

For the first case, assuming that `other` is a `RectangleCollider`, we will assert that a collision has occurred iff the vertex of one `RectangleCollider` lies within the boundaries of the other. Therefore, let `thisVerticesAndBasis = this.computeVerticesAndBasis()` and `otherVerticesAndBasis = other.computeVerticesAndBasis()`. We need to check if `other`'s vertices lie in `this`'s region. For each vertex `v` in `otherVerticesAndBasis[0:4]`, compute the displacement vector `d: Vec2d` of `v` relative to `this.origin`. That is 

$$
\mathbf{d} = \mathbf{v} - \mathtt{this.origin}
$$ 

We now express `d` in terms of `this`'s basis vectors which amounts to finding constants $c_1$ and $c_2$ such that 

$$
\mathbf{d} = c_1 \mathbf{b}_1 + c_2 \mathbf{b}_2
$$
where the $\mathbf{b}_i$'s are `this`'s basis vectors. From linear algebra it turns out that $c_i = \mathbf{d} \cdot \mathbf{b}_i$. This is due to the fact that the $\mathbf{b}_i$'s form what is known as an "orthonormal basis". In any event, if 

$$    
0 \leq c_1 \leq \mathtt{this.width}
$$

and

$$
0 \leq c_2 \leq \mathtt{this.height}
$$

then `v` is in the bound of `this`'s region and we can return true. We now do the same analysis but interchange the roles of `this` and `other` to check whether `this`'s vertices lie within `other`'s bound. The analysis is omitted and the code below should clear any confusion.

```java
public boolean collide(Collider other) {
    if (other instanceof RectangleCollider) {
        RectangleCollider otherRect = (RectangleCollider) other;
        Vec2d[] thisVerticesAndBasis = this
                                        .computeVerticesAndBasis();
        Vec2d[] otherVerticesAndBasis = otherRect
                                        .computeVerticesAndBasis();

        // Check if other's vertices are inside this's rectangular region
        for (int i = 0; i < 4; i++) {
            Vec2d vertex = otherVerticesAndBasis[i];
            Vec2d displacement = vertex.sub(thisVerticesAndBasis[0]);
            // Decompose displacement in terms of this's
            // local coordinate space
            // displacement = c1*this.b1 + c2*this.b2
            double c1 = thisVerticesAndBasis[4].dot(displacement);
            double c2 = thisVerticesAndBasis[5].dot(displacement);

            // vertex is in this's region iff
            // 0 <= c1 <= this.width and
            // 0 <= c2 <= this.height
            if (
                0 <= c1 && c1 <= this.width && 
                0 <= c2 && c2 <= this.height
            ) {
                return true;
            }
        }

        // Check if this's vertices are inside other's rectangular region
        for (int i = 0; i < 4; i++) {
            Vec2d vertex = thisVerticesAndBasis[i];
            Vec2d displacement = vertex.sub(otherVerticesAndBasis[0]);
            // Decompose displacement in terms of other's 
            // local coordinate space
            // displacement = c1*other.b1 + c2*other.b2
            double c1 = otherVerticesAndBasis[4].dot(displacement);
            double c2 = otherVerticesAndBasis[5].dot(displacement);

            // vertex is in this's region iff
            // 0 <= c1 <= other.width and
            // 0 <= c2 <= other.height
            if (
                0 <= c1 && c1 <= otherRect.width && 
                0 <= c2 && c2 <= otherRect.height
            ) {
                return true;
            }
        }
    } else if (other instanceof CircleCollider) {
        // --snip--
    }
    return false;
}
```

We have handled rectangle on rectangle collisions. We shall now focus our attention on rectangle on circle collisions. While still in `RectangleCollider`, we handle the case in which `other: Collider` is now in fact a `CircleCollider`.

![Closest Point](document_assets/rect_closest.png).

Before discussing rectangle on circle collisions, it is helpful to understand how one can find the closest point on the rectangle to some point whose displacement from the rectangle's origin is $\mathbf{v}$. See the diagram above. As the diagram suggests, we can find the closest point by representing $\mathbf{v}$ in terms of the basis vectors similar to what we did for rectangle on rectangle collisions. Using the same technique as before we can find constants $c_1$ and $c_2$ such that 

$$
\mathbf{v} = c_1 \mathbf{b}_1 + c_2 \mathbf{b}_2
$$

A point is in the rectangle iff its displacement vector has $c_1 \in [0, w]$ and $c_2 \in [0, h]$, assuming we are following the notation laid out in the diagram. Therefore we can compute the closest point by finding the closest value in $[0, w]$ to $c_1$ and the closest value in $[0, h]$ to $c_2$. Therefore to find the closest point let $c_1' = \max(0, \min(c_1, w))$ and $c_2' = \max(0, \min(c_2, h))$. The closest point will thus be 

$$
\text{closest point} = \mathtt{origin} + c_1' \mathbf{b}_1 + c_2' \mathbf{b}_2
$$

The theory outlined above is realized in the code below. The method `findClosestPoint` takes a `point: Vec2d` and returns a `Vec2d` which is the point on the rectangle closest to `point`.
```java
/**
 * Finds the closest point on the rectangle to a given point
 * @param point the point to find the closest point to
 * @return the closest point to the given point
 */
public Vec2d findClosestPoint(Vec2d point) {
    Vec2d[] thisVerticesAndBasis = this.computeVerticesAndBasis();
    Vec2d basis1 = thisVerticesAndBasis[4];
    Vec2d basis2 = thisVerticesAndBasis[5];
    Vec2d displacement = point.sub(thisVerticesAndBasis[0]);

    // Decompose displacement in terms of basis1 and basis2
    // displacement = c1*basis1 + c2*basis2
    double c1 = basis1.dot(displacement);
    double c2 = basis2.dot(displacement);

    // Clamp c1 to [0, width] and clamp c2 to [0, height]
    c1 = Math.max(0, c1);
    c2 = Math.max(0, c2);
    c1 = Math.min(width, c1);
    c2 = Math.min(height, c2);

    return basis1.scale(c1).add(basis2.scale(c2)).add(thisVerticesAndBasis[0]);
}
```

Now that we have a way of finding the closest point, to determine rectangle on circle collision, we simply have to find the closest point on the rectangle to the circle and check if the distance from the circle's center to the closest point is less than or equal to the circle's radius. Diagrammatically the figure below argues the reasoning for this collision check.

![Rectangle on Circle Collision](document_assets/rect_circ.png)

In code the collision check would be as follow

```java
public boolean collide(Collider other) {
    if (other instanceof RectangleCollider) {
        // --snip--
    } else if (other instanceof CircleCollider) {
        CircleCollider otherCircle = (CircleCollider) other;
        Vec2d closestPoint = findClosestPoint(otherCircle.getCenter());
        return 
            closestPoint.sub(otherCircle.getCenter()).mag() 
        <= 
            otherCircle.getRadius();
    }
    return false;
}
```

With the `RectangleCollider`'s implementation finished, the `CircleCollider` should also be finished. Like in the `RectangleCollider`, the `collide` method takes an `other: Collider` object and we should execute different logic depending on the type of `Collider` `other` is. If `other` is a `CircleCollider`, determining a collision is straightforward. Simply compute the distance between the two centers. If that distance is less than or equal to the sum of the radii, then a collision occurred.

If, however, `other` is a `RectangleCollider` then simply call `other.collide(this)` as circle on rectangle collisions are the same as rectangle on circle collisions. 

In code `collide` for the `CircleCollider` would be

```java
public boolean collide(Collider other) {
    if (other instanceof CircleCollider) {
        CircleCollider otherCircle = (CircleCollider) other;
        double distance = otherCircle.center.sub(this.center).mag();
        return distance <= this.radius + otherCircle.radius;
    } else if (other instanceof RectangleCollider) {
        RectangleCollider otherRect = (RectangleCollider) other;
        return otherRect.collide(this);
    }
    return false;
}
```

The diagram below shows the components of the physics engine.

![Physics UML Diagram](document_assets/physics_uml_img.png)

## Entities
Up to this point, we can implement the key objects in the game pong. There are obstacles, players, and a ball. Unifying all these objects are that they are game objects present in the game which we will describe as an "entity". 

To unify all game objects under the same framework, we will create an abstract `Entity` class. This class will have the following protected fields that children can inherit: `id: String`, `position: Vec2d`, `velocity: Vec2d`, and `colliders: ArrayList<Collider>`. These fields will characterize an `Entity` and provide enough information for the dynamics of an entity. Setters and getters for all these fields should be made.

Subclasses of `Entity` should have some way to be rendered to the screen. Thus `Entity` will have an abstract method `render` which takes a `Canvas` object. 

```java
/**
 * Renders the entity to a canvas
 * @param canvas the canvas to render the entity on
 */
public abstract void render(Canvas canvas);
```

Furthermore, when an `Entity` collides with another `Entity` the entities should be alerted of this fact. Thus there needs to be an abstract `onCollision` method with two parameters: `other: Entity` and `otherCollider: Collider`. The first parameter is the `Entity` that collided with the current `Entity`. The second parameter is the `Collider` on the other `Entity` that triggered the collision event. 

```java
/**
 * The method invoked when this entity has collided with another entity
 * @param other the entity that this entity collided with
 * @param otherCollider the collider on the other entity that detected 
 * the collision
 */
public abstract void onCollision(Entity other, Collider otherCollider);
```

Every `Entity` has associated with it a list of `Collider`s in the `colliders` field. Thus it would be helpful to have a method `collidesWith` that takes another `Entity` called `other` and returns `null` if the two entities did not collide but returns two `Collider`s if a collision did take place where the two `Collider`s are the colliders that caused a collision. Checking for a collision between two entities is straightforward. We simply check every pair of `Collider`s in both entities and run the `collide` method to check if the pair collides.

```java
/**
 * Checks if this entity collides with another entity
 * @param other the entity to check collision with
 * @return return either null or 2 colliders. Null is returned if there is 
 * no collision, but if there is a collision the first collider will be 
 * the collider in this entity and the second collider will be the 
 * collider in the other entity.
 */
public Collider[] collidesWith(Entity other) {
    for (Collider thisCollider : this.colliders) {
        for (Collider otherCollider : other.colliders) {
            if (thisCollider.collide(otherCollider)) {
                return new Collider[]{thisCollider, otherCollider};
            }
        }
    }

    return null;
}
```

Finally, `Entity` should implement `Serializable` so that they can be saved to disk or transmitted over a network.

### The Obstacle Entity
An `Obstacle` class extending the `Entity` class will essentially be the `Entity` form of composite `Collider`s. The idea is that we would like to combine `Collider`s into one shape which can be deemed as an `Entity` for the game engine to actuate. 

`Obstacle`s should have a color assigned. The color will be representing using RGB in `colorRgb: int[]`. We should also have the ability to choose whether the `Obstacle` is visible which will be in a field `isVisible: boolean`. Invisible `Obstacle`'s allow for trigger zones where some action can trigger if some collision occurs. For example when the ball leaves the screen we could put trigger zones outside the field to reset the game. With this in mind, the last field in an `Obstacle` should be a `trigger: CollisionEventHandler` where `CollisionEventHandler` is an interface whose only method requires that the `Entity` and its `Collider` that cause the collision event to occur be passed. Additionally, it should extend `Serializable`.

```java
public interface CollisionEventHandler extends Serializable {
    void handleCollision(Entity other, Collider otherCollider);
}
```

Whenever a developer creates an `Obstacle`, a `CollisionEventHandler` can be passed to allow for customizable game logic.

Setters and getters for all the fields should be made. However, special care is taken for the constructor. 

```java
public Obstacle(
    String name, 
    Collider[] hitZones, 
    int[] colorRgb, 
    boolean isVisible, 
    CollisionEventHandler trigger
) {
    this.id = name;
    this.position = new Vec2d(0, 0);
    this.velocity = new Vec2d(0, 0);
    this.colliders = new ArrayList<>();
    colliders.addAll(Arrays.asList(hitZones));

    this.colorRgb = colorRgb;
    this.isVisible = isVisible;
    this.trigger = trigger;
}
```

Recall that `Obstacle` extends `Entity` so the fields of `Entity` should be populated. The code displayed above is self-explanatory, but attention should be focused on the fact that the `position` is initialized to `(0, 0)`. This position was arbitrarily chosen as it is not clear what should be the position if the `Obstacle` is composed of multiple "zones".

We should also override the `setPosition` method in `Entity` as setting the `position` field alone does not actually move the `Obstacle`. The reason is that the `Collider`s need to move whenever the position is changed. Because the `position` was arbitrarily chosen to be `(0, 0)`, we need to understand what it means to update an `Obstacle`'s position. Ideally we would want the colliders forming the `Obstacle` to maintain their positions relative to each other. Therefore whenever `setPosition` is called on the `Obstacle`, we need to compute a `displacement` that represents how much we need to move each `Collider` to give the illusion of the `Obstacle` having its position changed. The `displacement` is simply the difference between the given `position` passed to `setPosition` and the `Obstacle`'s current position. With this `displacement` we just change the position of each `Collider` in `colliders` by this `displacement.`

```java
@Override
public void setPosition(Vec2d position) {
    Vec2d displacement = position.sub(this.position);
    super.setPosition(position);

    for (Collider collider : this.colliders) {
        collider.setPosition(
                collider.getPosition().add(displacement)
        );
    }
}
```

`Entity` also has an abstract `render` method which means we must define how to render an `Obstacle`. This is mechanically straightforward to do. We simply iterate through `colliders` and draw a rectangle if the `Collider` is a `RectangleCollider` and draw a circle if the `Collider` is a `CircleCollider`. The particularities of JavaFX drawing are explored in the code below. Note that if `isVisible` is `false`, then no rendering should take place. 

**From this point on, we will impose that all spatial quantities in `Entity` assume that the coordinate system of the screen space is based on the unit square. All temporal units are assumed to be seconds.**

The unit square screen space means the top left corner is `(0, 0)`, the top right corner is `(1, 0)`, the bottom left corner is `(0, 1)`, and the bottom right corner is `(1, 1)`. This design choice was made to decouple the rendering from the peculiarities of different screen resolutions and different window sizes.

This unit square coordinate system is why during rendering quantities are multiplied by the `Canvas`'s width and height for quantities relating to horizontal and vertical distances, respectively.

```java
@Override
public void render(Canvas canvas) {
    if (!isVisible) {
        return;
    }

    GraphicsContext ctx = canvas.getGraphicsContext2D();
    ctx.setFill(Color.rgb(colorRgb[0], colorRgb[1], colorRgb[2]));
    for (Collider collider : this.colliders) {
        if (collider instanceof RectangleCollider) {
            RectangleCollider rectangle = (RectangleCollider) collider;
            Vec2d[] verticesAndBasis = rectangle.computeVerticesAndBasis();
            ctx.fillPolygon(
                    new double[]{
                        verticesAndBasis[0].getX() * canvas.getWidth(),
                        verticesAndBasis[1].getX() * canvas.getWidth(),
                        verticesAndBasis[2].getX() * canvas.getWidth(),
                        verticesAndBasis[3].getX() * canvas.getWidth(),
                    },
                    new double[]{
                        verticesAndBasis[0].getY() * canvas.getHeight(),
                        verticesAndBasis[1].getY() * canvas.getHeight(),
                        verticesAndBasis[2].getY() * canvas.getHeight(),
                        verticesAndBasis[3].getY() * canvas.getHeight(),
                    },
                    4
            );
        } else if (collider instanceof CircleCollider) {
            CircleCollider circle = (CircleCollider) collider;
            double centerX = circle.getCenter().getX();
            double centerY = circle.getCenter().getY();
            double r = circle.getRadius();
            ctx.fillOval(
                    (centerX - r) * canvas.getWidth(),
                    (centerY - r) * canvas.getHeight(),
                    2*r * canvas.getWidth(),
                    2*r * canvas.getHeight()
            );
        }
    }
}
```

Finally, `Entity` has `onCollision` as abstract. As stated before, whenever a collision with the `Obstacle` is detected, this method will be invoked. What to do during a collision event is up to the developer so will we will simply pass the parameters of `onCollision` to `trigger.handleCollision` as such

```java
@Override
public void onCollision(Entity other, Collider otherCollider) {
    if (trigger != null) {
        trigger.handleCollision(other, otherCollider);
    }
}
```

Note that a developer may choose to not pass a `CollisionEventHandler`. In which case, we simply do nothing when a collision occurs.

### The Player Entity
The player is a rectangular paddle. For the `Player` class which also extends `Entity`, it is sensible to have a `RectangularCollider` called `collider` as one of the fields. This `Collider` will also parameterize the player's width and height. We would also like the `Player` to have a property `moveSpeed: double` that dictates how fast the player moves. For example if `moveSpeed = 0.3`, then that implies that the player can move 30% of the screen in 1 second. We also should have a field `direction: int` which takes on the values `{-1, 0, 1}`. The value of direction determines if the `Player` is currently moving in the positive direction associate with it which we will call `positiveDirection: Vec2d`. Recall that `Player`s may be on the top/bottom or left/right sides of the screen so the positive direction will be down and right, respectively. Next we need to consider the keys that move the player. The fields `directionKeyPositive: int` and `directionKeyNegative: int` should be in `Player` and are the key codes on the keyboard associated with positive and negative movement. Finally, there needs to be a field `lastContactFreePosition: Vec2d` which will be used to revert the `Player` back to a position that is hopefully collision free. 

Setters and getters should be made only for `moveSpeed` and `direction` as the other fields are "internal fields" that should not be exposed to the outside world.

Making the constructor is slightly less straightforward as we need to take into account the fields of `Entity`. In any event, the code below is self-explanatory and merely fills in the fields mentioned above and the fields in `Entity`. It should be noted that we are assuming that `Player`s created are initialized to in a collision free state. When creating the `RectangleCollider` for the `Player`, we also need to add this collider to `colliders` in `Entity`. Otherwise, the `Player` would effectively have no colliders in the eyes of the engine.
```java
public Player(
        String name,
        Vec2d position,
        double width,
        double height,
        Vec2d positiveDirection,
        int directionKeyPositive,
        int directionKeyNegative,
        double moveSpeed
) {
    // Fill in Entity attributes
    this.id = name;
    this.position = position;
    this.velocity = new Vec2d(0, 0);

    this.collider = new RectangleCollider(position, width, height, 0);
    this.colliders = new ArrayList<>();
    this.colliders.add(collider);

    // Fill in Player fields
    direction = 0;
    this.positiveDirection = positiveDirection;
    this.directionKeyPositive = directionKeyPositive;
    this.directionKeyNegative = directionKeyNegative;
    this.moveSpeed = moveSpeed;
    lastContactFreePosition = position;
}
```

Just like the `Obstacle` class, we need to override the `setPosition` method in `Entity` so that the `RectangleCollider` in `Player` can "track" the `Player` as it has its position updated. First, whenever the position is updated, we will update `lastContactFreePosition` to be the current position _before_ we update the `position`. Then we modify `position` to the position passed, and then we update the position of the `RectangleCollider`.

```java
@Override
public void setPosition(Vec2d position) {
    lastContactFreePosition = this.position;
    super.setPosition(position);
    // Update the collider's position
    collider.setPosition(position);
}
```

The `render` method from `Entity` is not particularly interesting. We simply draw a rectangle wherever the player is.

```java
@Override
public void render(Canvas canvas) {
    GraphicsContext ctx = canvas.getGraphicsContext2D();
    double canvasWidth = canvas.getWidth();
    double canvasHeight = canvas.getHeight();

    ctx.setFill(Color.RED);
    ctx.fillRect(
            collider.getOrigin().getX() * canvasWidth,
            collider.getOrigin().getY() * canvasHeight,
            collider.getWidth() * canvasWidth,
            collider.getHeight() * canvasHeight
    );
}
```

The `onCollision` method from `Entity` also is not very exciting. Essentially, if a collision is detected, we will revert the `Player`'s position back to its last contact free location, making sure that `lastContactFreeLocation` truly is a location that is contact free. Recall that `setPosition` will update the `lastContactFreeLocation`, but if a collision occurred, we most certainly do not want to assert that `lastContactFreeLocation` is the location the `Player` is currently in which is causing a collision with another `Entity`. 

```java
@Override
public void onCollision(Entity other, Collider otherCollider) {
    setPosition(lastContactFreePosition);
    lastContactFreePosition = this.position;
}
```

As a convenience, we will make the following methods `setDirectionKeyPress` and `setDirectionKeyRelease`. Both of these methods take a `keyCode: int` and change the direction of the `Player` depending on the key pressed.

For `setDirectionKeyPress`, if the key code matches `directionKeyPositive` then we set the `direction` to `+1`. If the key code matches `directionKeyNegative` then we set the `direction` to `-1`.

```java
/**
 * Sets the direction of the player based on the key pressed
 * @param keyCode the key pressed
 */
public void setDirectionKeyPress(int keyCode) {
    if (keyCode == directionKeyPositive) {
        setDirection(1);
    } else if (keyCode == directionKeyNegative) {
        setDirection(-1);
    }
}
```

For `setDirectionKeyRelease`, if the key released matches the direction the `Player` is currently moving in, then we want to set the `Player`'s `direction` to `0`.

```java
/**
 * Sets the direction of the player based on the key released
 * @param keyCode the key released
 */
public void setDirectionKeyRelease(int keyCode) {
    // If the key released matches the direction being moved in, stay still
    if (
        (keyCode == directionKeyPositive && direction == 1) ||
        (keyCode == directionKeyNegative && direction == -1)
    ) {
        setDirection(0);
    }
}
```

We are not quite finished yet. We need to modify the `setDirection` method. Such that whenever the direction is set, the `Player`'s velocity is also altered. We simply just change `velocity` to $(\mathtt{direction} \cdot \mathtt{moveSpeed}) \cdot \mathtt{positiveDirection}$.

```java
public void setDirection(int direction) {
    this.direction = direction;
    this.velocity = positiveDirection.scale(direction * moveSpeed);
}
```

### The Ball Entity
Like the `Player` class, the `Ball` will be characterized by its `Collider` `collider: BallCollider` which holds both the `Ball`'s position and radius. The `Ball` class will also have a `lastContactFreeLocation: Vec2d` to help revert the `Ball` back to a collision free state. The constructor is shown below and it follows the same format as the previous two entities.

```java
public Ball(
    String name,
    Vec2d centerPosition,
    double radius
) {
    this.id = name;
    this.position = centerPosition;
    this.velocity = new Vec2d(0, 0);
    this.colliders = new ArrayList<>();

    this.collider = new CircleCollider(centerPosition, radius);
    this.colliders.add(collider);

    lastContactFreePosition = centerPosition;
}
```

For the exact same reasons as `Player` the `setPosition` method from `Entity` needs to be updated to 

```java
@Override
public void setPosition(Vec2d position) {
    lastContactFreePosition = this.position;
    super.setPosition(position);
    collider.setPosition(position);
}
```

The `render` method from `Entity` is also not particularly interesting. We simply use the `CircleCollider` to determine where to render the `Ball` to the `Canvas`.

```java
@Override
public void render(Canvas canvas) {
    GraphicsContext ctx = canvas.getGraphicsContext2D();

    ctx.setFill(Color.BLUE);
    double centerX = collider.getCenter().getX();
    double centerY = collider.getCenter().getY();
    double r = collider.getRadius();
    ctx.fillOval(
            (centerX - r) * canvas.getWidth(),
            (centerY - r) * canvas.getHeight(),
            (2*r) * canvas.getWidth(),
            (2*r) * canvas.getHeight()
    );
}
```

The interesting portion of this class is when the `Ball` collides with another object. What the ball does when the `CircleCollider` comes into contact with a `RectangleCollider` is going to be different from the case when the `CircleCollider` comes into contact with a `CircleCollider`. Therefore we break up the `onCollision` code into cases.

![Reflection](document_assets/reflection.png)

When the `Ball` collides with another object, we want it to reflect off the surface. In general, if we know the surface normal and the velocity of the object, we can compute the new velocity of the object after reflecting off the surface. Refer to the figure above. Let $\mathbf{v}$ denote the velocity of the object and let $\mathbf{n}$ be the surface normal. The new velocity, which is the reflection is simply

$$
\mathbf{v} - 2 \cdot \text{proj}_{\mathbf{n}}{\mathbf{v}}
$$

In the case of the `Ball` colliding with a `RectangleCollider`, let $\mathbf{c}$ be the point on the rectangle closest to the `Ball`'s center $\mathbf{x}$. We will say that the normal vector is $\mathbf{n} = \mathbf{c} - \mathbf{x}$. However if the `Ball` with center $\mathbf{x}$ collides with a `CircleCollider` with center $\mathbf{c}$, we will say that the normal vector is $\mathbf{n} = \mathbf{c} - \mathbf{x}$. 

We can now compute the reflection and then update the velocity. See the code below as a reference for how such a calculation might be implemented.

```java
@Override
public void onCollision(Entity other, Collider otherCollider) {
    setPosition(lastContactFreePosition);
    lastContactFreePosition = this.position;
    if (otherCollider instanceof RectangleCollider) {
        RectangleCollider rectangleCollider = 
                        (RectangleCollider) otherCollider;
        Vec2d contactPoint = rectangleCollider
                            .findClosestPoint(collider.getCenter());
        Vec2d normal = collider.getCenter().sub(contactPoint);

        this.velocity = this.velocity
                        .add(
                            this.velocity.projectOnto(normal).scale(-2.0)
                        );
    } else if (otherCollider instanceof CircleCollider) {
        CircleCollider otherCircleCollider = 
                        (CircleCollider) otherCollider;
        Vec2d normal = collider
                        .getCenter()
                        .sub(otherCircleCollider.getCenter());
        this.velocity = this.velocity
                        .add(
                            this.velocity.projectOnto(normal).scale(-2.0)
                        );
    }
}
```

Diagrammatically what we have developed in this section is shown below

![Entity UML Diagram](document_assets/entity_uml_img.png)

<script src="https://polyfill.io/v3/polyfill.min.js?features=es6"></script>
<script id="MathJax-script" async src="https://cdn.jsdelivr.net/npm/mathjax@3/es5/tex-mml-chtml.js"></script>
<script>
window.MathJax = {
  tex: {
    inlineMath: [['$', '$'], ['\\(', '\\)']],
    displayMath: [
        ['$$', '$$'],
        ['\\[', '\\]']
    ],
  }
};
</script>



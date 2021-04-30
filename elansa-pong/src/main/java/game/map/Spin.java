package game.map;

import engine.*;
import game.AbstractLocalGame;
import game.Ball;
import game.Obstacle;
import game.Player;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class Spin extends AbstractLocalGame {
    long resetTime = 3_000_000_000L;
    Long t0 = null;
    Long lastRecordedTime = null;
    Long ballThrowTime = null;

    // Dimensions
    double wallOffset = 0.01;
    double playerLength = 0.1;
    double playerThickness = 0.03;
    double moveSpeed = 0.6;

    double ballMoveSpeed = 0.4;
    double ballRadius = 0.02;

    double killzoneOffset = 0.1;
    double wallThickness = 0.015;

    double blockLength = playerThickness + wallOffset;

    int[] lives = {0, 0, 0, 0};
    int initialLives = 2;

    Obstacle block02 = new Obstacle(
            "Block02",
            new Collider[]{
                    new RectangleCollider(
                            new Vec2d(0.0, 0.0),
                            blockLength,
                            blockLength,
                            0.0
                    )
            },
            new int[]{0, 0, 0},
            true,
            null
    );
    Obstacle block03 = new Obstacle(
            "Block03",
            new Collider[]{
                    new RectangleCollider(
                            new Vec2d(0.0, 1.0 - blockLength),
                            blockLength,
                            blockLength,
                            0.0
                    )
            },
            new int[]{0, 0, 0},
            true,
            null
    );
    Obstacle block12 = new Obstacle(
            "Block12",
            new Collider[]{
                    new RectangleCollider(
                            new Vec2d(1 - blockLength, 0.0),
                            blockLength,
                            blockLength,
                            0.0
                    )
            },
            new int[]{0, 0, 0},
            true,
            null
    );
    Obstacle block13 = new Obstacle(
            "Block13",
            new Collider[]{
                    new RectangleCollider(
                            new Vec2d(1 - blockLength, 1 - blockLength),
                            blockLength,
                            blockLength,
                            0.0
                    )
            },
            new int[]{0, 0, 0},
            true,
            null
    );

    Obstacle[] walls = {
            new Obstacle(
                    "Wall0",
                    new Collider[]{
                            new RectangleCollider(
                                    new Vec2d(0.0, 0.0),
                                    wallThickness,
                                    1.0,
                                    0.0
                            )
                    },
                    new int[]{0, 0, 0},
                    true,
                    null
            ),
            new Obstacle(
                    "Wall1",
                    new Collider[]{
                            new RectangleCollider(
                                    new Vec2d(1 - wallThickness, 0.0),
                                    wallThickness,
                                    1.0,
                                    0.0
                            )
                    },
                    new int[]{0, 0, 0},
                    true,
                    null
            ),
            new Obstacle(
                    "Wall2",
                    new Collider[]{
                            new RectangleCollider(
                                    new Vec2d(0.0, 0.0),
                                    1.0,
                                    wallThickness,
                                    0.0
                            )
                    },
                    new int[]{0, 0, 0},
                    true,
                    null
            ),
            new Obstacle(
                    "Wall3",
                    new Collider[]{
                            new RectangleCollider(
                                    new Vec2d(0.0, 1 - wallThickness),
                                    1.0,
                                    wallThickness,
                                    0.0
                            )
                    },
                    new int[]{0, 0, 0},
                    true,
                    null
            )
    };
    Obstacle[] killzones = {
            new Obstacle(
                    "Player0 Killzone",
                    new Collider[]{
                            new RectangleCollider(
                                    new Vec2d(-killzoneOffset, -10),
                                    killzoneOffset / 2,
                                    20,
                                    0.0
                            )
                    },
                    new int[]{0, 0, 0},
                    false,
                    new CollisionEventHandler() {
                        @Override
                        public void handleCollision(Entity other, Collider otherCollider) {
                            if (other instanceof Ball) {
                                Spin.this.deductLife(0);
                            }
                        }
                    }
            ),
            new Obstacle(
                    "Player1 Killzone",
                    new Collider[]{
                            new RectangleCollider(
                                    new Vec2d(1.0 + killzoneOffset / 2, -10),
                                    killzoneOffset / 2,
                                    20,
                                    0.0
                            )
                    },
                    new int[]{0, 0, 0},
                    false,
                    new CollisionEventHandler() {
                        @Override
                        public void handleCollision(Entity other, Collider otherCollider) {
                            if (other instanceof Ball) {
                                Spin.this.deductLife(1);
                            }
                        }
                    }
            ),
            new Obstacle(
                    "Player2 Killzone",
                    new Collider[]{
                            new RectangleCollider(
                                    new Vec2d(-10, -killzoneOffset),
                                    20,
                                    killzoneOffset / 2,
                                    0.0
                            )
                    },
                    new int[]{0, 0, 0},
                    false,
                    new CollisionEventHandler() {
                        @Override
                        public void handleCollision(Entity other, Collider otherCollider) {
                            if (other instanceof Ball) {
                                Spin.this.deductLife(2);
                            }
                        }
                    }
            ),
            new Obstacle(
                    "Player3 Killzone",
                    new Collider[]{
                            new RectangleCollider(
                                    new Vec2d(-10, 1.0 + killzoneOffset / 2),
                                    20,
                                    killzoneOffset / 2,
                                    0.0
                            )
                    },
                    new int[]{0, 0, 0},
                    false,
                    new CollisionEventHandler() {
                        @Override
                        public void handleCollision(Entity other, Collider otherCollider) {
                            if (other instanceof Ball) {
                                Spin.this.deductLife(3);
                            }
                        }
                    }
            )
    };

    Vec2d[] spinnerOffsets = new Vec2d[]{
        new Vec2d(0.1, 0.0),
        new Vec2d(0.0, 0.25)
    };
    double[] spinnerAngularVelocity = new double[]{
        2*Math.PI / 4,
        2*Math.PI / 6,
    };
    Obstacle[] spinners = new Obstacle[]{
        new Obstacle(
            "Spinner0",
            new Collider[]{
                    new CircleCollider(
                            new Vec2d(0.5, 0.5).add(spinnerOffsets[0]),
                            0.05),
            },
            new int[]{0, 0, 0},
            true,
            null
        ),
        new Obstacle(
            "Spinner2",
            new Collider[]{
                    new RectangleCollider(
                            new Vec2d(0.5, 0.5).add(spinnerOffsets[1]),
                            0.05,
                            0.05,
                            Math.PI/4
                    ),
            },
            new int[]{0, 0, 0},
            true,
            null
        ),
    };

    Ball ball = new Ball(
    "Ball",
        new Vec2d(0.5, 0.5),
        ballRadius
    );

    Player[] initialPlayers = new Player[]{
            new Player(
                "Player0",
                new Vec2d(0.0 + wallOffset, 0.5 - playerLength / 2),
                playerThickness,
                playerLength,
                new Vec2d(0.0, 1.0),
                65,
                81,
                moveSpeed
            ),
            new Player(
                "Player1",
                new Vec2d(1.0-playerThickness-wallOffset, 0.5 - playerLength / 2),
                playerThickness,
                playerLength,
                new Vec2d(0.0, 1.0),
                222,
                91,
                moveSpeed
            ),
            new Player(
                "Player2",
                new Vec2d(0.5 - playerLength / 2, 0.0 + wallOffset),
                playerLength,
                playerThickness,
                new Vec2d(1.0, 0.0),
                86,
                67,
                moveSpeed
            ),
            new Player(
                "Player3",
                new Vec2d(0.5 - playerLength / 2, 1.0-playerThickness-wallOffset),
                playerLength,
                playerThickness,
                new Vec2d(1.0, 0.0),
                44,
                77,
                moveSpeed
            )
    };

    public Spin() {
        resetPlayers();
        dynamicEntities.add(ball);
        dynamicEntities.addAll(Arrays.asList(spinners));
    }

    private void resetPlayers() {
        for (int i = 0; i < players.length; i++) {
            players[i] = (Player) initialPlayers[i].clone();
        }
    }

    private int determineWinner() {
        // Check if there is one active player
        int res = -1;
        for (int i = 0; i < activePlayers.length; i++) {
            if (activePlayers[i]) {
                if (res == -1) {
                    res = i;
                } else {
                    // Found another active player
                    return -1;
                }
            }
        }
        return res;
    }

    @Override
    protected void deductLife(int playerNumber) {
        resetGame();
        lives[playerNumber] -= 1;
        if (lives[playerNumber] == 0) {
            deactivatePlayer(playerNumber);
            gameEventHandler.onPlayerElimination(playerNumber);
        }
        gameEventHandler.onLifeChange(lives, activePlayers);
        // Winner has been found pick only player with nonzero lives
        int winner = determineWinner();
        if (winner != -1) {
            gameEventHandler.onWinnerDetermined(winner);
        }
    }

    @Override
    protected void updateEntitiesList() {
        entities = new ArrayList<>();
        entities.addAll(staticEntities);
        entities.addAll(dynamicEntities);

        // Add players and killzones
        for (int playerNumber = 0; playerNumber < players.length; playerNumber++) {
            if (activePlayers[playerNumber]) {
                entities.add(players[playerNumber]);
                entities.add(killzones[playerNumber]);
            }
        }
    }

    private void updatePlayerAreas() {
        staticEntities = new ArrayList<>();

        // Add wall where there is not player
        for (int playerNumber = 0; playerNumber < players.length; playerNumber++) {
            if (!activePlayers[playerNumber]) {
                staticEntities.add(walls[playerNumber]);
            }
        }

        // Add blocks
        if (activePlayers[0] && activePlayers[2]) {
            staticEntities.add(block02);
        }
        if (activePlayers[0] && activePlayers[3]) {
            staticEntities.add(block03);
        }
        if (activePlayers[1] && activePlayers[2]) {
            staticEntities.add(block12);
        }
        if (activePlayers[1] && activePlayers[3]) {
            staticEntities.add(block13);
        }

        updateEntitiesList();
    }

    @Override
    public void activatePlayer(int playerNumber, boolean automated) {
        lives[playerNumber] = initialLives;
        this.activePlayers[playerNumber] = true;
        this.automatedPlayers[playerNumber] = automated;
        updatePlayerAreas();
    }

    @Override
    public void deactivatePlayer(int playerNumber) {
        this.activePlayers[playerNumber] = false;
        this.automatedPlayers[playerNumber] = false;
        updatePlayerAreas();
    }

    @Override
    public void resetGame() {
        resetPlayers();
        ball.setPosition(new Vec2d(0.5, 0.5));
        ball.setVelocity(new Vec2d(0.0, 0.0));
        ballThrowTime = lastRecordedTime + resetTime;
    }

    @Override
    public void updateState(long currentTime) {
        double deltaTime = 0.0;
        if (lastRecordedTime != null) {
            deltaTime = (double) (currentTime - lastRecordedTime) / 1000000000.0;
        } else {
            // The game just started
            ballThrowTime = currentTime + resetTime;
            t0 = currentTime;
        }
        double elapsedTime = (currentTime - t0) / 1000000000.0;
        lastRecordedTime = currentTime;

        // Throw ball
        if (ball.getVelocity().mag() < Math.ulp(1.0) && currentTime > ballThrowTime) {
            ball.setVelocity(
                new Vec2d(ballMoveSpeed, 0.0).rotate(new Random().nextDouble() * 2 * Math.PI)
            );
        }

        // Bring ball back if it exploded out of the game
        if (ball.getPosition().sub(new Vec2d(0.5, 0.5)).mag() > 2 ||
                !Double.isFinite(ball.getPosition().getX()) ||
                !Double.isFinite(ball.getPosition().getY())
        ) {
            ball.setPosition(new Vec2d(0.5, 0.5));
            ball.setVelocity(
                    new Vec2d(ballMoveSpeed, 0.0).rotate(new Random().nextDouble() * 2 * Math.PI)
            );
        }

        // Update automated players
        ArrayList<Ball> balls = new ArrayList<>();
        balls.add(ball);
        for (int i = 0; i < automatedPlayers.length; i++) {
            if (automatedPlayers[i]) {
                players[i].setDirectionAutomatically(balls);
            }
        }

        // Update spinners
        for (int i = 0; i < spinners.length; i++) {
            // Each spinner obstacle has 1 collider
            spinners[i]
                    .getColliders()
                    .get(0)
                    .setPosition(
                        new Vec2d(0.5, 0.5).add(spinnerOffsets[i].rotate(elapsedTime * spinnerAngularVelocity[i]))
                    );
        }

        // Progress Animation
        for (Entity entity : entities) {
            entity.setPosition(
                entity.getPosition().add(entity.getVelocity().scale(deltaTime))
            );
        }

        // Handle Collisions
        for (int i = 0; i < entities.size()-1; i++) {
            for (int j = i+1; j < entities.size(); j++) {
                Entity entity1 = entities.get(i);
                Entity entity2 = entities.get(j);
                Collider[] colliders = entity1.collidesWith(entity2);
                if (colliders != null) {
                    entity1.onCollision(entity2, colliders[1]);
                    entity2.onCollision(entity1, colliders[0]);
                }
            }
        }

        updateEntitiesList();
    }

    @Override
    public void onKeyPressed(KeyEvent e) {
        for (Player player : players) {
            player.setDirectionKeyPress(e.getCode().getCode());
        }
    }

    @Override
    public void onKeyReleased(KeyEvent e) {
        for (Player player : players) {
            player.setDirectionKeyRelease(e.getCode().getCode());
        }
    }

    @Override
    public void render(Canvas canvas) {
        GraphicsContext ctx = canvas.getGraphicsContext2D();
        ctx.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        for (Entity entity : entities) {
            entity.render(canvas);
        }
    }
}

package game;

import engine.*;
import netcode.packets.*;

import java.util.ArrayList;

public class GameSettings {
    public static final Class<?>[] SERIALIZABLE_WHITELIST = {
            // Built-in Java Classes
            ArrayList.class,
            int[].class,
            boolean[].class,

            // Engine Classes
            CircleCollider.class,
            Collider.class,
            CollisionEventHandler.class,
            Entity.class,
            RectangleCollider.class,
            Vec2d.class,

            // Game-specific Classes
            Ball.class,
            Obstacle.class,
            Player.class,

            // Networking Packets
            Connect.class,
            GameOver.class,
            LivesUpdate.class,
            Packet.class,
            PlayerAssignment.class,
            PlayerEliminated.class,
            PlayerInput.class,
            Ready.class,
            Synchronization.class
    };
    public static final String BALL_BONK_AUDIO = "resources/bonk.wav";
    public static final String BELL_AUDIO = "resources/bell.wav";
    public static boolean SOUND_EFFECTS_ON = true;
}

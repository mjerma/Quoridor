package hr.algebra.model;

public enum PlayerColor {
    RED, BLUE;

    public static PlayerColor getPlayerColor(String token)
    {
        return PlayerColor.valueOf(token);
    }
    public static String getName(PlayerColor color) { return color.name(); }
}

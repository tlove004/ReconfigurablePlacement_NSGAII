package misc;

/**
 * Created by Tyson Loveless
 *
 * Basic pair class for placement of operations
 *
 * @param <x>
 * @param <y>
 */
public class Location<x, y> {

    private final x x_coord;
    private final y y_coord;

    public Location(x x_coord, y y_coord) {
        this.x_coord = x_coord;
        this.y_coord = y_coord;
    }

    public x getX() { return x_coord; }
    public y getY() { return y_coord; }

    @Override
    public int hashCode() { return x_coord.hashCode() ^ y_coord.hashCode(); }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Location)) return false;
        Location loco = (Location) o;
        return this.x_coord.equals(loco.getX()) &&
                this.y_coord.equals(loco.getY());
    }


    @Override
    public String toString() {
        return (String.valueOf(getX()) + "\t" + String.valueOf(getY()));
    }

}

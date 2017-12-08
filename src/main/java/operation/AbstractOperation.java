package operation;

import misc.Location;

/**
 * Created by Tyson Loveless
 *
 * Use this class to build new operations
 */
public abstract class AbstractOperation implements Operation {
    private int ID;
    private int type;
    private OperationSize<Integer, Integer> size;
    private Location<Integer, Integer> placement;

    public AbstractOperation(int ID, int type) {
        this.ID = ID;
        this.type = type;
    }

    public AbstractOperation(int ID, int type, OperationSize<Integer, Integer> size) {
        this.ID = ID;
        this.type = type;
        this.size = size;
    }

    @Override
    public int getID() {
        return ID;
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public OperationSize<Integer, Integer> getSize() {
        return size;
    }

    public Location<Integer, Integer> getPlacement() {
        return placement;
    }

    @Override
    public void setPlacement(int x, int y) {
        placement = new Location<>(x, y);
    }

    public void setSize(int height, int width) {
        this.size = new OperationSize<>(height, width);
    }

    @Override
    public void rotate() {
        int height = this.getSize().getHeight();
        int width = this.getSize().getWidth();
        this.setSize(width, height);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getType() + "\t");
        if (getPlacement() != null) {
            sb.append(getPlacement().toString());
        }
        return sb.toString();
    }
}

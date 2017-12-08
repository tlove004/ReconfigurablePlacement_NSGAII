package operation;

/**
 * Created by Tyson Loveless
 */
public interface Operation {
    int getID();
    int getType();
    OperationSize<Integer, Integer> getSize();
    void setPlacement(int x, int y);
    void rotate();
}

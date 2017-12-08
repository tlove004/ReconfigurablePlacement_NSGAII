package operation;

/**
 * Created by Tyson Loveless
 */
public class Sense extends AbstractOperation {

    public Sense(int ID) {
        super(ID, 7, new OperationSize<>(1, 1));
    }
}

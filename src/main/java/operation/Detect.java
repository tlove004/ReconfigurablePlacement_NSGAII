package operation;

/**
 * Created by Tyson Loveless
 */
public class Detect extends AbstractOperation {

    public Detect(int ID) {
        super(ID, 8, new OperationSize<>(1, 1));
    }
}

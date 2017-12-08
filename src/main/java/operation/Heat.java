package operation;

/**
 * Created by Tyson Loveless
 */
public class Heat extends AbstractOperation {

    public Heat(int ID) {
        super(ID, 9, new OperationSize<>(1, 1));
    }
}

package operation;

/**
 * Created by Tyson Loveless
 */
public class Split extends AbstractOperation {

    public Split(int ID) {
        super(ID, 4, new OperationSize<>(1, 3));
    }

}

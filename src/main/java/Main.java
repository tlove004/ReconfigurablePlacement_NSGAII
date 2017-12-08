import misc.Parser;
import misc.Placement;
import operation.Mix;
import operation.Operation;
import operation.OperationSize;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.moeaframework.Executor;
import org.moeaframework.core.*;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by Tyson on 2017/12/04
 */
public class Main {
    //public static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String... args) throws Exception {

        //logger.info("Logger starting...");

        //parse input files
        Parser parser = new Parser(args);

        //will need these later
        List<Operation> operations = parser.getOperations();
        double alpha = parser.getAlpha();

        //get count of special operations and io ops to set up problem instance
        int num_special_ops = 0;
        int num_io = 0;
        for (Operation op : operations) {
            if (op.getType() == 1 || op.getType() == 2) {
                num_io++;
            }
            if (op.getType() >= 7) {
                num_special_ops++;
            }
        }

        try {
            //set up problem instance
            DMFB_ReconfigPlacementProblem reconfigPlacement = new DMFB_ReconfigPlacementProblem(
                    parser.getArch(),
                    parser.getOperations(),
                    parser.getInterferenceGraph(),
                    parser.getCommunicationGraph(),
                    parser.getAlpha(),
                    num_special_ops,
                    num_io);

            //pass problem instance to NSGAII
            NondominatedPopulation placement = new Executor()
                    .withProblem(reconfigPlacement)
                    .withAlgorithm("NSGAII")
                    .withMaxEvaluations(25000)
                    .run();



            boolean success = true;
            //logger.info("Objective:");
            System.out.print("Final objective value: ");
            for (Solution solution : placement) {
                if (solution.violatesConstraints()) {
                    //logger.warn("no solution found");
                    System.out.print("no solution found\n");
                    success = false;
                    int i = 0;
                    for (double c : solution.getConstraints()) {
                        //logger.info(i++ + ": " + c);
                        System.out.print(i++ + ": " + c);
                    }
                }
                else {
                    //logger.info(solution.getObjective(0));
                    System.out.print(solution.getObjective(0) + "\n");
                }
            }

            //if we have found a solution:
            if (success) {
                HashSet<Placement> placements = reconfigPlacement.getPlacements();

                //find best placement solution matching best objective function result
                Iterator<Placement> it = placements.iterator();
                Placement solution = null;
                Placement p;
                while (it.hasNext()) {
                    p = it.next();
                    for (Solution s : placement) {
                        if (p.getObj() == s.getObjective(0)) {
                            solution = p;
                            break;
                        }
                    }
                    if (solution == null) {
                        solution = p;
                    }
                }

                //this is unnecessary, but the objects felt incomplete, so here I update the operations with their
                //   placements, sizes, and orientations
                OperationSize size;
                for (Operation op : operations) {
                    op.setPlacement((int) solution.getPlacement().get(op.getID() - 1).getX(), (int) solution.getPlacement().get(op.getID() - 1).getY());
                    if (op.getType() == 3) {
                        size = solution.getMix_sizes().get(op.getID() - 1);
                        ((Mix) op).setSize((int) size.getHeight(), (int) size.getWidth());
                    }
                    if (op.getType() >= 3 && op.getType() <= 5) {
                        if (solution.getOrientations().get(op.getID() - 1) == 1) {
                            op.rotate();
                        }
                    }
                }

                //formatting output file
                List<String> lines = new ArrayList<>();
                lines.add(String.valueOf(solution.getObj()) + "\t" +
                        String.valueOf(solution.getD_comm()) + "\t" +
                        String.valueOf(solution.getT_mix()) + "\t" +
                        String.valueOf(alpha));
                for (Operation operation : operations) {
                    lines.add(operation.toString());
                }
                Path file = Paths.get("placement.out");
                Files.write(file, lines, Charset.forName("UTF-8"));
            }
            // otherwise (no solution found)
            else {
                Path file = Paths.get("placement.out");
                Files.write(file, Collections.singletonList("No solution found."), Charset.forName("UTF-8"));
            }

        } catch (Exception e) {
            //logger.error(e.getMessage());
            System.out.print(e.getMessage());
        }
    }
}
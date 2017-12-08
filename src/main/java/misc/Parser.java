package misc;
import graph.*;
import operation.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.*;

//String archFile, String opsFile, String graphFile, String alphaFile

/**
 *
 */
public class Parser {


    //private final static Logger logger = LogManager.getLogger(Parser.class);

    private Architecture arch;
    private List<Operation> operations;
    private Graph interferenceGraph;
    private Graph communicationGraph;
    private double alpha;

    /**
     *
     * @param files
     * @throws FileNotFoundException
     */
    public Parser(String... files) throws FileNotFoundException {

        BufferedReader reader = null;
        File file;
        FileReader filereader;
        try {
            for (String filename : files) {
                file = new File(this.getClass().getClassLoader().getResource(filename).getFile());
                //file = new File(filename);
                filereader = new FileReader(file);
                reader = new BufferedReader(filereader);

                if (filename.contains("arch.in")) {
                    System.out.print("\nConfiguring architecture...");
                    parseArchFile(reader);
                    //logger.debug("\nArchitecture parsed:\n" + getArch().toString() + "\n");
                    System.out.print("....Done!.....\n\n");
                }
                else if (filename.contains("ops.in")) {
                    System.out.print("Reading assay...");
                    parseOpsFile(reader);
                    //logger.debug("\nOperations parsed:\n" + getOperations().toString() + "\n");
                    System.out.print("....Done!.....\n\n");
                }
                else if (filename.contains("graphs.in")) {
                    System.out.print("Looking for interference and dependencies...");
                    parseGraphsFile(reader);
                    //logger.debug("\nGraphs parsed:\n" +
                    //        "Interference Graph:\n" + getInterferenceGraph().toString() +
                    //        "\nCommunication Graph:\n" + getCommunicationGraph().toString() + "\n");
                    System.out.print("....Done!.....\n\n");
                }
                else if (filename.contains("alpha.in")) {
                    System.out.print("Configuring objective...");
                    parseAlphaFile(reader);
                    //logger.debug("\nAlpha parsed:\n" + getAlpha() + "\n");
                    System.out.print("....Done!.....\n\n");
                }
                else {
                    throw new FileNotFoundException("File: \"" + filename + "\" not found.");
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    /**
     *
     * @param reader
     * @param num_locations
     * @return
     * @throws IOException
     */
    private Vector<Location<Integer, Integer>> parseLocations(BufferedReader reader, int num_locations) throws IOException {
        Vector<Location<Integer, Integer>> locations = new Vector<Location<Integer, Integer>>(num_locations);
        String line;
        String[] strings;
        for (int i = 0; i < num_locations; i++) {
            line = reader.readLine();

            //expecting "(x, y)"
            //List<Integer> loc = new ArrayList<Integer>(Integer.parseInt(line));
            strings = line.split("\\s+");

            Integer x = Integer.parseInt(strings[0]);
            Integer y = Integer.parseInt(strings[1]);
            Location<Integer, Integer> location = new Location<Integer, Integer>(x, y);
            locations.add(i, location);
        }
        return locations;
    }

    private List<Edge<Operation, Operation>> parseEdges(BufferedReader reader, int num_edges, boolean costs) throws IOException {
        List<Edge<Operation, Operation>> edges = new LinkedList<Edge<Operation, Operation>>();
        String line;
        String[] strings;
        Edge<Operation, Operation> edge;
        if (costs) {
            Map<Edge<Operation, Operation>, Integer> costMap = new HashMap<Edge<Operation, Operation>, Integer>();
        }
        for (int i = 0; i < num_edges; i++) {
            line = reader.readLine();
            strings = line.trim().split("\\s+");
            //expecting "op1 op2"
            Operation op1 = operations.get(Integer.parseInt(strings[0])-1);
            Operation op2 = operations.get(Integer.parseInt(strings[1])-1);

            if (costs) {
                Integer cost = Integer.parseInt(strings[2]);
                edge = new Edge<Operation, Operation>(op1, op2, cost);
            }
            else {
                edge = new Edge<Operation, Operation>(op1, op2);
            }
            edges.add(edge);
        }
        return edges;
    }

    /**
     *
     * @param reader
     * @throws IOException
     */
    private void parseArchFile(BufferedReader reader) throws IOException {
        try {
            String line;
            line = reader.readLine();
            String[] strings = line.trim().split("\\s+");

            //num rows
            int m = Integer.parseInt(strings[0]);
            //num columns
            int n = Integer.parseInt(strings[1]);

            line = reader.readLine();
            strings = line.trim().split("\\s+");

            // num inputs
            int num_inputs = Integer.parseInt(strings[0]);
            // num outputs
            int num_outputs = Integer.parseInt(strings[1]);

            line = reader.readLine();
            strings = line.trim().split("\\s+");

            // num sensors
            int num_sensors = Integer.parseInt(strings[0]);
            // num detectors
            int num_detectors = Integer.parseInt(strings[1]);
            // num heaters
            int num_heaters = Integer.parseInt(strings[2]);


            //get all input locations
            Vector<Location<Integer, Integer>> inputs = parseLocations(reader, num_inputs);

            //get all output locations
            Vector<Location<Integer, Integer>> outputs = parseLocations(reader, num_outputs);

            //get all sensor locations
            Vector<Location<Integer, Integer>> sensors = parseLocations(reader, num_sensors);

            //get all detector locations
            Vector<Location<Integer, Integer>> detectors = parseLocations(reader, num_detectors);

            //get all heater locations
            Vector<Location<Integer, Integer>> heaters = parseLocations(reader, num_heaters);

            this.arch = new Architecture(m, n, inputs, outputs, sensors, detectors, heaters);

        } catch (IOException e) {
            throw e;
        }
    }

    private void parseOpsFile(BufferedReader reader) throws IOException {
        String line;
        line = reader.readLine();
        String[] strings = line.trim().split("\\s*");

        //num ops
        int num_ops = Integer.parseInt(strings[0]);
        int res;
        operations = new LinkedList<Operation>();
        Operation op;
        for (int i = 1; i < num_ops+1; i++) {
            line = reader.readLine();
            strings = line.trim().split("\\s+");
            //operations should match: <type> <op specific stuff>
            op = null;
            switch (Integer.parseInt(strings[0])) { //type of operation
                case 1: //input
                    res = Integer.parseInt(strings[1]);
                    op = new Input(i, res);
                    ((AbstractOperation)op).setPlacement(getArch().getInputs().get(res-1).getX(), getArch().getInputs().get(res-1).getY());
                    break;
                case 2: //output
                    res = Integer.parseInt(strings[1]);
                    op = new Output(i, res);
                    ((AbstractOperation)op).setPlacement(getArch().getOutputs().get(res-1).getX(), getArch().getOutputs().get(res-1).getY());
                    break;
                case 3: //mix
                    op = new Mix(i, Double.parseDouble(strings[1]));
                    break;
                case 4: // split
                    op = new Split(i);
                    break;
                case 5: // merge
                    op = new Merge(i);
                    break;
                case 6: // store
                    op = new Store(i);
                    break;
                case 7: // sense
                    op = new Sense(i);
                    break;
                case 8: // detect
                    op = new Detect(i);
                    break;
                case 9: // heat
                    op = new Heat(i);
                    break;
                default:
                    System.out.print("Invalid operation type");
            }
            operations.add(op);
        }
    }

    private void parseGraphsFile(BufferedReader reader) throws IOException {
        String line;
        line = reader.readLine();
        String[] strings = line.trim().split("\\s+");

        // num vertices (operations)
        int num_vertices = Integer.parseInt(strings[0]);

        // num edges in interference graph
        int num_int_edges = Integer.parseInt(strings[1]);

        // num edges in communication graph
        int num_comm_edges = Integer.parseInt(strings[2]);

        //get interference graph edges
        List<Edge<Operation, Operation>> edges = parseEdges(reader, num_int_edges, false);
        //build interference graph
        interferenceGraph = new Graph(operations, new LinkedList<>(edges));

        edges.clear();

        // get communication graph edges and costs
        edges = parseEdges(reader, num_comm_edges, true);
        //build communication graph
        communicationGraph = new Graph(operations, new LinkedList<>(edges));

    }

    private void parseAlphaFile(BufferedReader reader) throws IOException {
        String line = reader.readLine();
        alpha = Double.parseDouble(line);
    }

    public Architecture getArch() {
        return arch;
    }

    public List<Operation> getOperations() {
        return operations;
    }

    public Graph getInterferenceGraph() {
        return interferenceGraph;
    }

    public Graph getCommunicationGraph() {
        return communicationGraph;
    }

    public double getAlpha() {
        return alpha;
    }
}

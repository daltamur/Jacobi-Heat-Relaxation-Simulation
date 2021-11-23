import javax.swing.*;
import java.util.ArrayList;
import java.util.concurrent.CountedCompleter;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.atomic.AtomicInteger;

public class heatPropogationJacobi implements constants{
    public static JFrame frame;
    public static AtomicInteger lines = new AtomicInteger(0);
    public static int leafComputations = 0;

    public static void main(String[] args) throws InterruptedException {
        int cores = Runtime.getRuntime().availableProcessors();
        Double S = 70.0;
        Double T =90.0;
        region[][] a = new region[500][1000];
        region[][] b = new region[500][1000];
        for(int i = 0; i<500; i++){
            for(int j = 0; j<1000; j++){
                a[i][j] = new region(false);
                b[i][j] = new region(false);
            }
        }

        engine engine = new engine(a, b, 0, 499, 0, 999, 1);
        long sTime = System.currentTimeMillis();
        engine.invoke();
        System.out.println(a[0][0].getCurrentTemp());
        long time = System.currentTimeMillis() - sTime;
        double secs = ((double)time) / 1000.0;
        System.out.println("Took "+secs+" seconds");

        lines.getAndSet(0);
        sTime = System.currentTimeMillis();
        for(int i = 0; i<500; i++){
            for(int j = 0; j<500; j++){
                a[i][j].changeTemp(a[i][j].getCurrentTemp()+15.45454);
                lines.getAndAdd(1);
            }
        }
        time = System.currentTimeMillis() - sTime;
        secs = ((double)time) / 1000.0;
        System.out.println("Took "+secs+" seconds");
        System.out.println(lines);
    }


    abstract static class MeshSection extends CountedCompleter<Void>{
        //all mesh sections must hold a value of the maximum difference among the separate sections
        double maxDifference;
        MeshSection(CountedCompleter<?> counter, int c) {super(counter, c);}
    }

    static class fourQuad extends MeshSection{
        //reference to the four quadrants of the current mesh section
        MeshSection q1;
        MeshSection q2;
        MeshSection q3;
        MeshSection q4;

        //childt of MeshSection, so needs to use its super constructor
        fourQuad(CountedCompleter<?> counter){
            super(counter, 3);
        }

        //when we complete all the forks for each quadrant, find the maximum difference
        @Override
        public void onCompletion(CountedCompleter<?> caller) {
            double maxDif = q1.maxDifference;
            //this value will be the one we compare against
            double m;
            if((m = q2.maxDifference) > maxDif){
                maxDif = m;
            }
            if((m = q3.maxDifference) > maxDif){
                maxDif = m;
            }
            if((m = q4.maxDifference) > maxDif){
                maxDif = m;
            }
            //after going through the tests, make the final maximum difference equal to whoever the greatest maxDif is
            maxDifference = maxDif;
            //reset the counted completer in case we have to do the computations again
            setPendingCount(3);
        }

        @Override
        public void compute() {
            //fork each quadrant
            q4.fork();
            q3.fork();
            q2.fork();
            q1.compute();
        }
    }

    static class Leaf extends MeshSection{
        private final region[][] A; //values from old matrix
        private final region[][] B; //values from new matrix

        //the matrix we swap from one to the other will change based on if the current step is even or odd
        private final int lowRow;
        private final int lowColumn;
        private final int highColumn;
        private final int highRow;
        private int steps = 0;



        Leaf(CountedCompleter<?> counter, region[][] a, region[][] b,
             int lowRow, int highRow,
             int lowColumn, int highColumn) {
            super(counter, 0);
            A = a;
            B = b;
            this.lowRow = lowRow;
            this.lowColumn = lowColumn;
            this.highColumn = highColumn;
            this.highRow = highRow;
        }

        @Override
        public void compute() {
            leafComputations++;
            //AtoB checks if the current step is an even or odd number. If it is even, it'll be 0. Otherwise it'll be 1 or smth I think
            //note that the steps always start at 0, so it will always be from A to B at first
            boolean AtoB = (steps++ % 2) == 0;
            // if the current step is even, the array we are taking values from will be A, otherwise it will be B.
            region[][] a = (AtoB)? A : B;
            // if the current step is even, the array we are putting values into will be B, otherwise it will be A.
            region[][] b = (AtoB)? B : A;

            double currentMaxDif = 0;
            for(int i = lowRow; i <= highRow; i++){
                for(int j = lowColumn; j <= highColumn; j++){

                }
            }
            maxDifference = currentMaxDif;
            //let the quadrant that this leaf belongs to know that it is done computing
            tryComplete();
        }
    }

    static class engine extends RecursiveAction{
        //the root mesh
        private MeshSection root;
        //the two mesh regions
        private region[][] A;
        private region[][] B;
        private int firstRow;
        private int lastRow;
        private int firstColumn;
        private int lastColumn;
        private int steps = 0;
        private final int granularity;
        int numberOfSegments = 0;
        int numOfZeroes = 0;

        public engine(region[][] A, region[][] B, int firstRow, int lastRow, int firstColumn, int lastColumn, int granularity) throws InterruptedException {
            this.A = A;
            this.B = B;
            this.firstRow = firstRow;
            this.lastRow = lastRow;
            this.firstColumn = firstColumn;
            this.lastColumn = lastColumn;
            this.granularity = granularity;
            root = build(null, this.A, this.B, this.firstRow, this.lastRow, this.firstColumn, this.lastColumn, this.granularity);
            System.out.println("Built using "+ numberOfSegments + " segments");
            System.out.println(numOfZeroes);
        }

        @Override
        protected void compute() {
            int steps = 0;
            while(true){
                //sets the counted completer of the root to handle 4 quadrants running, being that the root is always
                // guaranteed to have four quadrants to run on. It does this for every subsequent run of the computation
                //otherwise we have no reason to run this in parallel if it is small enough where a single leaf is made
                System.out.println(numberOfSegments);
                System.out.println(numOfZeroes);
                root.setPendingCount(3);
                root.invoke();


                if(root.maxDifference < constants.EPSILON){
                    //System.out.println("Converged after " + steps + " steps.");
                    //break;
                }else {
                    //number of steps used goes up
                    steps++;
                    //reset the book keeping for all the forks so that we can run the computation again
                    //root.reinitialize();
                }
                //root.reinitialize();
                break;
            }
        }

        public MeshSection build(MeshSection predecessor, region[][] a, region[][] b, int lr, int hr, int lc, int hc, int granularity){
            //get number of rows
            int rows = (hr - lr + 1);
            //get number of columns
            int cols = (hc - lc + 1);

            //get the midpoint of the columns and rows using the bitshift operation that divides by 2
            int midpointRows = (lr + hr) >>> 1;
            int midpointColumns = (lc + hc) >>> 1;
            //get how many rows and columns each half has
            //int rowAmountMidpoint = (midpointRows - lr +1);
            //int columnAmountMidpoint = (midpointColumns - lc + 1);
            if (rows * cols <= granularity){
                ++numberOfSegments;
                numOfZeroes += rows*cols;
                return new Leaf(predecessor, a, b, lr, hr, lc, hc);
            }else{
                fourQuad quad = new fourQuad(predecessor);
                quad.q1 = build(quad, a, b, lr, midpointRows, lc, midpointColumns, granularity);
                quad.q2 = build(quad, a, b, lr, midpointRows, midpointColumns + 1, hc, granularity);
                quad.q3 = build(quad, a, b, midpointRows + 1, hr, lc, midpointColumns, granularity);
                quad.q4 = build(quad, a, b, midpointRows + 1, hr, midpointColumns + 1, hc, granularity);
                return quad;
            }
        }
    }
}

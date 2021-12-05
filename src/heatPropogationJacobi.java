import javax.swing.*;
import java.awt.*;
import java.util.concurrent.*;

public class heatPropogationJacobi extends JPanel implements constants{
    public static region [][] paintedA;
    public static region [][] paintedB;
    public static boolean startPainting;
    public static boolean paintB;

    @Override
    protected void paintComponent(Graphics g) {
        if(startPainting){
            super.paintComponent(g);
            int curXPos=0;
            int curYPos=0;
             // width will store the width of the screen
            int widthFrame =  frame.getSize().width;

            double widthMesh = Math.ceil((double) widthFrame/(double) paintedA[0].length);

            // height will store the height of the screen
            int heightFrame = frame.getSize().height;
            double heightMesh = Math.ceil((double) heightFrame/(double) paintedA.length);
                    for (int i = 0; i<paintedA.length; i++){
                        for(int j = 0; j<paintedA[0].length; j++){
                                double curTemp = paintedB[i][j].getCurrentTemp();
                                if (curTemp > 255){
                                    curTemp = 255.0;
                                }
                                //use the function y = -x + 255 to get the inverses where x=curTemp
                                curTemp = -curTemp + 255;
                                double hue = curTemp/360.0;

                                g.setColor(Color.getHSBColor((float) hue, 1, 1));

                            g.fillRect(curXPos, curYPos, (int) widthMesh, (int) heightMesh);
                            if(j == paintedA[0].length - 1){
                                curXPos = 0;
                                curYPos += heightMesh;
                            }else{
                                curXPos += widthMesh;
                            }

                        }
                    }
            }
    }


    @Override
    public Dimension getPreferredSize() {
        // so that our GUI is big enough
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();

        // width will store the width of the screen
        int widthV = gd.getFullScreenWindow().getWidth();

        // height will store the height of the screen
        int heightV = gd.getFullScreenWindow().getHeight();
        return new Dimension(widthV, heightV);
    }


    private static void createAndShowGui() {
        heatPropogationJacobi mainPanel = new heatPropogationJacobi();
        mainPanel.setLayout(new BorderLayout());
        JLabel title = new JLabel("Title");
        title.setBackground(Color.black);
        title.setFont(new Font(title.getFont().toString(), Font.BOLD, 48));
        title.setForeground(Color.white);
        title.setText("Heat Propagation Visualization of a Heated Metal Alloy");
        title.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel name = new JLabel("Name");
        name.setBackground(Color.black);
        name.setFont(new Font(title.getFont().toString(), Font.BOLD, 15));
        name.setForeground(Color.white);
        name.setText("Authored by Dominic Altamura");
        mainPanel.add(title, BorderLayout.PAGE_START);
        mainPanel.add(name,BorderLayout.SOUTH);
        GraphicsEnvironment graphics = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice device = graphics.getDefaultScreenDevice();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(mainPanel);
        //show a new floor every fifth of a second
        Timer t = new Timer(10,
                e -> mainPanel.repaint());
        t.start();
        frame.setVisible(true);
        beginProgram = true;
        GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Rectangle bounds = environment.getMaximumWindowBounds();
        System.out.println("Screen Bounds = " + bounds);
        GraphicsConfiguration config = device.getDefaultConfiguration();
        System.out.println("Screen Size = " + config.getBounds());
        System.out.println("Frame Size = " + frame.getSize());

    }

    public static JFrame frame;
    public static boolean beginProgram = false;
    public static double S = 0.0;
    public static double T = 0.0;




    public static void main(String[] args) throws InterruptedException{
        frame = new JFrame("Jacobi Relaxation");
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setUndecorated(true);
        frame.setVisible(true);
        int height = 0;
        int width = 0;

        try {
             height = Integer.parseInt(JOptionPane.showInputDialog(null,"Height of mesh (preferrable if you make the height a divisor of " + frame.getSize().height));
        }catch (Exception e){
            System.out.println(e);
            System.exit(-1);
        }

        try {
           width = Integer.parseInt(JOptionPane.showInputDialog(null,"Width of mesh (preferrable if you make the height a divisor of " + frame.getSize().width));
        }catch (Exception e){
            System.out.println(e);
            System.exit(-1);
        }
        frame.setVisible(false);

        SwingUtilities.invokeLater(heatPropogationJacobi::createAndShowGui);

        while (!beginProgram){
            System.out.println("Waiting on gui initialization");
        }
        runProgram(height, width);
    }

    public static void runProgram(int height, int width) throws InterruptedException {
        region[][] a = new region[height][width];
        region[][] b = new region[height][width];
        S = 1000000;//ThreadLocalRandom.current().nextDouble(100000, 5000000);
        T= 1000000;//ThreadLocalRandom.current().nextDouble(100000, 5000000);

        for(int i = 0; i<a.length; i++){
            for(int j = 0; j<a[0].length; j++){
                double metal1Portion = ThreadLocalRandom.current().nextDouble(0,1);
                double metal2Portion = ThreadLocalRandom.current().nextDouble(0, 1-metal1Portion);
                double metal3Portion = 1 - metal1Portion - metal2Portion;
                //for metal1Percentage, this one should never be a majority value, as it will always bring the temperature down if it is a majority.
                double currentTemp = 45;
                a[i][j] = new region(false, metal1Portion, metal2Portion, metal3Portion, currentTemp);
                b[i][j] = new region(false, metal1Portion, metal2Portion, metal3Portion, currentTemp);
            }
        }
        a[0][0].changeTemp(S);
        a[0][0].setHeatedCorner(true);
        a[a.length-1][a[0].length-1].changeTemp(T);
        a[a.length-1][a[0].length-1].setHeatedCorner(true);
        b[0][0].changeTemp(S);
        b[0][0].setHeatedCorner(true);
        b[b.length-1][b[0].length-1].changeTemp(T);
        b[b.length-1][b[0].length-1].setHeatedCorner(true);
        paintedA = a;
        paintedB = b;
        startPainting = true;
        engine engine = new engine(a, b, 0, a.length-1, 0, a[0].length-1, 150);
        long sTime = System.currentTimeMillis();
        /*

            for (int i = 0; i < a.length; i++) {

                for (int j = 0; j < a[0].length; j++) {
                    System.out.print("|");
                    System.out.print(String.format("%.02f", b[i][j].getCurrentTemp()));
                    System.out.print("|");
                }
                System.out.println();
            }

         */


        engine.invoke();
        long time = System.currentTimeMillis() - sTime;
        double secs = ((double) time) / 1000.0;
        System.out.println("Took " + secs + " seconds");
        engine.reinitialize();

        for (int i = 0; i < a.length; i++) {

            for (int j = 0; j < a[0].length; j++) {
                System.out.print("|");
                System.out.print(String.format("%.02f", b[i][j].getCurrentTemp()));
                System.out.print("|");
            }
            System.out.println();
        }



        /*
        while(true) {
            S = ThreadLocalRandom.current().nextDouble(100000, 5000000);
            T= ThreadLocalRandom.current().nextDouble(100000, 5000000);
            for (int i = 0; i < a.length; i++) {
                for (int j = 0; j < a[0].length; j++) {
                    double currentTemp = 0.0;
                    a[i][j].changeTemp(currentTemp);
                    b[i][j].changeTemp(currentTemp);
                }
            }
            a[0][0].changeTemp(S);
            a[a.length - 1][a[0].length - 1].changeTemp(T);
            b[0][0].changeTemp(S);
            b[b.length - 1][b[0].length - 1].changeTemp(T);
            engine.reset();
            engine.invoke();
            engine.reinitialize();
        }

         */

    }



    abstract static class MeshSection extends RecursiveAction{
        //all mesh sections must hold a value of the maximum difference among the separate sections
        double maxDifference;
        boolean isDone;

        public abstract void reset();
    }

    static class fourQuad extends MeshSection{
        //reference to the four quadrants of the current mesh section
        MeshSection q1;
        MeshSection q2;
        MeshSection q3;
        MeshSection q4;

        //child of MeshSection, so needs to use its super constructor
        fourQuad(){}

        //when we complete all the forks for each quadrant, find the maximum difference



        public void compute() {
            //fork each quadrant
            q4.fork();
            q3.fork();
            q2.fork();
            q1.fork();
            q4.join();
            q3.join();
            q2.join();
            q1.join();
            q1.reinitialize();
            q2.reinitialize();
            q3.reinitialize();
            q4.reinitialize();
            this.maxDifference = q1.maxDifference;
            this.maxDifference = Math.max(this.maxDifference, q2.maxDifference);
            this.maxDifference = Math.max(this.maxDifference, q3.maxDifference);
            this.maxDifference = Math.max(this.maxDifference, q4.maxDifference);
        }

        @Override
        public void reset() {
            q1.reset();
            q2.reset();
            q3.reset();
            q4.reset();
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



        Leaf(region[][] a, region[][] b,
             int lowRow, int highRow,
             int lowColumn, int highColumn) {
            A = a;
            B = b;
            this.lowRow = lowRow;
            this.lowColumn = lowColumn;
            this.highColumn = highColumn;
            this.highRow = highRow;
        }


        public void compute() {
            //AtoB checks if the current step is an even or odd number. If it is even, it'll be 0. Otherwise it'll be 1 or smth I think
            //note that the steps always start at 0, so it will always be from A to B at first
            boolean AtoB = (steps++ % 2) == 0;
            // if the current step is even, the array we are taking values from will be A, otherwise it will be B.
            region[][] a = (AtoB)? A : B;
            // if the current step is even, the array we are putting values into will be B, otherwise it will be A.
            region[][] b = (AtoB)? B : A;

            double currentMaxDif = 0;

            //this.maxDifference = currentMaxDif;

            for(int i = lowRow; i<=highRow; ++i){
                for(int j = lowColumn; j<=highColumn; ++j) {
                    //keep the heated corners constant
                    if(!a[i][j].getIsHeatedCorner()){
                        double numberOfNeighbors = 0.0;
                        //get what neighbors exist
                        boolean topLeft = i-1>=0 && j-1>=0;
                        numberOfNeighbors = (topLeft)? numberOfNeighbors+1.0 : numberOfNeighbors;
                        boolean topRight = i-1>=0 && j+1<=a[0].length-1;
                        numberOfNeighbors = (topRight)? numberOfNeighbors+1.0 : numberOfNeighbors;
                        boolean top = i-1 >=0;
                        numberOfNeighbors = (top)? numberOfNeighbors+1.0 : numberOfNeighbors;
                        boolean bottom = i+1 <=a.length-1;
                        numberOfNeighbors = (bottom)? numberOfNeighbors+1.0 : numberOfNeighbors;
                        boolean left = j-1 >= 0;
                        numberOfNeighbors = (left)? numberOfNeighbors+1.0 : numberOfNeighbors;
                        boolean right = j+1 <= a[0].length-1;
                        numberOfNeighbors = (right)? numberOfNeighbors+1.0 : numberOfNeighbors;
                        boolean bottomLeft = i+1 <=a.length-1 && j-1>=0;
                        numberOfNeighbors = (bottomLeft)? numberOfNeighbors+1.0 : numberOfNeighbors;
                        boolean bottomRight = i+1 <= a.length-1 && j+1 <= a[0].length-1;
                        numberOfNeighbors = (bottomRight)? numberOfNeighbors+1.0 : numberOfNeighbors;
                        double finalNewTemp;
                        double constantSummation = 0.0;
                        //the summation multiply neighbor sums by their thermal constant
                        for(int currentConstantIteration = 0; currentConstantIteration<3; currentConstantIteration++){
                            double neighborSummation = 0.0;
                            double currentConstant;
                            switch (currentConstantIteration){
                                case 0: currentConstant = c1;
                                        break;
                                case 1: currentConstant = c2;
                                        break;
                                case 2: currentConstant = c3;
                                        break;
                                default:
                                    throw new IllegalStateException("Unexpected value: " + currentConstantIteration);
                            }
                            if(top){
                               switch (currentConstantIteration){
                                   case 0: neighborSummation = neighborSummation + a[i-1][j].getCurrentTemp()*a[i-1][j].getMetal1Percentage();
                                       break;
                                   case 1: neighborSummation = neighborSummation + a[i-1][j].getCurrentTemp()*a[i-1][j].getMetal2Percentage();
                                       break;
                                   case 2: neighborSummation = neighborSummation + a[i-1][j].getCurrentTemp()*a[i-1][j].getMetal3Percentage();
                                       break;
                                   default:
                                       throw new IllegalStateException("Unexpected value: " + currentConstantIteration);
                               }
                            }
                            if(bottom){
                                switch (currentConstantIteration){
                                    case 0: neighborSummation = neighborSummation + a[i+1][j].getCurrentTemp()*a[i+1][j].getMetal1Percentage();
                                        break;
                                    case 1: neighborSummation = neighborSummation + a[i+1][j].getCurrentTemp()*a[i+1][j].getMetal2Percentage();
                                        break;
                                    case 2: neighborSummation = neighborSummation + a[i+1][j].getCurrentTemp()*a[i+1][j].getMetal3Percentage();
                                        break;
                                    default:
                                        throw new IllegalStateException("Unexpected value: " + currentConstantIteration);
                                }
                            }
                            if(left){

                                switch (currentConstantIteration){
                                    case 0: neighborSummation = neighborSummation + a[i][j-1].getCurrentTemp()*a[i][j-1].getMetal1Percentage();
                                        break;
                                    case 1: neighborSummation = neighborSummation + a[i][j-1].getCurrentTemp()*a[i][j-1].getMetal2Percentage();
                                        break;
                                    case 2: neighborSummation = neighborSummation + a[i][j-1].getCurrentTemp()*a[i][j-1].getMetal3Percentage();
                                        break;
                                    default:
                                        throw new IllegalStateException("Unexpected value: " + currentConstantIteration);
                                }//neighborSummation += a[i][j-1].getCurrentTemp() * a[i][j-1].getMetal1Percentage() * a[i][j-1].getMetal2Percentage() * a[i][j-1].getMetal3Percentage();
                            }
                            if(right){
                                switch (currentConstantIteration){
                                    case 0: neighborSummation = neighborSummation + a[i][j+1].getCurrentTemp()*a[i][j+1].getMetal1Percentage();
                                        break;
                                    case 1: neighborSummation = neighborSummation + a[i][j+1].getCurrentTemp()*a[i][j+1].getMetal2Percentage();
                                        break;
                                    case 2: neighborSummation = neighborSummation + a[i][j+1].getCurrentTemp()*a[i][j+1].getMetal3Percentage();
                                        break;
                                    default:
                                        throw new IllegalStateException("Unexpected value: " + currentConstantIteration);
                                }
                                //neighborSummation += a[i][j+1].getCurrentTemp() * a[i][j+1].getMetal1Percentage() * a[i][j+1].getMetal2Percentage() * a[i][j+1].getMetal3Percentage();
                            }
                            if(topLeft){
                                switch (currentConstantIteration){
                                    case 0: neighborSummation = neighborSummation + a[i-1][j-1].getCurrentTemp()*a[i-1][j-1].getMetal1Percentage();
                                        break;
                                    case 1: neighborSummation = neighborSummation + a[i-1][j-1].getCurrentTemp()*a[i-1][j-1].getMetal2Percentage();
                                        break;
                                    case 2: neighborSummation = neighborSummation + a[i-1][j-1].getCurrentTemp()*a[i-1][j-1].getMetal3Percentage();
                                        break;
                                    default:
                                        throw new IllegalStateException("Unexpected value: " + currentConstantIteration);
                                }
                                //neighborSummation += a[i-1][j-1].getCurrentTemp() * a[i-1][j-1].getMetal1Percentage() * a[i-1][j-1].getMetal2Percentage() * a[i-1][j-1].getMetal3Percentage();
                            }
                            if(topRight){
                                switch (currentConstantIteration){
                                    case 0: neighborSummation = neighborSummation + a[i-1][j+1].getCurrentTemp()*a[i-1][j+1].getMetal1Percentage();
                                        break;
                                    case 1: neighborSummation = neighborSummation + a[i-1][j+1].getCurrentTemp()*a[i-1][j+1].getMetal2Percentage();
                                        break;
                                    case 2: neighborSummation = neighborSummation + a[i-1][j+1].getCurrentTemp()*a[i-1][j+1].getMetal3Percentage();
                                        break;
                                    default:
                                        throw new IllegalStateException("Unexpected value: " + currentConstantIteration);
                                }
                                //neighborSummation += a[i-1][j+1].getCurrentTemp() * a[i-1][j+1].getMetal1Percentage() * a[i-1][j+1].getMetal2Percentage() * a[i-1][j+1].getMetal3Percentage();
                            }
                            if(bottomLeft){
                                switch (currentConstantIteration){
                                    case 0: neighborSummation = neighborSummation + a[i+1][j-1].getCurrentTemp()*a[i+1][j-1].getMetal1Percentage();
                                        break;
                                    case 1: neighborSummation = neighborSummation + a[i+1][j-1].getCurrentTemp()*a[i+1][j-1].getMetal2Percentage();
                                        break;
                                    case 2: neighborSummation = neighborSummation + a[i+1][j-1].getCurrentTemp()*a[i+1][j-1].getMetal3Percentage();
                                        break;
                                    default:
                                        throw new IllegalStateException("Unexpected value: " + currentConstantIteration);
                                }
                                //neighborSummation += a[i+1][j-1].getCurrentTemp() * a[i+1][j-1].getMetal1Percentage() * a[i+1][j-1].getMetal2Percentage() * a[i+1][j-1].getMetal3Percentage();
                            }
                            if(bottomRight){
                                switch (currentConstantIteration){
                                    case 0: neighborSummation = neighborSummation + a[i+1][j+1].getCurrentTemp()*a[i+1][j+1].getMetal1Percentage();
                                        break;
                                    case 1: neighborSummation = neighborSummation + a[i+1][j+1].getCurrentTemp()*a[i+1][j+1].getMetal2Percentage();
                                        break;
                                    case 2: neighborSummation = neighborSummation + a[i+1][j+1].getCurrentTemp()*a[i+1][j+1].getMetal3Percentage();
                                        break;
                                    default:
                                        throw new IllegalStateException("Unexpected value: " + currentConstantIteration);
                                }
                            }

                            neighborSummation = neighborSummation*currentConstant;
                            constantSummation += neighborSummation;
                        }
                        constantSummation = constantSummation/numberOfNeighbors;
                        finalNewTemp = constantSummation;
                        double thisMaxDif = Math.abs(b[i][j].getCurrentTemp()-finalNewTemp);
                        if(thisMaxDif > currentMaxDif){
                            currentMaxDif = thisMaxDif;
                        }
                        b[i][j].changeTemp(finalNewTemp);
                    }

                }
            }
            this.maxDifference = currentMaxDif;
            this.isDone = true;
        }

        @Override
        public void reset() {
            steps = 0;
        }
    }

    static class engine extends RecursiveAction{
        //the root mesh
        private MeshSection root;
        //the two mesh regions
        private region[][] A;
        private region[][] B;
        private int firstRow;
        private int firstColumn;
        private int lastColumn;
        private int steps = 0;
        private int granularity;
        int numberOfSegments = 0;
        int numOfZeroes = 0;

        public engine(region[][] A, region[][] B, int firstRow, int lastRow, int firstColumn, int lastColumn, int granularity) throws InterruptedException {
            this.A = A;
            this.B = B;
            this.firstRow = firstRow;
            this.firstColumn = firstColumn;
            this.lastColumn = lastColumn;
            this.granularity = granularity;
            root = build(this.A, this.B, this.firstRow, lastRow, this.firstColumn, this.lastColumn, this.granularity);
            System.out.println("Built using "+ numberOfSegments + " segments");
            System.out.println(numOfZeroes);
        }

        @Override
        protected void compute() {
            int steps = 1;
            while(true) {
                //sets the counted completer of the root to handle 4 quadrants running, being that the root is always
                // guaranteed to have four quadrants to run on. It does this for every subsequent run of the computation
                //otherwise we have no reason to run this in parallel if it is small enough where a single leaf is made

                root.invoke();

                /*
                System.out.println("A array");
                for(int i = 0; i<A.length; i++){
                    for (int j = 0; j<A[0].length; j++){
                        System.out.print("|");
                        System.out.print(String.format("%.02f", A[i][j].getCurrentTemp()));
                        System.out.print("|");
                    }
                    System.out.println();
                }

                System.out.println("B array");
                for(int i = 0; i<A.length; i++){
                    for (int j = 0; j<A[0].length; j++){
                        System.out.print("|");
                        System.out.print(String.format("%.02f", B[i][j].getCurrentTemp()));
                        System.out.print("|");
                    }
                    System.out.println();
                }

                 */





                if (root.maxDifference <= EPSILON) {
                    System.out.println("Converged after "+ this.steps + " steps");
                    System.out.println("Max Difference: "+ root.maxDifference);
                    System.out.println("B array");
                    for(int i = 0; i<A.length; i++){
                        for (int j = 0; j<A[0].length; j++){
                            System.out.print("|");
                            System.out.print(String.format("%.02f", B[i][j].getCurrentTemp()));
                            System.out.print("|");
                        }
                        System.out.println();
                    }
                    root.maxDifference = 0.0;
                    this.steps = 0;
                    root.reinitialize();
                    break;
                }
                this.steps++;
                if(this.steps % 2 == 0){
                    paintB = false;
                }else{
                    paintB = true;
                }

                root.reinitialize();
            }
        }

        public MeshSection build(region[][] a, region[][] b, int lr, int hr, int lc, int hc, int granularity){
            //get number of rows
            int rows = (hr - lr + 1);
            //get number of columns
            int cols = (hc - lc + 1);

            //get the midpoint of the columns and rows using the bitshift operation that divides by 2
            int midpointRows = (lr + hr) >>> 1;
            int midpointColumns = (lc + hc) >>> 1;

            //if the current section is less than or equal to the granularity, that is a leaf where we do actual calculations
            if (rows * cols <= granularity){
                ++numberOfSegments;
                return new Leaf(a, b, lr, hr, lc, hc);
            }else{
                fourQuad quad = new fourQuad();
                quad.q1 = build(a, b, lr, midpointRows, lc, midpointColumns, granularity);
                quad.q2 = build(a, b, lr, midpointRows, midpointColumns + 1, hc, granularity);
                quad.q3 = build(a, b, midpointRows + 1, hr, lc, midpointColumns, granularity);
                quad.q4 = build(a, b, midpointRows + 1, hr, midpointColumns + 1, hc, granularity);
                return quad;
            }
        }

        public void reset(){
            root.reset();
        }
    }
}

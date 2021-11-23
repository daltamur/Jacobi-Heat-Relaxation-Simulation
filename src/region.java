import java.util.concurrent.ThreadLocalRandom;

public class region {
    private final double metal1Percentage;
    private final double metal2Percentage;
    private final double metal3Percentage;
    private volatile double currentTemp;
    private boolean isOuterEdge;
    private boolean hasBeenVisited = false;

    public region(boolean isOuterEdge){
        this.isOuterEdge = isOuterEdge;
        if (!isOuterEdge) {
            metal1Percentage = .08 + (.58 - .08) * ThreadLocalRandom.current().nextDouble();
            if (metal1Percentage > 1.0/3.0){
                double maximum = .58 - metal1Percentage;
                metal2Percentage = .08 + (maximum - .08) * ThreadLocalRandom.current().nextDouble();
            }else if (metal1Percentage < 1.0/3.0){
                double minimum = .08 + (.33 - metal1Percentage);
                metal2Percentage = minimum + (.58 - minimum) * ThreadLocalRandom.current().nextDouble();
            }else {
                metal2Percentage = 1.0 / 3.0;
            }
            metal3Percentage = 1 - (metal1Percentage + metal2Percentage);
            currentTemp =  ThreadLocalRandom.current().nextDouble(5.0, 15.0);
        }else{
            metal1Percentage = 0.0;
            metal2Percentage = 0.0;
            metal3Percentage = 0.0;
            currentTemp = 0;
        }
    }


    public double getMetal1Percentage(){
        return metal1Percentage;
    }

    public double getMetal2Percentage(){
        return metal2Percentage;
    }

    public double getMetal3Percentage(){
        return metal3Percentage;
    }

    public boolean getIsOuterEdge(){
        return isOuterEdge;
    }

    public double getCurrentTemp(){
        return currentTemp;
    }

    public void changeTemp(double change){
        currentTemp = change;
    }

    public void setHasBeenVisited(){
        hasBeenVisited = true;
    }

    public boolean getHasBeenVisited(){
        return hasBeenVisited;
    }
}

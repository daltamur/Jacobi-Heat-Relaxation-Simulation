import java.util.concurrent.ThreadLocalRandom;

public class region {
    private final double metal1Percentage;
    private final double metal2Percentage;
    private final double metal3Percentage;
    private volatile double currentTemp;
    private boolean isHeatedCorner;
    private boolean hasBeenVisited = false;
    int numberOfTimesVisited = 0;

    public region(boolean isHeatedCorner, double metal1Percentage, double metal2Percentage, double metal3Percentage, double currentTemp){
        this.isHeatedCorner = isHeatedCorner;
        this.metal1Percentage = metal1Percentage;
        this.metal2Percentage = metal2Percentage;
        this.metal3Percentage = metal3Percentage;
        this.currentTemp = currentTemp;
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

    public boolean getIsHeatedCorner(){
        return isHeatedCorner;
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

    public void setHeatedCorner(boolean value){
        isHeatedCorner = value;
    }
}

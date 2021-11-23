import java.util.Arrays;

public class testArray {
    public static void main(String[] args){
        double[] arrayVal = new double[]{1.0, 2.5, 6.7};
        outerArray outer = new outerArray(arrayVal);
        outerArray outer2 = new outerArray(arrayVal);
        outer.setInnerVals();
        System.out.println(Arrays.toString(outer2.arrayVal));

    }


    public static class outerArray{
        double[] arrayVal;
        innerArray innerVal;

        public outerArray(double[] value){
            arrayVal = value;
        }

        public void setInnerVals(){
            innerVal = new innerArray(arrayVal);
            System.out.println(Arrays.toString(arrayVal));
            innerVal.print();
        }


    }

    public static class innerArray{
        double[] arrayVal;

        public innerArray(double[] arrayVal){
            this.arrayVal = arrayVal;
            setArray();
        }

        public void setArray(){
            arrayVal[0] = .423243423;
        }

        public void print(){
            System.out.println(Arrays.toString(arrayVal));
        }

    }
}
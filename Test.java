// import ;
import java.util.Arrays;
import java.util.Random;


public class Test {
    public static void main(String[] args){
        Test test = new Test();
        int[] A = {2, 1, 7, 9, 5, 8};
        test.sort(A, 0, 5);
        System.out.println(Arrays.toString(A));
    }

    void sort(int[] nums, int lo, int hi){
        if(lo>=hi) return;

        int p = partition(nums, lo, hi);

        sort(nums, lo, p-1);
        sort(nums, p+1, hi);
    }

    int partition(int[] nums, int lo, int hi){
        swap(nums, randInt(lo, hi), hi);

        int i, j;

        for(i = lo, j=lo; j < hi; j++){
            if(nums[j] < nums[hi]){
                swap(nums, i++, j);
            }
        }
        swap(nums, i, j);
        return i;
    }

    void swap(int[] nums, int i, int j){
        int tmp = nums[i];
        nums[i] = nums[j];
        nums[j] = tmp;
    }

    int randInt(int lo, int hi){
        Random rand = new Random();
        return rand.nextInt((hi - lo) + 1) + lo;
    }
}

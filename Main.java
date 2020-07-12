import java.util.*;

public class Main{
    static int ans = 5;
    static String s = "(sub (mul 2 4) (div 9 3))";

    public static void main(String[] args){
        Scanner sc = new Scanner(s);
        while(sc.hasNextLine()){
            String in = sc.nextLine();

            int res = f(in);
            System.out.println(res);
        }

    }

    public static int f(String in){
        Deque<Character> queue = new LinkedList<>();
        char[] chars = in.toCharArray();
        for(int i=0; i<chars.length; i++){
            Character ch = chars[i];
            if(ch != ' ' || (ch == ' ' && chars[i-1] != '(' && chars[i+1] != ')')) queue.addLast(ch);
        }
        queue.forEach(e->System.out.print(e+ " "));
        System.out.println("");
        return calculate(queue);


    }

    public static int calculate(Deque<Character> queue){
        // if(queue.peekFirst() == '('){
        //     System.out.println(Arrays.toString(queue.toArray()));
        // }
        String sign = "add";
        int prev = 0;
        int num = 0;
        Deque<Integer> stack = new LinkedList<>();

        int sum = 0;
        while(!queue.isEmpty()){
            Character c = queue.removeFirst();
            if(c == '('){
                num = calculate(queue);
            } else if(Character.isDigit(c)){
                num = num * 10 + c - '0';
            } else if(c == ' '){
                prev = num;
                num = 0;
            } else if(c == ')'){
                if(sign.equals("add")){
                    sum = prev+num;
                } else if(sign.equals("sub")){
                    sum = prev-num;
                } else if(sign.equals("mul")){
                    sum = prev * num;
                } else if(sign.equals("div")){
                    sum = prev / num;
                }
                break;
            } else {
                char[] op = new char[]{c, queue.removeFirst(), queue.removeFirst()};
                sign = new String(op);
            }

        }
        // stack.forEach(e->System.out.print(e+ " "));
        // System.out.println("");

        // System.out.println("===");
        // while(!stack.isEmpty()){
        //     sum += stack.removeLast();
        // }
        System.out.println("sum=" + sum);
        return sum;


    }

}

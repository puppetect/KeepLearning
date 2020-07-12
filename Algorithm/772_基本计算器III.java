import java.util.*;

public class Main{
    static int ans = -12;
    static String s = "(2+6* 3+5- (3*14/7+2)*5)+3";
    // static String s = "1 - 2*3 * (4+5)";

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
        for(Character ch: in.toCharArray()){
            if(ch != ' ') queue.addLast(ch);
        }
        queue.addLast('+');
        queue.forEach(e->System.out.print(e+ " "));
        System.out.println("===");
        return calculate(queue);


    }

    public static int calculate(Deque<Character> queue){
        // if(queue.peekFirst() == '('){
        //     System.out.println(Arrays.toString(queue.toArray()));
        // }
        Character sign = '+';
        int num = 0;
        Deque<Integer> stack = new LinkedList<>();
        while(!queue.isEmpty()){
            Character c = queue.removeFirst();
            if(c == '('){
                num = calculate(queue);
            } else if(Character.isDigit(c)){
                num = num * 10 + c - '0';
            } else {
                if(sign == '+'){
                    stack.addFirst(num);
                } else if(sign == '-'){
                    stack.addFirst(-num);
                } else if(sign == '*'){
                    stack.addFirst(stack.removeFirst() * num);
                } else if(sign == '/'){
                    stack.addFirst(stack.removeFirst() / num);
                }
                sign = c;
                num = 0;

                if(c == ')'){
                    break;
                }

            }

        }
        stack.forEach(e->System.out.print(e+ " "));
        System.out.println("");

        System.out.println("===");
        int sum = 0;
        while(!stack.isEmpty()){
            sum += stack.removeLast();
        }
        return sum;


    }

}

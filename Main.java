import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;

public class Main{
    static String s = "2 abc 123456789";

    public static void main(String[] args){
        Scanner sc = new Scanner(s);
        while(sc.hasNext()){
            Integer n = sc.nextInt();
            List<String> list = new ArrayList<>();
            for(int i=0; i<n; i++){
                list.add(sc.next());
            }
            List<String> res = f(list);
            res.stream().forEach(System.out::println);
        }
        
    }
    
    public static List<String> f(List<String> strList){
        List<String> result = new ArrayList<>();
        for(String strItem: strList){
            int i = 0;
            while(i - strItem.length() < 0){
                String str = new String();
                if(i + 8 - strItem.length() <= 0){
                    str = strItem.substring(i, i+8);
                } else {
                    str = strItem.substring(i);
                    Integer paddingLength = i + 8 - strItem.length();
                    if(paddingLength >0){
                        for(int j=0; j<paddingLength; j++){
                            str += "0";
                        }
                    }
                }
                result.add(str);
                i += 8;
            }
        }
        return result;
    }
    
}
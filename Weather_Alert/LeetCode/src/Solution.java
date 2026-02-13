import java.io.*;
import java.math.*;
import java.security.*;
import java.text.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;
import java.util.regex.*;
import java.util.stream.*;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;



class Result {

    /*
     * Complete the 'countDuplicate' function below.
     *
     * The function is expected to return an INTEGER.
     * The function accepts INTEGER_ARRAY numbers as parameter.
     */

    public static int countDuplicate(List<Integer> numbers) {
        Set<Integer> seen = new HashSet<>();
        Set<Integer> duplicates = new HashSet<>();

        for (Integer num : numbers) {
            if (!seen.add(num)) {
                duplicates.add(num);
            }
        }
        return duplicates.size();

    }
    public static List<String> matchingBraces(List<String> braces) {
        List<String> answers = new ArrayList<>();
        for(String currentBraces: braces) {
            Stack<Character> stack = new Stack<>();
            boolean isBalanced = true;
            for(char c : currentBraces.toCharArray()){
                if(c == '(' || c == '[' || c == '{'){
                    stack.push(c);
                }else{
                    if(stack.isEmpty()){
                        isBalanced = false;
                        break;
                    }
                    char top = stack.pop();
                    if((c == ')' && top != '(') || (c == ']'
                            && top != '[') || (c == '}'
                            && top != '{')) {
                    isBalanced = false;
                    break;
                    }
                }
            }
            answers.add((isBalanced && stack.isEmpty()) ? "YES" : "NO");
        }
        return answers;
    }

    public static int lengthOfLastWord(String s){
        String [] arrayOfWords = s.split(" ");
        int longestWordLength = 0;
        for(int i = 0; i< arrayOfWords.length; i++){
            if(longestWordLength < arrayOfWords[i].length()){
                longestWordLength = arrayOfWords[i].length();
            }
        }
        return longestWordLength;
    }
    public static String rollingString(String s, List<String> operations) {
        char[] chars = s.toCharArray();

        for (String op : operations) {
            String[] parts = op.split(" ");
            int i = Integer.parseInt(parts[0]);
            int j = Integer.parseInt(parts[1]);
            char direction = parts[2].charAt(0);

            for (int index = i; index <= j; index++) {
                if (direction == 'R') {
                    chars[index] = (char) ((chars[index] - 'a' + 1) % 26 + 'a');
                } else if (direction == 'L') {
                    chars[index] = (char) ((chars[index] - 'a' - 1 + 26) % 26 + 'a');
                }
            }
        }

        return new String(chars);
    }

}

public class Solution {
    public static void main(String[] args) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(System.getenv("OUTPUT_PATH")));

        int bracesCount = Integer.parseInt(bufferedReader.readLine().trim());

        List<String> braces = IntStream.range(0, bracesCount).mapToObj(i -> {
                    try {
                        return bufferedReader.readLine();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                })
                .collect(toList());

        List<String> result = Result.matchingBraces(braces);

        bufferedWriter.write(
                result.stream()
                        .collect(joining("\n"))
                        + "\n"
        );

        bufferedReader.close();
        bufferedWriter.close();
    }
}

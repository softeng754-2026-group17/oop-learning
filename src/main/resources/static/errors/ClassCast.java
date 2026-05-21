public class ClassCast {
    public static void main(String[] args) {
        Object obj = Integer.valueOf(42);
        String s = (String) obj;
        System.out.println(s);
    }
}

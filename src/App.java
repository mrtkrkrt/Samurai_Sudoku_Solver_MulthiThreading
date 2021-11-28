import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class App {

    private static HashMap<Long, Integer> done_squares = new HashMap<>();
    private static HashMap<Long, Integer> done_squares2 = new HashMap<>();
    private static long start_ms = System.currentTimeMillis();
    private static long start_ms2 = System.currentTimeMillis();
    private static ArrayList<ArrayList<Integer>> result = new ArrayList<>();
    private static ArrayList<ArrayList<Integer>> result2 = new ArrayList<>();

    static class Task implements Runnable{

        private ArrayList<ArrayList<Integer>> sudoku;
        private ArrayList<ArrayList<ArrayList<Integer>>> avaliable;
        private int i_start;
        private int i_end;
        private int y_start;
        private int y_end;
        private String name;
        private ReentrantLock lock = new ReentrantLock();

        public Task(ArrayList<ArrayList<Integer>> sudoku, ArrayList<ArrayList<ArrayList<Integer>>> avaliable, int i_start, int i_end, int y_start, int y_end, String name){
            this.sudoku = sudoku;
            this.name = name;
            this.avaliable = avaliable;
            this.i_start = i_start;
            this.i_end = i_end;
            this.y_start = y_start;
            this.y_end = y_end;
        }

        @Override
        public synchronized void run() {
            int i = i_start, j = y_start;
            while(true){
                if(i == i_end && j == y_end){
                    place_one(this.sudoku, this.avaliable, i, j, this.name);
                    place_cube_difference(this.sudoku, this.avaliable, i, j, this.name);
                    break;
                } 
                try{
                    lock.lock();
                    place_one(this.sudoku, this.avaliable, i, j, this.name);
                    place_cube_difference(this.sudoku, this.avaliable, i, j, this.name);
                    if(j == y_end){
                        i++;
                        j = y_start;
                    }else j++;
                }catch(Exception e){
                    System.out.println();
                }finally{
                    lock.unlock();
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {

        try {
            File myObj = new File("Steps.txt");
            if (myObj.createNewFile()) {
              System.out.println("File created: " + myObj.getName());
            } else {
              PrintWriter writer = new PrintWriter("Steps.txt");
              writer.print(""); writer.close();
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        ArrayList<ArrayList<Integer>> samurai_sudoku = create_sudoku();
        ArrayList<ArrayList<ArrayList<Integer>>> avaliable_matrix = get_avaliable_nums_matrix(samurai_sudoku);
        long now = System.currentTimeMillis() - start_ms;
        done_squares.put(now, 0);
        fill_more(samurai_sudoku, avaliable_matrix, true); 
        print_nat(result, "10 THREAD ÇÖZÜMÜ");
        //display_chart("10 Threadli Çözüm");

        long now2 = System.currentTimeMillis();
        start_ms2 = now2;
        ArrayList<ArrayList<Integer>> samurai_sudoku2 = create_sudoku();
        ArrayList<ArrayList<ArrayList<Integer>>> avaliable_matrix2 = get_avaliable_nums_matrix(samurai_sudoku2);
        done_squares.put(now2, 0);
        fill_more(samurai_sudoku2, avaliable_matrix2, false); 
        print_nat(result2, "5 THREAD ÇÖZÜMÜ");
        display_chart("5 Threadli Çözüm");
    }

    private static void display_chart(String title){
        TreeMap<Long, Integer> sorted = new TreeMap<>();
 
        // Copy all data from hashMap into TreeMap
        sorted.putAll(done_squares);
        ArrayList<Long> aaaa = new ArrayList<>();
        ArrayList<Integer> bbbb = new ArrayList<>();

        for (Map.Entry<Long, Integer> entry : sorted.entrySet()){
            aaaa.add(entry.getKey());
            bbbb.add(entry.getValue());
        }
        
        TreeMap<Long, Integer> sorted2 = new TreeMap<>();

        sorted2.putAll(done_squares2);
        ArrayList<Long> aaaa2 = new ArrayList<>();
        ArrayList<Integer> bbbb2 = new ArrayList<>();

        for (Map.Entry<Long, Integer> entry : sorted2.entrySet()){
            aaaa2.add(entry.getKey());
            bbbb2.add(entry.getValue());
        }
        Chart ch = new Chart(aaaa, bbbb, aaaa2, bbbb2);
    }

    private static ArrayList<ArrayList<Integer>> create_sudoku(){
        ArrayList<ArrayList<Integer>> sudoku = new ArrayList<>();
        BufferedReader reader;
        try{
            reader = new BufferedReader(new FileReader("C:\\Users\\mrtkr\\Desktop\\2.4\\bitmiyordu\\src\\hard1.txt"));
            String line = reader.readLine();   
            int temp = line.length();  
            while(line != null){
                ArrayList<Integer> row = new ArrayList<>();
                String[] chrctrs =  line.split("");
                for (String c : chrctrs) {
                    if(c.equals("*")) row.add(0);
                    else row.add(Integer.parseInt(c));
                }
                if(row.size() == temp){
                    for (int i = 0; i < 3; i++) row.add(9, -1);
                }
                else if (row.size() > temp){

                }else if(row.size() < temp){
                    for (int i = 0; i < 6; i++) row.add(0, -1);
                    for (int i = 0; i < 6; i++) row.add(row.size(), -1);
                }
                sudoku.add(row);
                line = reader.readLine();
            }
            reader.close();
        }catch(IOException e){
            e.printStackTrace();
        }
        return sudoku;
    }

    // Create avaliable Arraylist
    private static ArrayList<ArrayList<ArrayList<Integer>>> get_avaliable_nums_matrix(ArrayList<ArrayList<Integer>> sudoku){
        ArrayList<ArrayList<ArrayList<Integer>>> avaliable = new ArrayList<>();
        for (int i = 0; i < sudoku.size(); i++) {
            ArrayList<ArrayList<Integer>> temp2 = new ArrayList<>();
            for (int j = 0; j < sudoku.get(0).size(); j++) {
                ArrayList<Integer> temp = new ArrayList<>();
                if(sudoku.get(i).get(j) == 0){
                    for (int k = 0; k < 9; k++) temp.add(k+1);
                    set_cube_nums(temp, sudoku, i, j);
                    set_x_nums(temp, sudoku, i, j);
                }
                temp2.add(temp);
            }
            avaliable.add(temp2);
        }
        return avaliable;
    }
    private static void set_cube_nums(ArrayList<Integer> temp, ArrayList<ArrayList<Integer>> sudoku, int i, int j){
        if(sudoku.get(i).get(j) != 0){
            temp.clear();
            return;
        } 
        i = i - (i % 3);
        j = j - (j % 3);

        for(int k = 0; k < 3; k++){
            for(int l = 0;l < 3; l++){
                if(temp.contains(sudoku.get(i + k).get(j + l))) temp.remove(Integer.valueOf(sudoku.get(i + k).get(j + l)));
            }
        }
    }

    private static void set_x_nums(ArrayList<Integer> temp, ArrayList<ArrayList<Integer>> sudoku, int i, int j){
        if((i > 5 && i < 9) && (j > 5 && j < 9)){
            //Sol üst
            //System.out.print(sudoku.get(i).get(j) + "=>");
            //System.out.println("Sol Üst Kare " + i + "," + j);
            for(int k = 0; k < 15; k++){
                if(sudoku.get(i).get(k) != 0 && temp.contains(sudoku.get(i).get(k))) temp.remove(Integer.valueOf(sudoku.get(i).get(k)));
                if(sudoku.get(k).get(j) != 0 && temp.contains(sudoku.get(k).get(j))) temp.remove(Integer.valueOf(sudoku.get(k).get(j)));
            }
        }else if((i > 5 && i < 9) && (j > 11 && j < 15)){
            //sağ üst
            //System.out.println("Sağ Üst Kare");
            for(int k = 0; k < 15; k++){
                if(sudoku.get(k).get(j) != 0 && temp.contains(sudoku.get(k).get(j))) temp.remove(Integer.valueOf(sudoku.get(k).get(j)));
            }
            for(int k = 6; k < 21; k++){
                if(sudoku.get(i).get(k) != 0 && temp.contains(sudoku.get(i).get(k))) temp.remove(Integer.valueOf(sudoku.get(i).get(k)));
            }
        }else if((i > 11 && i < 15) && (j > 5 && j < 9)){
            //sol alt
            for(int k = 0; k < 15; k++){
                if(sudoku.get(i).get(k) != 0 && temp.contains(sudoku.get(i).get(k))) temp.remove(Integer.valueOf(sudoku.get(i).get(k)));
            }
            for(int k = 6; k < 21; k++){
                if(sudoku.get(k).get(j) != 0 && temp.contains(sudoku.get(k).get(j))) temp.remove(Integer.valueOf(sudoku.get(k).get(j)));
            }
        }else if((i > 11 && i < 15) && (j > 11 && j < 15)){
            //sağ alt
            for(int k = 6; k < 21; k++){
                if(sudoku.get(i).get(k) != 0 && temp.contains(sudoku.get(i).get(k))) temp.remove(Integer.valueOf(sudoku.get(i).get(k)));
                if(sudoku.get(k).get(j) != 0 && temp.contains(sudoku.get(k).get(j))) temp.remove(Integer.valueOf(sudoku.get(k).get(j)));
            }
        }else{
            //normal
            if(i < 9 && j > 11){
                for(int k = 0; k < 9; k++){
                    if(sudoku.get(i).get(k+12) != 0 && temp.contains(sudoku.get(i).get(k+12))) temp.remove(Integer.valueOf(sudoku.get(i).get(k+12)));
                    if(sudoku.get(k).get(j) != 0 && temp.contains(sudoku.get(k).get(j))) temp.remove(Integer.valueOf(sudoku.get(k).get(j)));
                }
            }else if(i > 11 && j < 9){//17,2
                for(int k = 0; k < 9; k++){
                    if(sudoku.get(i).get(k) != 0 && temp.contains(sudoku.get(i).get(k))) temp.remove(Integer.valueOf(sudoku.get(i).get(k)));
                    if(sudoku.get(k+12).get(j) != 0 && temp.contains(sudoku.get(k+12).get(j))) temp.remove(Integer.valueOf(sudoku.get(k+12).get(j)));
                }
            }else if(i > 11 && j > 11){//19,18
                for(int k = 0; k < 9; k++){
                    if(sudoku.get(i).get(k+12) != 0 && temp.contains(sudoku.get(i).get(k+12))) temp.remove(Integer.valueOf(sudoku.get(i).get(k+12)));
                    if(sudoku.get(k+12).get(j) != 0 && temp.contains(sudoku.get(k+12).get(j))) temp.remove(Integer.valueOf(sudoku.get(k+12).get(j)));
                }
            }else if (i < 9 && j < 9){
                for(int k = 0; k < 9; k++){
                    if(sudoku.get(i).get(k) != 0 && temp.contains(sudoku.get(i).get(k))) temp.remove(Integer.valueOf(sudoku.get(i).get(k)));
                    if(sudoku.get(k).get(j) != 0 && temp.contains(sudoku.get(k).get(j))) temp.remove(Integer.valueOf(sudoku.get(k).get(j)));
                }
            }else{
                for(int k = 0; k < 9; k++){//11,10  
                    if(sudoku.get(i).get(k+6) != 0 && temp.contains(sudoku.get(i).get(k+6))) temp.remove(Integer.valueOf(sudoku.get(i).get(k+6)));
                    if(sudoku.get(k+6).get(j) != 0 && temp.contains(sudoku.get(k+6).get(j))) temp.remove(Integer.valueOf(sudoku.get(k+6).get(j)));
                }
            }
        }
    }

    private static void delete_cube_nums(ArrayList<ArrayList<Integer>> samurai_sudoku,  ArrayList<ArrayList<ArrayList<Integer>>> avaliable, int i, int j, int num){
        //İlk olarak matristeki yeri doldur
        samurai_sudoku.get(i).set(j, num);
        (avaliable.get(i)).get(j).clear();
        //Sırada satır, sütun ve küptekileri silv
        int temp_i = i - (i % 3);
        int temp_j = j - (j % 3);

        for (int k = 0; k < 3; k++) {
            for (int k2 = 0; k2 < 3; k2++) {
                if(avaliable.get(temp_i + k).get(temp_j + k2).contains(num)){
                    avaliable.get(temp_i + k).get(temp_j + k2).remove(Integer.valueOf(num));
                }
            }
        }
    }

    public static void place_num(ArrayList<ArrayList<Integer>> samurai_sudoku,  ArrayList<ArrayList<ArrayList<Integer>>> avaliable, int i, int j, int num, String thread_name){
        //System.out.println(i + "," + j + " inserted " + num);
        try {
            String step = thread_name + " => " + i + ","  + j + "'ye " + num + " eklendi.";
            BufferedWriter writer = new BufferedWriter(new FileWriter("Steps.txt", true));
            writer.append('\n');
            writer.append(step);
            writer.close();
          } catch (IOException e) {
            System.out.println("An error occurred while writing txt.");
            e.printStackTrace();
        }
        delete_cube_nums(samurai_sudoku, avaliable, i, j, num);
        delete_x_nums(samurai_sudoku, avaliable, i, j, num);
    }

    private static void delete_x_nums(ArrayList<ArrayList<Integer>> sudoku,  ArrayList<ArrayList<ArrayList<Integer>>> avaliable, int i, int j, int num){
        if((i > 5 && i < 9) && (j > 5 && j < 9)){
            //Sol üst
            for(int k = 0; k < 15; k++){
                if(avaliable.get(i).get(k).contains(num)) avaliable.get(i).get(k).remove(Integer.valueOf(num));
                if(avaliable.get(k).get(j).contains(num)) avaliable.get(k).get(j).remove(Integer.valueOf(num));
            }
        }else if((i > 5 && i < 9) && (j > 11 && j < 15)){
            //sağ üst
            for(int k = 0; k < 15; k++){
                if(avaliable.get(k).get(j).contains(num)) avaliable.get(k).get(j).remove(Integer.valueOf(num));
                if(avaliable.get(i).get(k+6).contains(num)) avaliable.get(i).get(k+6).remove(Integer.valueOf(num));
            }
        }else if((i > 11 && i < 15) && (j > 5 && j < 9)){
            //sol alt
            for(int k = 0; k < 15; k++){
                if(avaliable.get(k+6).get(j).contains(num)) avaliable.get(k+6).get(j).remove(Integer.valueOf(num));
                if(avaliable.get(i).get(k).contains(num)) avaliable.get(i).get(k).remove(Integer.valueOf(num));
            }
        }else if((i > 11 && i < 15) && (j > 11 && j < 15)){
            //sağ alt
            for(int k = 6; k < 21; k++){
                if(avaliable.get(i).get(k).contains(num)) avaliable.get(i).get(k).remove(Integer.valueOf(num));
                if(avaliable.get(k).get(j).contains(num)) avaliable.get(k).get(j).remove(Integer.valueOf(num));
            }
        }else{
            //normal
            if(i < 9 && j > 11){
                for(int k = 0; k < 9; k++){
                    if(avaliable.get(i).get(k+12).contains(num)) avaliable.get(i).get(k+12).remove(Integer.valueOf(num));
                    if(avaliable.get(k).get(j).contains(num)) avaliable.get(k).get(j).remove(Integer.valueOf(num));
                }
            }else if(i > 11 && j < 9){
                for(int k = 0; k < 9; k++){
                    if(avaliable.get(i).get(k).contains(num)) avaliable.get(i).get(k).remove(Integer.valueOf(num));
                    if(avaliable.get(k+12).get(j).contains(num)) avaliable.get(k+12).get(j).remove(Integer.valueOf(num));
                }
            }else if(i > 11 && j > 11){
                for(int k = 0; k < 9; k++){
                    if(avaliable.get(k+12).get(j).contains(num)) avaliable.get(k+12).get(j).remove(Integer.valueOf(num));
                    if(avaliable.get(i).get(k+12).contains(num)) avaliable.get(i).get(k+12).remove(Integer.valueOf(num));
                }
            }else if (i < 9 && j < 9){
                for(int k = 0; k < 9; k++){
                    if(avaliable.get(i).get(k).contains(num)) avaliable.get(i).get(k).remove(Integer.valueOf(num));
                    if(avaliable.get(k).get(j).contains(num)) avaliable.get(k).get(j).remove(Integer.valueOf(num));
                }
            }else{
                for(int k = 0; k < 9; k++){
                    if(avaliable.get(i).get(k+6).contains(num)) avaliable.get(i).get(k+6).remove(Integer.valueOf(num));
                    if(avaliable.get(k+6).get(j).contains(num)) avaliable.get(k+6).get(j).remove(Integer.valueOf(num));
                }
            }
        }
    }
    public static boolean place_one(ArrayList<ArrayList<Integer>> sudoku, ArrayList<ArrayList<ArrayList<Integer>>> avaliable, int i, int j, String thread_name){
        if(sudoku.get(i).get(j) == 0 && avaliable.get(i).get(j).size() == 1){
            place_num(sudoku, avaliable, i, j, avaliable.get(i).get(j).get(0), thread_name);
            return true;
        }else{
            return false;
        }
    }

    public static boolean place_cube_difference(ArrayList<ArrayList<Integer>> sudoku, ArrayList<ArrayList<ArrayList<Integer>>> avaliable, int i, int j, String thread_name){
        ArrayList<Integer> temp = new ArrayList<>();
        for (int k = 0; k < avaliable.get(i).get(j).size(); k++) {
            temp.add(avaliable.get(i).get(j).get(k));
        }

        int temp_i = i - (i % 3), temp_j = j - (j % 3);

        for (int k = 0; k < 3; k++) {
            for(int l = 0; l < 3;l++){
                if(!(i == (temp_i + k) && j == (temp_j + l)) && sudoku.get(temp_i + k).get(temp_j + l) == 0){
                    temp.removeAll(avaliable.get(temp_i + k).get(temp_j + l));
                }
            }
        }
        if(temp.size() == 1){
            //System.out.println("CUBE DIFFERENCE AT => " + i + "," + j);
            place_num(sudoku, avaliable, i, j, (int) temp.get(0), thread_name);
            return true;
        }
        return false;
    }

    private static boolean check_sudoku_complete(ArrayList<ArrayList<Integer>> sudoku){
        for (int i = 0; i < sudoku.size(); i++) {
            for (int j = 0; j < sudoku.get(0).size(); j++) {
                if(sudoku.get(i).get(j) == 0) return false;
            }
        }
        return true;
    }

    private static boolean check_wrong_complete(ArrayList<ArrayList<Integer>> sudoku, ArrayList<ArrayList<ArrayList<Integer>>> avaliable){
        for (int i = 0; i < sudoku.size(); i++) {
            for (int j = 0; j < sudoku.get(0).size(); j++) {
                if(sudoku.get(i).get(j) == 0 && avaliable.get(i).get(j).size() == 0) return false;
            }
        }
        return true;
    }

    private static ArrayList<Integer> get_next_more(ArrayList<ArrayList<ArrayList<Integer>>> avaliable){
        ArrayList<Integer> idx = new ArrayList<>();
        for (int i = 0; i < avaliable.size(); i++) {
            for (int j = 0; j < avaliable.get(0).size(); j++) {
                if(avaliable.get(i).get(j).size() > 1){
                    idx.add(i); idx.add(j);
                    return idx;
                }
            }            
        }
        return idx;
    }

    private static boolean fill_more(ArrayList<ArrayList<Integer>> sudoku, ArrayList<ArrayList<ArrayList<Integer>>> avaliable, boolean five_or_ten){
        while(true){
            if(check_sudoku_complete(sudoku)){
                if(five_or_ten) result = sudoku;
                else result2 = sudoku;
                return true;
            }
            if(!check_wrong_complete(sudoku, avaliable)){
                //System.out.println("Avaliable hatası");
                return false;
            }
           boolean change = false;
            ArrayList<ArrayList<Integer>> temp = new ArrayList<>();
            for (int i = 0; i < sudoku.size(); i++) {
                ArrayList<Integer> c = new ArrayList<>();
                for (int j = 0; j < sudoku.get(0).size(); j++) {
                    c.add(sudoku.get(i).get(j));
                }
                temp.add(c);
            }
            try {
                if(five_or_ten) initialize_10_threads(sudoku, avaliable);
                else initialize_5_threads(sudoku, avaliable);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            change = check_sudoku_change(sudoku, temp);
            /* int count = 0;
            for (int i = 0; i < sudoku.size(); i++) {
                for (int j = 0; j < sudoku.get(0).size(); j++) {
                    boolean b = place_one(sudoku, avaliable, i, j);
                    boolean a = place_cube_difference(sudoku, avaliable, i, j);
                    if(b || a) {
                        count++;
                        i = 0; j = 0;
                    }
                }
            } */
            if(!change){
                ArrayList<Integer> more = get_next_more(avaliable);
                int idx1 = more.get(0), idx2 = more.get(1);
                for (int z = 0; z < avaliable.get(idx1).get(idx2).size(); z++) {
                    ArrayList<ArrayList<Integer>> temp_sudoku = new ArrayList<>();
                    for (int i = 0; i < sudoku.size(); i++) {
                        ArrayList<Integer> a = new ArrayList<>();
                        for (int j = 0; j < sudoku.size(); j++) {
                            a.add(sudoku.get(i).get(j));
                        }
                        temp_sudoku.add(a);
                    }
                    ArrayList<ArrayList<ArrayList<Integer>>> temp_avaliable = new ArrayList<>();
                    for (int i = 0; i < avaliable.size(); i++) {
                        ArrayList<ArrayList<Integer>> a = new ArrayList<>();
                        for (int j = 0; j < avaliable.get(0).size(); j++) {
                            ArrayList<Integer> b = new ArrayList<>();
                            for (int j2 = 0; j2 < avaliable.get(i).get(j).size(); j2++) {
                                b.add(avaliable.get(i).get(j).get(j2));
                            }
                            a.add(b);
                        }
                        temp_avaliable.add(a);
                    }
                    place_num(temp_sudoku, temp_avaliable, idx1, idx2, avaliable.get(idx1).get(idx2).get(z), "Thread Last");
                    if(fill_more(temp_sudoku, temp_avaliable, five_or_ten)){
                        System.out.println("ÇÖZDÜ");
                        System.out.println(done_squares);
                        return true;
                    } 
                }
                return false;
            }
        }
    }

    private static void print_nat(ArrayList<ArrayList<Integer>> temp_sudoku, String title){
        System.out.println(title);

        try {
            File myObj = new File("Result.txt");
            if (myObj.createNewFile()) {
              System.out.println("File created: " + myObj.getName());
            } else {
              PrintWriter writer = new PrintWriter("Result.txt");
              writer.print(""); writer.close();
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        for (int z = 0; z < temp_sudoku.size(); z++) {
            String line = "";
            for (int y = 0; y < temp_sudoku.size(); y++) {
                if((int)(( temp_sudoku.get(z)).get(y)) != -1){
                    System.out.print(temp_sudoku.get(z).get(y) + " ");
                    line += String.valueOf(temp_sudoku.get(z).get(y)) + " ";
                }
                else{
                    System.out.print("* ");
                    line += "* ";
                }   
            }
            System.out.println("");
            //line += "\n";
            BufferedWriter wr;
            try {
                wr = new BufferedWriter(new FileWriter("Result.txt", true));
                wr.append('\n');
                wr.append(line);
                wr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        for (int i = 0; i < 5; i++)System.out.println();
    }

    private static boolean check_sudoku_change(ArrayList<ArrayList<Integer>> sudokuu, ArrayList<ArrayList<Integer>> sudoku2){
        for (int i = 0; i < sudokuu.size(); i++) {
            for (int j = 0; j < sudokuu.get(0).size(); j++) {
                if (sudokuu.get(i).get(j) != sudoku2.get(i).get(j)) return true;
            }
        }   
        return false;
    }

    private static void initialize_10_threads(ArrayList<ArrayList<Integer>> sudoku, ArrayList<ArrayList<ArrayList<Integer>>> avaliable) throws InterruptedException{
        int num_start = get_done_squares_num(sudoku);
        //System.out.println("10 Thread Başladı");
        Task t1 = new Task(sudoku, avaliable, 0, 4, 0, 8, "Thread1");
        Task t2 = new Task(sudoku, avaliable, 5, 8, 0, 9, "Thread 2");
        Task t3 = new Task(sudoku , avaliable, 0, 4, 12, 20, "Thread 3");
        Task t4 = new Task(sudoku , avaliable, 5, 8, 12, 20, "Thread 4");
        Task t5 = new Task(sudoku, avaliable, 12, 16, 0, 9, "Thread 5");
        Task t6 = new Task(sudoku, avaliable, 17, 20, 0, 9, "Thread 6");
        Task t7 = new Task(sudoku, avaliable, 12, 16, 12, 20, "Thread 7");
        Task t8 = new Task(sudoku, avaliable, 17, 20, 12, 20, "Thread 8");
        Task t9 = new Task(sudoku, avaliable, 6, 10, 6, 14, "Thread 9");
        Task t10 = new Task(sudoku, avaliable, 11, 14, 6, 14, "Thread 10");

        Thread lu1 = new Thread(t1);
        Thread lu2 = new Thread(t2);
        Thread ru1 = new Thread(t3);
        Thread ru2 = new Thread(t4);
        Thread lb1 = new Thread(t5);
        Thread lb2 = new Thread(t6);
        Thread rb1 = new Thread(t7);
        Thread rb2 = new Thread(t8);
        Thread md1 = new Thread(t9);
        Thread md2 = new Thread(t10);

        md1.start();md2.start();
        lu1.start();lu2.start();
        ru1.start();ru2.start();
        lb1.start();lb2.start();
        rb1.start();rb2.start();

        
        md1.join();md2.join();
        lu1.join();lu2.join();
        ru1.join();ru2.join();
        lb1.join();lb2.join();
        rb1.join();rb2.join();

        Thread last = new Thread(new Runnable() {

            @Override
            public void run() {
                int num_end = num_start - get_done_squares_num(sudoku);
                long end = System.currentTimeMillis() - start_ms;
                done_squares.put(end, num_end);   
            } 
        });
        last.start();
        last.join();
    }

    private static void initialize_5_threads(ArrayList<ArrayList<Integer>> sudoku, ArrayList<ArrayList<ArrayList<Integer>>> avaliable){
        int num_start = get_done_squares_num(sudoku);
        
        /* System.out.println("5 Thread Başladı"); */
        Task t1 = new Task(sudoku, avaliable, 0, 8, 0, 8, "Thread 1_2");
        Task t2 = new Task(sudoku, avaliable, 0, 8, 12, 20, "Thread 2_2");
        Task t3 = new Task(sudoku , avaliable, 12, 20, 12, 20, "Thread 3_2");
        Task t4 = new Task(sudoku , avaliable, 12, 20, 0, 8, "Thread 4_2");
        Task t5 = new Task(sudoku, avaliable, 6, 14, 6, 14, "Thread 5_2");

        Thread lu = new Thread(t1);
        Thread ru = new Thread(t2);
        Thread rb = new Thread(t3);
        Thread lb = new Thread(t4);
        Thread md = new Thread(t5);

        try {
            lu.start();ru.start();rb.start();lb.start();md.start();
            lu.join();ru.join();rb.join();lb.join();md.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Thread last = new Thread(new Runnable() {

            @Override
            public void run() {
                int num_end = num_start - get_done_squares_num(sudoku);
                long end = System.currentTimeMillis() - start_ms2;
                done_squares2.put(end, num_end);   
            } 
        });
        last.start();
        try {
            last.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    private static int get_done_squares_num(ArrayList<ArrayList<Integer>> sudoku){
        int count = 0;
        for (int i = 0; i < sudoku.size(); i++) {
            for (int j = 0; j < sudoku.get(0).size(); j++) {
                if(sudoku.get(i).get(j) == 0) count++;   
            }
        }
        return count;
    }
}

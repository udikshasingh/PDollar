import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;
import java.util.ArrayList;

class pdollar {
	
	static ArrayList<PointCloud> templates = new ArrayList<>();
	static PointCloud gesture;
	private static final String ADD_GESTURE = "-t";
	private static final String REMOVE_GESTURE = "-r";
	
	public static void main(String args[]) throws IOException {
		
			if(args.length == 0) {
			printHelpScreen();
			}
			//parse command-line arguments
			for(String str:args) {
				if(args[0].contains(ADD_GESTURE)) {
					String filename = args[1];
					addGesture(filename);
				}
				else if(args[0].contains(REMOVE_GESTURE)) {
					templates.clear();
				}
				else if(args[0].contains("eventfile.txt")) {
					System.out.println("Gesture list is empty. Add a Gesture file.");
				}
				else 
					System.out.println("invalid argument");
			}
			
			Scanner sc = new Scanner(System.in);
			
			//execute subsequent user inputs
			while(true) {
				String input = sc.nextLine();
				String command[] = input.split(" ");
				if(input.contains(ADD_GESTURE)) {
					String filename = command[2];
					addGesture(filename);
				}
				else if(input.contains(REMOVE_GESTURE)) {
					templates.clear();
				}
				else if(input.contains("eventfile.txt")) {
					String filename = command[1];
					recognize(filename);
				}
				else if(input.equals("pdollar"))
					printHelpScreen();
				else 
					System.out.println("Invalid Argument");
			}
	}
	
	// print help screen 
	static void printHelpScreen() {
		System.out.println("pdollar –t <gesturefile> -> Adds the gesture file to the list of gesture templates");
		System.out.println("pdollar -r               -> Clears the templates");
		System.out.println("pdollar <eventstream>    -> Prints the name of gestures as they are recognized from the event stream");
	}
	
	//add gesture to the template list
	static void addGesture(String filename) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(filename));
		String line = br.readLine();
		String name = line;
		int strokeId = 1;
		ArrayList<Point> points = new ArrayList<>();
		while((line=br.readLine())!=null) {
			if(line.equals("BEGIN"))
				continue;
			else if(line.equals("END")) {
				strokeId++;
			}
			else {
				String cordinates[] = line.split(",");
				float x = Float.parseFloat(cordinates[0]);
				float y = Float.parseFloat(cordinates[1]);
				points.add(new Point(x, y, strokeId));
			} 
		}
		PointCloud pcloud = new PointCloud(name, points);
		templates.add(pcloud);
	}
	
	//recognize gesture from event stream
	static void recognize(String filename) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(filename));
		int strokeId = 1;
		ArrayList<Point> points = new ArrayList<>();
		String line = "";
		while((line=br.readLine())!=null) {
			if(line.equals("MOUSEDOWN"))
				continue;
			else if(line.equals("MOUSEUP"))
				strokeId++;
			else if(line.equals("RECOGNIZE")) {
				gesture = new PointCloud("", points);
				String result = classify();
				if(result.equals("")) {
					System.out.println("UNKNOWN");
				}
				else
					System.out.println(result);
				points.clear();
			}
			else {
				
				String cordinates[] = line.split(",");
				float x = Float.parseFloat(cordinates[0]);
				float y = Float.parseFloat(cordinates[1]);
				points.add(new Point(x, y, strokeId));
			}
		}
	}
	
	//classify the input gesture based on the template list
	static String classify() {
		float minDist = Float.MAX_VALUE;
		String gestureClass = "";
		for(PointCloud template : templates) {
			float dist = GreedyCloudMatch(gesture.points, template.points);
			if (dist < minDist) {
                minDist = dist;
                gestureClass = template.name;
            }
		}
		return gestureClass;
	}
	
	//greedy search for minimum distance between the two pointclouds
	static float GreedyCloudMatch(ArrayList<Point> gesturePoints, ArrayList<Point> templatePoints) {
		int n = gesturePoints.size();
		float eps = 0.5f; 
		int step = (int)Math.floor(Math.pow(n, 1.0f - eps));
		float minDist = Float.MAX_VALUE;
		for (int i = 0; i < n; i += step) {
			float dist1 = CloudDistance(gesturePoints, templatePoints, i);
			float dist2 = CloudDistance(templatePoints, gesturePoints, i);
			minDist = Math.min(minDist, Math.min(dist1, dist2));
		}
		return minDist;
	}
	
	//performs greedy matching based on minimum distance, beginning with a start point
	 static float CloudDistance(ArrayList<Point> points1, ArrayList<Point> points2, int startIndex) {
		 int n = points1.size(); 
		 int n2 = points2.size();
		 boolean[] matched = new boolean[n];
		 float sum = 0;
		 int i = startIndex;
		 do {
			 int index = -1;
			 float minDist = Float.MAX_VALUE;
			 for(int j = 0; j < n; j++) {
				 if (!matched[j])
                 {
                     float dist = calculateEuclideanDistance(points1.get(i), points2.get(j));  
                     if (dist < minDist)
                     {
                         minDist = dist;
                         index = j;
                     }
                 }
			 }
			 matched[index] = true;
			 float weight = 1.0f - ((i - startIndex + n) % n) / (1.0f * n);
			 sum += weight * minDist;
			 i = (i + 1) % n;
		 }while (i != startIndex);
		 return sum;
	 }
	 
	 //calculate Euclidean distance
	 static float calculateEuclideanDistance(Point a, Point b) {
		 return (float)Math.sqrt(SqrEuclideanDistance(a, b));
	 }
	 
	 static float SqrEuclideanDistance(Point a, Point b) {
		 return (a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y);
	 }
}

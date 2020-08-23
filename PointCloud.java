import java.util.ArrayList;

 class PointCloud {
String name;
ArrayList<Point> points;

PointCloud(String name, ArrayList<Point> points) {
	this.name = name;
	
	this.points = scale(points);
	this.points = translateToOrigin(points, centroid(points));
	this.points = resample(points, 32);
}

private Point centroid(ArrayList<Point> points)
{
    float cx = 0, cy = 0;
    for (int i = 0; i < points.size(); i++)
    {
        cx += points.get(i).x;
        cy += points.get(i).y;
    }
    return new Point(cx / points.size(), cy / points.size(), 0);
}

private ArrayList<Point> translateToOrigin(ArrayList<Point> points, Point p)
{
	ArrayList<Point> newPoints = new ArrayList<Point>();
    for (int i = 0; i < points.size(); i++) {
    	newPoints.add(new Point(points.get(i).x - p.x, points.get(i).y - p.y, points.get(i).id));
    }
    return newPoints;
}

public ArrayList<Point> resample(ArrayList<Point> points, int n) {
	ArrayList<Point> newPoints = new ArrayList<Point>();
	newPoints.add(0, new Point(points.get(0).x, points.get(0).y, points.get(0).id));
	int numPoints = 1;
	float I = PathLength(points) / (n - 1);
	float D = 0;
	for (int i = 1; i < points.size(); i++)
    {
        if (points.get(i).id == points.get(i-1).id)
        {
            float d = calculateEuclideanDistance(points.get(i - 1), points.get(i));
            if (D + d >= I)
            {
                Point firstPoint = points.get(i - 1);
                while (D + d >= I)
                {
                    float t = Math.min(Math.max((I - D) / d, 0.0f), 1.0f);
                    if (Float.isNaN(t)) t = 0.5f;
                    newPoints.add(numPoints++ ,new Point((1.0f - t) * firstPoint.x + t * points.get(i).x, (1.0f - t) * firstPoint.y + t * points.get(i).y, points.get(i).id));
                    d = D + d - I;
                    D = 0;
                    firstPoint = newPoints.get(numPoints - 1);
                }
                D = d;
            }
            else D += d;
        }
    }
	if (numPoints == n - 1) {
        newPoints.add(numPoints++, new Point(points.get(points.size() - 1).x , points.get(points.size() - 1).y, points.get(points.size() - 1).id));
	}
    return newPoints;
}

static float calculateEuclideanDistance(Point a, Point b) {
	 return (float)Math.sqrt(SqrEuclideanDistance(a, b));
}

static float SqrEuclideanDistance(Point a, Point b) {
	 return (a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y);
}

float PathLength(ArrayList<Point> points) {
	float length = 0;
    for (int i = 1; i < points.size(); i++)
        if (points.get(i).id == points.get(i - 1).id)
            length += calculateEuclideanDistance(points.get(i - 1), points.get(i));
    return length;
}

private ArrayList<Point> scale(ArrayList<Point> points)
{
    float minx = Float.MAX_VALUE, miny = Float.MAX_VALUE, maxx = -Float.MAX_VALUE, maxy = -Float.MAX_VALUE;
    for (int i = 0; i < points.size(); i++)
    {
        if (minx > points.get(i).x) minx = points.get(i).x;
        if (miny > points.get(i).y) miny = points.get(i).y;
        if (maxx < points.get(i).x) maxx = points.get(i).x;
        if (maxy < points.get(i).y) maxy = points.get(i).y;
    }
    ArrayList<Point> newPoints = new ArrayList<Point>();
    float scale = Math.max(maxx - minx, maxy - miny);
    for (int i = 0; i < points.size(); i++) {
    	newPoints.add(new Point((points.get(i).x - minx) / scale, (points.get(i).y - miny) / scale, points.get(i).id));
    }
    return newPoints;
}

}

import java.util.PriorityQueue;
import java.util.Stack;

public class SunlitSurface {

	public static void main(String[] args) {
		SunlitSurface sunlitSurface = new SunlitSurface();
		double[][][] buildings = {{{4,0},{4,-5},{7,-5},{7,0}}, {{0.4,-2},{0.4,-5},{2.5,-5},{2.5,-2}}, {{15,4},{15,-5},{18,-5},{18,4}}};
		double[] src = {3.5,1};
		sunlitSurface.sunlitArea(buildings, src);
	}

	Point prev = null;
	private void sunlitArea(double[][][] buildings, double[] src) {
		PriorityQueue<Boundary> queue = new PriorityQueue<Boundary>();
		Stack<Boundary> stack = new Stack<Boundary>();
		
		for(double[][] building : buildings) {
			queue.offer(new Boundary(building[0], building[1], building[2], building[3]));
		}
		
		Point sun = new Point(src[0], src[1]);
		double sunlitArea = 0;
		while(!queue.isEmpty()) {
			Boundary boundary = queue.poll();
			double area = 0;
			
			// cond for pushing into stack (left side of light)
			if(boundary.topRight.x <= sun.x) {
				stack.push(boundary);
				continue;
			}
			if(boundary.topLeft.x < sun.x && sun.x <= boundary.topRight.x) {
				Boundary part = boundary;
				part.topRight.x = sun.x;
				part.bottomRight.x = sun.x;
				stack.add(part);
				boundary.topLeft.x = sun.x;
				boundary.bottomLeft.x = sun.x;
			}
			
			// right side of light
			if (prev == null) {
				area = initLeftArea(sun, boundary);				
			} else {
				area = leftSurfaceArea(sun, boundary);
			}
			sunlitArea += area;
			System.out.println("area : " + area + "/" + sunlitArea);
		}
		
		sunlitArea += leftSideBuildingArea(stack, sun);
		System.out.println(sunlitArea);
	}


	private double leftSideBuildingArea(Stack<Boundary> stack, Point sun) {
		prev = null;
		double sunlitArea2 = 0;
		while(!stack.isEmpty()) {
			Boundary boundary = stack.pop();
			double area = 0;
			if (prev == null) {
				area = initRightArea(sun, boundary);				
			} else {
				area = rightSurfaceArea(sun, boundary);
			}
			System.out.println("area : " + area);
			sunlitArea2 += area;
		}
		return sunlitArea2;
	}


	private double initLeftArea(Point sun, Boundary boundary) {
		if(boundary.topLeft.y<sun.y) {
			prev = boundary.topRight;
			return (boundary.topLeft.x == sun.x) ? boundary.width : (boundary.height + boundary.width);
		} else {
			prev = boundary.topLeft;
			return (boundary.topLeft.x == sun.x) ? 0 : boundary.height;
		}
	}
	
	private double initRightArea(Point sun, Boundary boundary) {
		if(boundary.topRight.y<sun.y) {
			prev = boundary.topLeft;
			return (boundary.topRight.x == sun.x) ? boundary.width : (boundary.height + boundary.width);
		} else {
			prev = boundary.topRight;
			return (boundary.topRight.x == sun.x) ? 0 : boundary.height;
		}
	}
	

	private double leftSurfaceArea(Point sun, Boundary boundary) {
		double width;
		double area = 0;
		double slope = slope(sun, prev);
		Point topLeft = boundary.topLeft;
		Point topRight = boundary.topRight;
		Point bottomLeft = boundary.bottomLeft;
		double intersectX = lineProjectionX(sun, topLeft, slope);
		double intersectY = lineProjectionY(sun, topLeft, slope);
		
		if(topLeft.y > intersectY && intersectY > bottomLeft.y) {
			if(topLeft.y >= sun.y) {
				// special case
				width = 0;
				prev = topLeft;
			} else {
				width = boundary.width;
				prev = topRight;
			} 
			area = Math.abs(topLeft.y - intersectY) + width;
			
		} else if(bottomLeft.y > intersectY) {
			if(topLeft.y >= sun.y) {
				width = 0;
				prev = topLeft;
			} else {
				width = boundary.width;
				prev = topRight;
			} 
			area = boundary.height + width;
			
		} else if (topLeft.y <= intersectY) {
			
			area = 0;
			if(topRight.x > intersectX && intersectX >= topLeft.x) {
				area += Math.abs(topRight.x - intersectX);
				prev = topRight;
			} else if(topLeft.x > intersectX) {
				area += boundary.width;
				prev = topRight;
			} else if (intersectX > topRight.x) {
				// nothing
			}
		}
		return area;
	}
	

	private double rightSurfaceArea(Point sun, Boundary boundary) {
		double width;
		double area = 0;
		double slope = slope(sun, prev);
		Point topRight = boundary.topRight;
		Point topLeft = boundary.topLeft;
		Point bottomRight = boundary.bottomRight;
		
		double intersectX = lineProjectionX(sun, topRight, slope);
		double intersectY = lineProjectionY(sun, topRight, slope);
		
		
		if(topRight.y > intersectY && intersectY > bottomRight.y) {
			if(topRight.y >= sun.y) {
				// special case
				width = 0;
				prev = topRight;
			} else {
				width = boundary.width;
				prev = topLeft;
			}
			area = Math.abs(topRight.y - intersectY) + width;
			
		} else if(bottomRight.y > intersectY) {
			if(topRight.y >= sun.y) {
				width = 0;
				prev = topRight;
			} else {
				width = boundary.width;
				prev = topLeft;
			}
			area = boundary.height + width;
			prev = topLeft;
			
		} else if (topRight.y <= intersectY) {
			
			area = 0;
			if(topRight.x >= intersectX && intersectX > topLeft.x) {
				area += Math.abs(intersectX - topLeft.x);
				prev = topLeft;
			} else if(intersectX > topRight.x) {
				area += boundary.width;
				prev = topLeft;
			} else if (intersectX < topLeft.x) {
				// nothing
			}
		}
		if(topRight.y >= sun.y) {
			prev = topRight;
		}
		return area;
	}
	
	
	
	private double lineProjectionY(Point sun, Point curr, double slope) {
		double intersectionY = slope*curr.x - slope*sun.x + sun.y; 
		return intersectionY;
	}
	
	private double lineProjectionX(Point sun, Point curr, double slope) {
		double intersectionX = curr.y/slope - sun.y/slope + sun.x; 
		return intersectionX;
	}

	private double slope(Point sun, Point lastHighPoint) {
		return (lastHighPoint.y - sun.y)/(lastHighPoint.x - sun.x);
	}
	
	private class Boundary implements Comparable<Boundary>{
		Point topLeft, bottomLeft, bottomRight, topRight;
		double height, width;
		
		public Boundary(double[] a, double[] b, double[] c, double[] d) {
			topLeft = new Point(a[0], a[1]);
			bottomLeft = new Point(b[0], b[1]);
			bottomRight = new Point(c[0], c[1]);
			topRight = new Point(d[0], d[1]);
			height = Math.abs(topLeft.y - bottomLeft.y);
			width = Math.abs(topRight.x - topLeft.x);
		}
		
		@Override
		public int compareTo(Boundary point2) {
			return Double.compare(topLeft.x, point2.topLeft.x);
		}
	}
	
	
	private class Point {
		double x, y;

		public Point(double x, double y) {
			this.x = x;
			this.y = y;
		}
	}
}

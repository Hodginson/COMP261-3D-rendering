package renderer;

/**
 * EdgeList should store the data for the edge list of a single polygon in your
 * scene. A few method stubs have been provided so that it can be tested, but
 * you'll need to fill in all the details.
 *
 * You'll probably want to add some setters as well as getters or, for example,
 * an addRow(y, xLeft, xRight, zLeft, zRight) method.
 */
public class EdgeList {
	 private int beginningY, finalY;
	 private float[] leftX, rightX, leftZ, rightZ;
	
	public EdgeList(int startY, int endY) {
		// TODO fill this in.
		beginningY = startY;
		finalY = endY;
        int y = endY - startY;

        leftX = new float[y];
        rightX = new float[y];
        leftZ = new float[y];
        rightZ = new float[y];
         
        for (int i = 0; i < y; i++) {
            leftX[i] = Float.POSITIVE_INFINITY;
            rightX[i] = Float.NEGATIVE_INFINITY; 
            leftZ[i] = Float.POSITIVE_INFINITY;
            rightZ[i] = Float.POSITIVE_INFINITY;
        }
	}

	 public int getStartY() {
	        return beginningY;
	    }

	 public int getEndY() {
	    return finalY;
	 }

	 public float getLeftX(int y) {
	    return leftX[y];
	 }

	 public float getRightX(int y) {
	    return rightX[y];
	 }

	 public float getLeftZ(int y) {
	     return leftZ[y];
	 }

	 public float getRightZ(int y) {
	     return rightZ[y];
	 }
	 public void addRow(int y, float x, float z) {

	    if (x <= leftX[y]) {
	        leftX[y] = x;
	        leftZ[y] = z;
	    }

	    if (x >= rightX[y]) {
	        rightX[y] = x;
	        rightZ[y] = z;
	    }
	  }
}

// code for comp261 assignments

package renderer;

import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import renderer.Scene.Polygon;



/**
 * The Pipeline class has method stubs for all the major components of the
 * rendering pipeline, for you to fill in.
 * 
 * Some of these methods can get quite long, in which case you should strongly
 * consider moving them out into their own file. You'll need to update the
 * imports in the test suite if you do.
 */
public class Pipeline {

	/**
	 * Returns true if the given polygon is facing away from the camera (and so
	 * should be hidden), and false otherwise.
	 */
	public static boolean isHidden(Polygon poly) {
		return poly.getNormal().z > 0;
	}

	/**
	 * Computes the colour of a polygon on the screen, once the lights, their
	 * angles relative to the polygon's face, and the reflectance of the polygon
	 * have been accounted for.
	 * 
	 * @param lightDirection
	 *            The Vector3D pointing to the directional light read in from
	 *            the file.
	 * @param lightColor
	 *            The color of that directional light.
	 * @param ambientLight
	 *            The ambient light in the scene, i.e. light that doesn't depend
	 *            on the direction.
	 */
	public static Color getShading(Polygon poly, Vector3D lightDirection, Color lightColor, Color ambientLight) {	    
		    Vector3D Vectornormal = poly.getNormal();
		    double cos = Vectornormal.cosTheta(lightDirection);
		    
		    int red, green, blue;
		    if (cos > 0) {
		        red = (int) (poly.reflectance.getRed() / 255.0f * 
		                (ambientLight.getRed() + lightColor.getRed() * cos));
		        green = (int) (poly.reflectance.getGreen()  / 255.0f * 
	                    (ambientLight.getGreen()+ lightColor.getGreen() * cos));
		        blue = (int) (poly.reflectance.getBlue() / 255.0f * 
	                    (ambientLight.getBlue()+ lightColor.getBlue() * cos));
		    } else {
		        red = (int) (poly.reflectance.getRed() / 255.0f * ambientLight.getRed());
	            green = (int) (poly.reflectance.getGreen() / 255.0f * ambientLight.getGreen());
	            blue = (int) (poly.reflectance.getBlue() / 255.0f * ambientLight.getBlue());
		    }
		    
		    red = red > 255 ? 255 : red;
		    green = green > 255 ? 255 : green;
		    blue = blue > 255 ? 255 : blue;
		    
		    return new Color(red, green, blue);
	}

	/**
	 * This method should rotate the polygons and light such that the viewer is
	 * looking down the Z-axis. The idea is that it returns an entirely new
	 * Scene object, filled with new Polygons, that have been rotated.
	 * 
	 * @param scene
	 *            The original Scene.
	 * @param xRot
	 *            An angle describing the viewer's rotation in the YZ-plane (i.e
	 *            around the X-axis).
	 * @param yRot
	 *            An angle describing the viewer's rotation in the XZ-plane (i.e
	 *            around the Y-axis).
	 * @return A new Scene where all the polygons and the light source have been
	 *         rotated accordingly.
	 */
	public static Scene rotateScene(Scene scene, float xRot, float yRot) {
		// TODO fill this in.
		Transform rotateMatrix = Transform.newXRotation(xRot).compose(Transform.newYRotation(yRot));
		 return Matrix(scene, rotateMatrix);
	}

	/**
	 * This should translate the scene by the appropriate amount.
	 * 
	 * @param scene
	 * @return
	 */
	public static Scene translateScene(Scene scene,float x, float y, float z) {
		// TODO fill this in.
		Transform translateMatrix = Transform.newTranslation(x, y, z);
		return Matrix(scene, translateMatrix);
	}

	/**
	 * This should scale the scene.
	 * 
	 * @param scene
	 * @return
	 */
	public static Scene scaleScene(Scene scene,float x, float y, float z) {
		Transform scaleMatrix = Transform.newScale(x, y, z);
		 return Matrix(scene, scaleMatrix);
	}
	private static Scene Matrix(Scene scene, Transform matrix) {
	    
	    // keep the light in the same place
	 	Vector3D LightPosition = scene.getLight();
	    
	    // process the polygons
	    List<Polygon> PolygonList = new ArrayList<>();
	    for (Polygon p : scene.getPolygons()) {
	        Vector3D[] vectors = new Vector3D[3];
	        for (int i = 0; i < vectors.length; i++) {
	            vectors[i] = matrix.multiply(p.vertices[i]);
	        }
	        Polygon processedPolygon = new Polygon(vectors[0], vectors[1], 
	                vectors[2], p.reflectance);
	        PolygonList.add(processedPolygon);
	    }
	    
	    return new Scene(PolygonList, LightPosition);
	};
	
	public static Scene Translate(Scene scene, float[] sceneBorder, Dimension dimension) {

        float left = sceneBorder[0];
        float right = sceneBorder[1];
        float up = sceneBorder[2];
        float down = sceneBorder[3];
        
        float objWidth = right - left;
        float objHeight = down - up;
        int Drawing_Width = dimension.width;
        int Drawing_Height = dimension.height;
        
        //how much to move horizontally
        float MiddleX = (Drawing_Width - objWidth) / 2;
        float MoveHorizontal = MiddleX - left;
        
        //how much to move vertically
        float MiddleY = (Drawing_Height - objHeight) / 2;
        float MoveVertical = MiddleY - up;
        
        Transform translateMatrix = Transform.newTranslation(MoveHorizontal, MoveVertical, 0f);
        return Matrix(scene, translateMatrix);
    }
	
	public static Scene Scale(Scene scene, float[] sceneBorder, Dimension dimension) {

        float left = sceneBorder[0];
        float right = sceneBorder[1];
        float up = sceneBorder[2];
        float down = sceneBorder[3];
        float NearUser = sceneBorder[4];
        float AwayFromUser = sceneBorder[5];
        
        float objWidth = right - left;
        float objHeight = down - up;
        float objdepth = AwayFromUser - NearUser;
        int Drawin_Width = dimension.width;
        int Drawing_Height = dimension.height;

        float HorizontalRatio = Drawin_Width / 2 / objWidth;
        float VerticalRatio = Drawing_Height / 2 / objHeight;
        float DepthRatio = Math.min(Drawin_Width, Drawing_Height) / 2 / objdepth;
        
        float scale = Math.min(Math.min(HorizontalRatio, VerticalRatio), DepthRatio);
        Transform scaleMatrix = Transform.newScale(scale, scale, scale);
        return Matrix(scene, scaleMatrix);
    }
	
	public static Scene Scale_Translate(Scene scene, float[] sceneBorder, Dimension dimension) {

	    float left = sceneBorder[0];
	    float right = sceneBorder[1];
	    float up = sceneBorder[2];
	    float down = sceneBorder[3];
	    float NearUser = sceneBorder[4];
        float FarFromUser = sceneBorder[5];
	    
	    float objWidth = right - left;
	    float objHeight = down - up;
	    float objdepth = FarFromUser - NearUser;
	    int Drawing_Width = dimension.width;
	    int Drawing_Height = dimension.height;

        float ratioHorizontal = Drawing_Width / 2 / objWidth;
        float ratioVertical = Drawing_Height / 2 / objHeight;
        float ratioDepth = Math.min(Drawing_Width, Drawing_Height) / 2 / objdepth;
        
        float scale = Math.min(Math.min(ratioHorizontal, ratioVertical), ratioDepth);
        Transform scaleMatrix = Transform.newScale(scale, scale, scale);
        Scene scaledScene = Matrix(scene, scaleMatrix);
        
        // Auto-translate
        float scaledLeft = left * scale;
        float scaledUp = up * scale;
        
        // work out how much to shift horizontally
        float scaledObjectWidth = objWidth * scale;
        float centralPosX = (Drawing_Width - scaledObjectWidth) / 2;
        float horizontalShift = centralPosX - scaledLeft;
        
        // work out how much to shift vertically
        float scaledObjectHeight = objHeight * scale;
        float centralPosY = (Drawing_Height - scaledObjectHeight) / 2;
        float verticalShift = centralPosY - scaledUp;
        
        Transform translationMatrix = Transform.newTranslation(horizontalShift, verticalShift, 0f);
        return Matrix(scaledScene, translationMatrix);
    }

	

	/**
	 * Computes the edgelist of a single provided polygon, as per the lecture
	 * slides.
	 */
	public static EdgeList computeEdgeList(Polygon poly) {
		// TODO fill this in.
		Vector3D firstVertex = poly.vertices[0];
	    Vector3D secondVertex = poly.vertices[1];
	    Vector3D thirdVertex = poly.vertices[2];
	    int startY = (int) Math.min(Math.min(firstVertex.y, secondVertex.y), thirdVertex.y);
        int endY = (int) Math.max(Math.max(firstVertex.y, secondVertex.y), thirdVertex.y);
	    
        EdgeList edgeList = new EdgeList(startY, endY);
        
	    for (int i = 0; i < poly.vertices.length; i++) {
	        int j = i + 1;
	        j = j == 3 ? 0 : j; 
	        Vector3D Up;
            Vector3D Down;
            
            if (poly.vertices[i].y == poly.vertices[j].y) {
                continue; // Same Y value
            } else if (poly.vertices[i].y > poly.vertices[j].y) {
	            Up = poly.vertices[j];
	            Down = poly.vertices[i];
	        } else {
	            Up = poly.vertices[i];
                Down = poly.vertices[j];
	        }
	        
            float middlex = (Down.x - Up.x) / (Down.y - Up.y);
            float middlez = (Down.z - Up.z) / (Down.y - Up.y);
            
            float x = Up.x;
	        float z = Up.z;
	        
	        int y = (int) Up.y;
	        int yEnd = (int) Down.y;   
	        
	        for ( ; y < yEnd; y++, x += middlex, z += middlez) {
	            edgeList.addRow(y - startY, x, z);
	        }
	    }	    
		return edgeList;
	}

	/**
	 * Fills a zbuffer with the contents of a single edge list according to the
	 * lecture slides.
	 * 
	 * The idea here is to make zbuffer and zdepth arrays in your main loop, and
	 * pass them into the method to be modified.
	 * 
	 * @param zbuffer
	 *            A double array of colours representing the Color at each pixel
	 *            so far.
	 * @param zdepth
	 *            A double array of floats storing the z-value of each pixel
	 *            that has been coloured in so far.
	 * @param polyEdgeList
	 *            The edgelist of the polygon to add into the zbuffer.
	 * @param polyColor
	 *            The colour of the polygon to add into the zbuffer.
	 */
	public static void computeZBuffer(Color[][] zbuffer, float[][] zdepth, EdgeList polyEdgeList, Color polyColor) {
		// TODO fill this in.   
	 	    int startY = polyEdgeList.getStartY();
	 	    int endY = polyEdgeList.getEndY();
	 	    int height = endY - startY;
	 	    
	 	    int y = 0;
	 	    while (y < height) {
	 	        
	 	        // do not render pixels don't need to be drawn
	 	        if (y + startY < 0 || y + startY >= zbuffer[0].length) {
	 	            y++;
	 	            continue;
	 	        }
	 	        
	 	        int x = (int) polyEdgeList.getLeftX(y);
	 	        int rightX = (int) polyEdgeList.getRightX(y);
	 	        float z = polyEdgeList.getLeftZ(y);
	 	        float rightZ = polyEdgeList.getRightZ(y);
	 	        float middleZ = (rightZ - z) / (rightX - x);
	 	        
	 	        while (x < rightX) {
	 	            // don't draw unnecessary pixels 
	 	            if (x < 0 || x >= zbuffer.length) {
	 	                z += middleZ;
	 	                x++;
	 	                continue;
	 	            }           
	 	            if (z < zdepth[x][y + startY]) {
	 	                zdepth[x][y + startY] = z;
	 	                zbuffer[x][y + startY] = polyColor;
	 	            }
	 	            z += middleZ;
	 	            x++;
	 	        }
	 	        y++;
	 	    }
	 	}
	}


// code for comp261 assignments

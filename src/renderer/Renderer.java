package renderer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import renderer.Scene.Polygon;




public class Renderer extends GUI {
	public List<Vector3D> Lights;
	private Scene scene; 
	public float Xrotate = 0f;
	public float yRotate = 0f;
	public Vector3D viewerPos = new Vector3D(0f, 0f, 0f);
	public float currentScale = 1.0f;
	public boolean Rotate = true;
	public Scene CenteredScene;
	private float translateBy = 2.5f;
	private static final float ZOOM_FACTOR = 1.6f;
	private static final float Min_ZOOM = 0.4f;
	private static final float Max_Zoom = 6.0f;
	
	@Override
	protected void onLoad(File file) {
		// TODO fill this in.

		/*
		 * This method should parse the given file into a Scene object, which
		 * you store and use to render an image.
		 */
		  Xrotate = yRotate = 0f;
		  viewerPos = new Vector3D(0f, 0f, 0f);
		  currentScale = 1.0f;
		  Rotate = true;
		  CenteredScene = null;
		    
		  BufferedReader fileScanner;
		  List<Polygon> polygons = new ArrayList<>();
		  Vector3D lightPos;
		  
		  try {
	          fileScanner = new BufferedReader(new FileReader(file));
	          String line = fileScanner.readLine();
	      
	          if (line == null) {
	              System.out.println("Empty source file.");
	              fileScanner.close();
	              return;
	          }
	            
	          String[] values = line.split(" ");
	          float x = Float.parseFloat(values[0]);
	          float y = Float.parseFloat(values[1]);
	          float z = Float.parseFloat(values[2]);
	          lightPos = new Vector3D(x, y, z);         
	          line = fileScanner.readLine();
	          
	          // parse polygons one by one
	          while (line != null) {
	              values = line.split(" ");
	              float a_x = Float.parseFloat(values[0]);
	              float a_y = Float.parseFloat(values[1]);
	              float a_z = Float.parseFloat(values[2]);
	              float b_x = Float.parseFloat(values[3]);
	              float b_y = Float.parseFloat(values[4]);
	              float b_z = Float.parseFloat(values[5]);
	              float c_x = Float.parseFloat(values[6]);
	              float c_y = Float.parseFloat(values[7]);
	              float c_z = Float.parseFloat(values[8]);
	              float[] points = new float[]{a_x, a_y, a_z, b_x, b_y, b_z, c_x, c_y, c_z};
	              int Red = Integer.parseInt(values[9]);
	              int Green = Integer.parseInt(values[10]);
	              int Blue = Integer.parseInt(values[11]);
	              int[] color = new int[]{Red, Green, Blue};
	              polygons.add(new Polygon(points, color));          
	              line = fileScanner.readLine();
	          }
	          fileScanner.close();
	          this.scene = new Scene(polygons, lightPos);
	      } catch (FileNotFoundException e) {
	          System.err.println("FileNotFoundException");
	      } catch (IOException e) {
	          System.err.println("IOException");
	      }
	}
	
	@Override
	protected BufferedImage render() {
		// TODO fill this in.

		/*
		 * This method should put together the pieces of your renderer, as
		 * described in the lecture. This will involve calling each of the
		 * static method stubs in the Pipeline class, which you also need to
		 * fill in.
		 */
		Color[][] zbuffer = new Color[CANVAS_WIDTH][CANVAS_HEIGHT];
	    float[][] zdepth = new float[CANVAS_WIDTH][CANVAS_HEIGHT];
	    
	    // initialise all pixels as default color
	    Color Background = new Color(130, 130, 130);
	    for (int i = 0; i < zbuffer.length; i++) {
	        for (int j = 0; j < zbuffer[i].length; j++) {
	            zbuffer[i][j] = Background;
	        }
	    }
	    
	    if (scene == null) {
	        return convertBitmapToImage(zbuffer);
	    }
	    
	    Dimension dimension = DRAWING_SIZE;
	    if (CenteredScene == null) {
	        // scale the scene to fit in the canvas
	        float[] boundary = scene.getBorder();
	        CenteredScene = Pipeline.Scale_Translate(scene, boundary, dimension);
	    }
	    
	    // rotate
	    Scene rotatedScene = Pipeline.rotateScene(CenteredScene, Xrotate, yRotate);
	    
	    //scale
	    Scene scaledScene = Pipeline.scaleScene(rotatedScene, currentScale, currentScale, currentScale);
	    
	    float[] newBorder = scaledScene.getBorder();
	    Scene reCenterScene = Pipeline.Translate(scaledScene, newBorder, dimension);
	    
	    // translate towards the user
	    Scene translatedScene = Pipeline.translateScene(reCenterScene,
	            viewerPos.x, viewerPos.y, viewerPos.z);

	    for (int i = 0; i < zdepth.length; i++) {
            for (int j = 0; j < zdepth[i].length; j++) {
                zdepth[i][j] = Float.POSITIVE_INFINITY;
            }
        }
	    
	    // update colors in zbuffer
	    int[] ambient  = getAmbientLight();
	    Color Light = new Color(0,255,128);
	    float AmbientRed = ambient[0]/255f;
	    float AmbientGreen = ambient[1]/255f;
	    float AmbientBlue = ambient[2]/255f;  
	    Color AmbLight = new Color(AmbientRed,AmbientGreen,AmbientBlue);
	    Vector3D vecotrLight = translatedScene.getLight();
	    List<Polygon> polygonsList = translatedScene.getPolygons();
	    for (Polygon p : polygonsList) {
	        if (Pipeline.isHidden(p)) {
	            continue;
	        }
	        
	        Color shading = Pipeline.getShading(p, vecotrLight,Light, AmbLight);
	        EdgeList edgeList = Pipeline.computeEdgeList(p);
	        Pipeline.computeZBuffer(zbuffer, zdepth, edgeList, shading);
	    }

	    return convertBitmapToImage(zbuffer);
	}

	/**
	 * Converts a 2D array of Colors to a BufferedImage. Assumes that bitmap is
	 * indexed by column then row and has imageHeight rows and imageWidth
	 * columns. Note that image.setRGB requires x (col) and y (row) are given in
	 * that order.
	 */
	private BufferedImage convertBitmapToImage(Color[][] bitmap) {
		BufferedImage image = new BufferedImage(CANVAS_WIDTH, CANVAS_HEIGHT, BufferedImage.TYPE_INT_RGB);
		for (int x = 0; x < CANVAS_WIDTH; x++) {
			for (int y = 0; y < CANVAS_HEIGHT; y++) {
				image.setRGB(x, y, bitmap[x][y].getRGB());
			}
		}
		return image;
	}

	public static void main(String[] args) {
		new Renderer();
	}
	private void rotateX(float n) {
	    Xrotate -= n;
	}
	
	private void rotateY(float n) {
	    yRotate -= n;
	}
	private void Up(float n) {
	    viewerPos = viewerPos.minus(new Vector3D(0f, n, 0f));
	}

    private void Left(float n) {
        viewerPos = viewerPos.minus(new Vector3D(n, 0f, 0f));
    }
    
	@Override
	protected void onKeyPress(KeyEvent e) {
		// TODO Auto-generated method stub
		char c = e.getKeyChar();
		float rotationAngle = 0.1f;
	    // Rotation
	    if (e.getKeyCode() == KeyEvent.VK_UP) {
	    	rotateX(rotationAngle);
        } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            rotateX(-rotationAngle);
        } else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            rotateY(-rotationAngle);
        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            rotateY(rotationAngle);
            
        // Translation
        } else if (c == 'w' || c == 'W') {
            Up(translateBy);
        } else if (c == 's' || c == 'S') {
            Up(-translateBy);
        } else if (c == 'a' || c == 'A') {
            Left(translateBy);
        } else if (c == 'd' || c == 'D') {
            Left(-translateBy);
        } 
	  
	}

	 private void zoom(float scale) {
	        currentScale *= scale;
	        if (currentScale < Min_ZOOM) {
	            currentScale = Min_ZOOM;
	        } else if (currentScale > Max_Zoom) {
	            currentScale = Max_Zoom;
	        }
	    }
	 
	@Override
	protected void onScroll(MouseWheelEvent e) {
		// TODO Auto-generated method stub
		int i = e.getWheelRotation();
        if (i > 0) {
        	   zoom(1.0f / ZOOM_FACTOR);
        } else {
            zoom(ZOOM_FACTOR);
        }
	}

	@Override
	protected void onLightAdd() {

		float x = (float) ((Math.random() - 0.5) * 2);
		float y = (float) ((Math.random() - 0.5) * 2);
		float z = (float) ((Math.random() - 0.5) * 2);
		Lights.add(new Vector3D(x, y, z));
		System.out.printf("ADDED A LIGHT: %f %f %f\n", x, y, z);
	}
}

// code for comp261 assignments

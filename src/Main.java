import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;


public class Main extends Application {

    private int width = 1100;
    private int height = 650;

    private final int numBoids = 100;
    private final int visualRange = 80;

    private Boid[] boids = new Boid[numBoids];
    private GraphicsContext gc;
    private boolean drawTrial = false;

    //Controlling factors
    double centeringFactor = 0.005;
    int minDistance = 15;
    double avoidFactor = 0.05;
    double matchingFactor = 0.05;
    int speedLimit = 15;

    @Override
    public void start(Stage primaryStage) {
        HBox vbox = new HBox();

        Canvas canvas = new Canvas(width,height);
        gc = canvas.getGraphicsContext2D();
        initBoids();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long l) {
                animationLoop();
            }
        };
        timer.start();

        vbox.getChildren().addAll(canvas, buildSlidersBox());
        vbox.setBackground(new Background(new BackgroundFill(Color.web("#282B34"), CornerRadii.EMPTY, Insets.EMPTY)));

        primaryStage.setTitle("Boids");
        primaryStage.setScene(new Scene(vbox));
        //Currently Resizing with canvas doesn't work
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public VBox buildSlidersBox(){
        VBox vBox = new VBox();

        Label centeringFactorLabel = new Label("Centering Factor");
        centeringFactorLabel.setTextFill(Color.web("#fff"));
        Label separationLabel = new Label("Minimum Distance");
        separationLabel.setTextFill(Color.web("#fff"));
        Label matchingFactorLabel = new Label("Matching Factor");
        matchingFactorLabel.setTextFill(Color.web("#fff"));
        Label speedLimitLabel = new Label("Speed Limit");
        speedLimitLabel.setTextFill(Color.web("#fff"));
        Label avoidFactorLabel = new Label("Avoid Factor");
        avoidFactorLabel.setTextFill(Color.web("#fff"));

        Slider centeringFactorSlider = new Slider(0.003, 0.007, 0.005);
        Slider minDistanceSlider = new Slider(10, 20, 15);
        Slider matchingFactorSlider = new Slider(0.03, 0.07, 0.05);
        Slider speedLimitSlider = new Slider(10, 25, 15);
        Slider avoidFactorSlider = new Slider(0.03, 0.07, 0.05);


        centeringFactorSlider.valueProperty().addListener((observableValue, oldValue, newValue) -> {
            System.out.println("Centering Factor - observableValue: " + observableValue.getValue() + ", " + "oldValue: "+ oldValue +", " + "newValue: " + newValue);
            centeringFactor = newValue.doubleValue();
        });

        minDistanceSlider.valueProperty().addListener((observableValue, oldValue, newValue) -> {
            System.out.println("MinDistance - observableValue: " + observableValue.getValue() + ", " + "oldValue: "+ oldValue +", " + "newValue: " + newValue);
            minDistance = newValue.intValue();
        });

        matchingFactorSlider.valueProperty().addListener((observableValue, oldValue, newValue) -> {
            System.out.println("Matching Factor - observableValue: " + observableValue.getValue() + ", " + "oldValue: "+ oldValue +", " + "newValue: " + newValue);
            matchingFactor = newValue.doubleValue();
        });

        speedLimitSlider.valueProperty().addListener((observableValue, oldValue, newValue) -> {
            System.out.println("SpeedLimit - observableValue: " + observableValue.getValue() + ", " + "oldValue: "+ oldValue +", " + "newValue: " + newValue);
            speedLimit = newValue.intValue();
        });

        avoidFactorSlider.valueProperty().addListener((observableValue, oldValue, newValue) -> {
            System.out.println("Avoid Factor - observableValue: " + observableValue.getValue() + ", " + "oldValue: "+ oldValue +", " + "newValue: " + newValue);
            avoidFactor = newValue.doubleValue();
        });

        vBox.getChildren().addAll(centeringFactorLabel, centeringFactorSlider, separationLabel,
                minDistanceSlider, matchingFactorLabel, matchingFactorSlider,speedLimitLabel, speedLimitSlider, avoidFactorLabel, avoidFactorSlider);
        return vBox;
    }

    public void initBoids() {
        for (int i = 0; i < numBoids; i += 1) {
            boids[i] = new Boid(
                    Math.random() * width,
                    Math.random() * height,
                    Math.random() * 10 - 5,
                    Math.random() * 10 - 5,
                    new Point2D[100]
            );
        }
    }

    public double distance(Boid boid1, Boid boid2) {
        return Math.sqrt((boid1.x - boid2.x) * (boid1.x - boid2.x) + (boid1.y - boid2.y) * (boid1.y - boid2.y));
    }

    //Currently not used so why spent so much time?
    /*public Boid[] nClosestBoids(Boid boid, int n) {
        Boid[] sorted = new Boid[boids.length];
        System.arraycopy(boids, 0, sorted, 0, boids.length);


        return sorted;
    }*/

    public void keepWithinBounds(Boid boid) {
        int margin = 100;
        int turnFactor = 1;

        if (boid.x < margin) {
            boid.dx += turnFactor;
        }
        if (boid.x > width - margin) {
            boid.dx -= turnFactor;
        }
        if (boid.y < margin) {
            boid.dy += turnFactor;
        }
        if (boid.y > height - margin) {
            boid.dy -= turnFactor;
        }
    }

    public void flyTowardsCenter(Boid boid) {
        double centerX = 0;
        double centerY = 0;
        int numNeighbors = 0;

        for (Boid otherBoid : boids) {
            if (distance(boid, otherBoid) < visualRange) {
                centerX += otherBoid.x;
                centerY += otherBoid.y;
                numNeighbors += 1;
            }
        }

        if (numNeighbors != 0) {
            centerX = centerX / numNeighbors;
            centerY = centerY / numNeighbors;

            boid.dx += (centerX - boid.x) * centeringFactor;
            boid.dy += (centerY - boid.y) * centeringFactor;
        }
    }

    public void avoidOthers(Boid boid) {
        double moveX = 0;
        double moveY = 0;

        for (Boid otherBoid : boids) {
            if (otherBoid != boid) {
                if (distance(boid, otherBoid) < minDistance) {
                    moveX += boid.x - otherBoid.x;
                    moveY += boid.y - otherBoid.y;
                }
            }
        }

        boid.dx += moveX * avoidFactor;
        boid.dy += moveY * avoidFactor;
    }

    public void mathVelocity(Boid boid) {
        double avgDX = 0;
        double avgDY = 0;
        int numNeighbors = 0;

        for (Boid otherBoid : boids) {
            if (distance(boid, otherBoid) < visualRange) {
                avgDX += otherBoid.dx;
                avgDY += otherBoid.dy;
                numNeighbors += 1;
            }
        }

        if (numNeighbors != 0) {
            avgDX = avgDX / numNeighbors;
            avgDY = avgDY / numNeighbors;

            boid.dx += (avgDX - boid.dx) * matchingFactor;
            boid.dy += (avgDY - boid.dy) * matchingFactor;
        }
    }

    public void limitSpeed(Boid boid) {
        double speed = Math.sqrt(boid.dx * boid.dx + boid.dy * boid.dy);
        if (speed > speedLimit) {
            boid.dx = (boid.dx / speed) * speedLimit;
            boid.dy = (boid.dy / speed) * speedLimit;
        }
    }

    public void drawGraphics(GraphicsContext ctx, Boid boid) {
        double angle = Math.atan2(boid.dy, boid.dx);
        ctx.translate(boid.x, boid.y);
        ctx.rotate(angle);
        ctx.translate(-boid.x, -boid.y);
        Color fillColor = Color.web("#558cf4");
        Color storkeColor = Color.web("#558cf466");

        ctx.setFill(fillColor);

        ctx.beginPath();
        ctx.moveTo(boid.x, boid.y);
        ctx.lineTo(boid.x - 15, boid.y + 5);
        ctx.lineTo(boid.x - 15, boid.y - 5);
        ctx.lineTo(boid.x, boid.y);
        ctx.fill();
        ctx.setTransform(1, 0, 0, 1, 0, 0);

        if (drawTrial) {
            ctx.setStroke(storkeColor);
            ctx.beginPath();
            ctx.moveTo(boid.history[0].getX(), boid.history[0].getY());
            for (Point2D point : boid.history) {
                ctx.lineTo(point.getX(), point.getY());
            }
            ctx.stroke();
        }
    }


    public void animationLoop() {
        for (Boid boid : boids) {
            flyTowardsCenter(boid);
            avoidOthers(boid);
            mathVelocity(boid);
            limitSpeed(boid);
            keepWithinBounds(boid);

            boid.x += boid.dx;
            boid.y += boid.dy;

            push(boid.history, boid.x, boid.y);
            boid.history = slice(boid.history, boid.history.length - 50, 0);
        }

        gc.clearRect(0, 0, width, height);
        for (Boid boid : boids) {
            drawGraphics(gc, boid);
        }

    }

    public Point2D[] push(Point2D[] arr, double x, double y) {
        Point2D[] longer = new Point2D[arr.length + 1];
        System.arraycopy(arr, 0, longer, 0, arr.length);
        longer[arr.length] = new Point2D(x, y);
        return longer;
    }

    public Point2D[] slice(Point2D[] arr, int start, int end) {
        if (arr.length > 50) {
            Point2D[] slice = new Point2D[start - end];

            for (int i = 0; i < slice.length; i++) {
                slice[i] = arr[start + i];
            }

            return slice;
        }
        return arr;
    }

    public static void main(String[] args) {
        launch(args);
    }
}

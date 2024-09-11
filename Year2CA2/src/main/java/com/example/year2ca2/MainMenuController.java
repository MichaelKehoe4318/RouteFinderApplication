package com.example.year2ca2;

import javafx.event.ActionEvent;
import javafx.scene.control.Slider;
import javafx.scene.image.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.*;

public class MainMenuController {
    public ImageView origImage;
    public ImageView editedImage;
    public ImageView pathOverlayImageView;
    public Text imageInfo;
    public Slider redValue;
    public Slider greenValue;
    public Slider blueValue;

    private File selectedFile;
    private Graph graph = new Graph();
    private WritableImage pathOverlayImage;

    // Track the last placed node
    private Node lastNode = null;

    public void initialize() {
        origImage.setOnMouseClicked(this::handleMouseClick);
        editedImage.setOnMouseClicked(this::addNode);
        pathOverlayImage = new WritableImage(1, 1); // Placeholder initialization
        pathOverlayImageView.setImage(pathOverlayImage);
    }

    public void openImagePicker(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Image");

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.bmp", "*.gif")
        );

        selectedFile = fileChooser.showOpenDialog(origImage.getScene().getWindow());

        if (selectedFile != null) {
            Image image = new Image(selectedFile.toURI().toString());
            origImage.setImage(image);
            editedImage.setImage(image);

            // Initialize the path overlay image to the size of the original image
            pathOverlayImage = new WritableImage((int) image.getWidth(), (int) image.getHeight());
            pathOverlayImageView.setImage(pathOverlayImage);

            updateImageInfo(image);
        }
    }

    public void getImageInfo(ActionEvent actionEvent) {
        Image image = origImage.getImage();
        if (image != null) {
            updateImageInfo(image);
        }
    }

    private void updateImageInfo(Image image) {
        double width = image.getWidth();
        double height = image.getHeight();
        String filename = (selectedFile != null) ? selectedFile.getName() : "Unknown";

        imageInfo.setText(String.format("File: %s\nWidth: %.2f px\nHeight: %.2f px", filename, width, height));
    }

    public void greyscaleImage(ActionEvent actionEvent) {
        Image image = origImage.getImage();

        if (image != null) {
            WritableImage greyscaleImage = new WritableImage((int) image.getWidth(), (int) image.getHeight());
            PixelReader pixelReader = image.getPixelReader();
            PixelWriter pixelWriter = greyscaleImage.getPixelWriter();

            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    Color color = pixelReader.getColor(x, y);
                    double greyscale = (color.getRed() + color.getGreen() + color.getBlue()) / 3;
                    Color greyscaleColor = new Color(greyscale, greyscale, greyscale, color.getOpacity());
                    pixelWriter.setColor(x, y, greyscaleColor);
                }
            }

            editedImage.setImage(greyscaleImage);
        }
    }

    public void openColourChannels(ActionEvent actionEvent) {
        Image image = origImage.getImage();

        if (image != null) {
            WritableImage colorAdjustedImage = new WritableImage((int) image.getWidth(), (int) image.getHeight());
            PixelReader pixelReader = image.getPixelReader();
            PixelWriter pixelWriter = colorAdjustedImage.getPixelWriter();

            double redAdjust = redValue.getValue();
            double greenAdjust = greenValue.getValue();
            double blueAdjust = blueValue.getValue();

            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    Color color = pixelReader.getColor(x, y);
                    double newRed = Math.min(1.0, color.getRed() * redAdjust);
                    double newGreen = Math.min(1.0, color.getGreen() * greenAdjust);
                    double newBlue = Math.min(1.0, color.getBlue() * blueAdjust);
                    Color adjustedColor = new Color(newRed, newGreen, newBlue, color.getOpacity());
                    pixelWriter.setColor(x, y, adjustedColor);
                }
            }

            editedImage.setImage(colorAdjustedImage);
        }
    }

    public void handleMouseClick(MouseEvent event) {
        Image image = origImage.getImage();
        if (image == null) return;

        double x = event.getX();
        double y = event.getY();

        // Convert click coordinates to image coordinates
        double imgX = x / origImage.getFitWidth() * image.getWidth();
        double imgY = y / origImage.getFitHeight() * image.getHeight();

        // Detect the color at the clicked position
        Color colorToKeep = getColorAtPosition(imgX, imgY, image);

        // Create a new image with the detected color turned white and everything else black
        Image modifiedImage = createModifiedImage(colorToKeep, image);
        editedImage.setImage(modifiedImage);
    }

    private Color getColorAtPosition(double x, double y, Image image) {
        PixelReader pixelReader = image.getPixelReader();
        int ix = (int) Math.min(Math.max(x, 0), image.getWidth() - 1);
        int iy = (int) Math.min(Math.max(y, 0), image.getHeight() - 1);
        return pixelReader.getColor(ix, iy);
    }

    private Image createModifiedImage(Color colorToKeep, Image originalImage) {
        int width = (int) originalImage.getWidth();
        int height = (int) originalImage.getHeight();

        WritableImage writableImage = new WritableImage(width, height);
        PixelWriter pixelWriter = writableImage.getPixelWriter();
        PixelReader pixelReader = originalImage.getPixelReader();

        // Initialize the image with black color
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color currentColor = pixelReader.getColor(x, y);
                if (colorsAreSimilar(currentColor, colorToKeep)) {
                    pixelWriter.setColor(x, y, Color.WHITE);
                } else {
                    pixelWriter.setColor(x, y, Color.BLACK);
                }
            }
        }

        return writableImage;
    }

    private boolean colorsAreSimilar(Color color1, Color color2) {
        final double tolerance = 0.15;
        return Math.abs(color1.getRed() - color2.getRed()) < tolerance &&
                Math.abs(color1.getGreen() - color2.getGreen()) < tolerance &&
                Math.abs(color1.getBlue() - color2.getBlue()) < tolerance;
    }

    public void addNode(MouseEvent mouseEvent) {
        Image image = editedImage.getImage();
        if (image == null) return;

        double x = mouseEvent.getX();
        double y = mouseEvent.getY();

        // Convert click coordinates to image coordinates
        double imgX = x / editedImage.getFitWidth() * image.getWidth();
        double imgY = y / editedImage.getFitHeight() * image.getHeight();

        // Create a new node and add it to the graph
        Node newNode = new Node((int) imgX, (int) imgY);
        graph.addNode(newNode);

        // Automatically add an edge to the last placed node
        if (lastNode != null) {
            graph.addEdge(lastNode, newNode);
        }

        // Update the lastNode reference
        lastNode = newNode;

        // Generate edges based on white paths and then redraw
        generateEdgesFromPaths();

        // Draw the nodes and lines on the overlay image
        drawNodesAndLinesOnOverlay();
    }

    private void drawNodesAndLinesOnOverlay() {
        int width = (int) pathOverlayImage.getWidth();
        int height = (int) pathOverlayImage.getHeight();
        PixelWriter pixelWriter = pathOverlayImage.getPixelWriter();

        // Clear the overlay image
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pixelWriter.setColor(x, y, Color.TRANSPARENT);
            }
        }

        // Draw each node as a blue dot
        for (Node node : graph.getNodes()) {
            drawDot(pixelWriter, node.getX(), node.getY(), Color.BLUE);
        }

        // Draw lines connecting nodes
        for (Edge edge : graph.getEdges()) {
            drawLine(pixelWriter, edge.getStart().getX(), edge.getStart().getY(), edge.getEnd().getX(), edge.getEnd().getY(), Color.BLUE);
        }
    }

    private void drawDot(PixelWriter pixelWriter, int x, int y, Color color) {
        int radius = 5; // Radius of the dot
        for (int dy = -radius; dy <= radius; dy++) {
            for (int dx = -radius; dx <= radius; dx++) {
                if (dx * dx + dy * dy <= radius * radius) {
                    int px = x + dx;
                    int py = y + dy;
                    if (px >= 0 && px < pathOverlayImage.getWidth() && py >= 0 && py < pathOverlayImage.getHeight()) {
                        pixelWriter.setColor(px, py, color);
                    }
                }
            }
        }
    }

    private void drawLine(PixelWriter pixelWriter, int x1, int y1, int x2, int y2, Color color) {
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int sx = (x1 < x2) ? 1 : -1;
        int sy = (y1 < y2) ? 1 : -1;
        int err = dx - dy;

        while (true) {
            if (x1 >= 0 && x1 < pathOverlayImage.getWidth() && y1 >= 0 && y1 < pathOverlayImage.getHeight()) {
                pixelWriter.setColor(x1, y1, color);
            }
            if (x1 == x2 && y1 == y2) break;
            int e2 = err * 2;
            if (e2 > -dy) {
                err -= dy;
                x1 += sx;
            }
            if (e2 < dx) {
                err += dx;
                y1 += sy;
            }
        }
    }

    private void generateEdgesFromPaths() {
        List<Node> nodes = graph.getNodes();
        if (nodes.isEmpty()) return;

        Image image = editedImage.getImage();
        PixelReader pixelReader = image.getPixelReader();

        for (int i = 0; i < nodes.size() - 1; i++) {
            Node startNode = nodes.get(i);

            for (int j = i + 1; j < nodes.size(); j++) {
                Node endNode = nodes.get(j);

                // Perform BFS to find if there's a white path connecting the nodes
                if (bfsPathExists(startNode, endNode, pixelReader, (int) image.getWidth(), (int) image.getHeight())) {
                    graph.addEdge(startNode, endNode);

                }
            }
        }
    }

    private boolean bfsPathExists(Node startNode, Node endNode, PixelReader pixelReader, int width, int height) {
        boolean[][] visited = new boolean[width][height];
        Queue<Node> queue = new LinkedList<>();
        queue.add(startNode);
        visited[startNode.getX()][startNode.getY()] = true;

        int[] dx = {-1, 1, 0, 0};
        int[] dy = {0, 0, -1, 1};

        while (!queue.isEmpty()) {
            Node current = queue.poll();
            int cx = current.getX();
            int cy = current.getY();

            // If we reached the end node
            if (cx == endNode.getX() && cy == endNode.getY()) {
                return true;
            }

            // Explore neighbors
            for (int i = 0; i < 4; i++) {
                int nx = cx + dx[i];
                int ny = cy + dy[i];

                if (nx >= 0 && ny >= 0 && nx < width && ny < height && !visited[nx][ny]) {
                    Color color = pixelReader.getColor(nx, ny);
                    if (color.equals(Color.WHITE)) {
                        visited[nx][ny] = true;
                        queue.add(new Node(nx, ny));
                    }
                }
            }
        }

        return false;
    }

    public void exitApplication(ActionEvent actionEvent) {
        Stage stage = (Stage) origImage.getScene().getWindow();
        stage.close();
    }

    public void ClearNodes(ActionEvent actionEvent) {
        graph = new Graph(); // Reset the graph
        lastNode = null; // Reset lastNode reference
        drawNodesAndLinesOnOverlay(); // Redraw the overlay
    }

    private List<Node> dfsPath(Node startNode, Node endNode) {
        Stack<Node> stack = new Stack<>();
        Map<Node, Node> cameFrom = new HashMap<>();
        Set<Node> visited = new HashSet<>();

        stack.push(startNode);
        visited.add(startNode);
        cameFrom.put(startNode, null);

        while (!stack.isEmpty()) {
            Node current = stack.pop();

            // If we found the end node, reconstruct the path
            if (current.equals(endNode)) {
                return reconstructPath(cameFrom, startNode, endNode);
            }

            // Explore neighbors
            for (Node neighbor : graph.getNeighbors(current)) {
                if (!visited.contains(neighbor)) {
                    stack.push(neighbor);
                    visited.add(neighbor);
                    cameFrom.put(neighbor, current);
                }
            }
        }


        // No path found
        return null;
    }

    private List<Node> reconstructPath(Map<Node, Node> cameFrom, Node startNode, Node endNode) {
        List<Node> path = new LinkedList<>();
        Node current = endNode;

        while (current != null) {
            path.add(0, current);
            current = cameFrom.get(current);
        }

        return path;
    }

    public void drawPath(List<Node> path) {
        if (path == null || path.isEmpty()) return;

        PixelWriter pixelWriter = pathOverlayImage.getPixelWriter();

        // Draw the path as a red line on the overlay
        for (int i = 0; i < path.size() - 1; i++) {
            Node start = path.get(i);
            Node end = path.get(i + 1);
            drawLine(pixelWriter, start.getX(), start.getY(), end.getX(), end.getY(), Color.RED);
        }
    }

    public void findAndDrawPath(Node startNode, Node endNode) {
        List<Node> path = dfsPath(startNode, endNode);
        if (path != null) {
            drawPath(path);
        } else {
            System.out.println("No path found between the selected nodes.");
        }
    }



}

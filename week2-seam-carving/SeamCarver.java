import edu.princeton.cs.algs4.Picture;

import java.util.Arrays;

public class SeamCarver {
    private static final double MAX_ENERGY = 1000.0;

    private int width;
    private int height;
    private double[][] energies;
    private int[][] colors;
    private boolean isTransposed;

    // create a seam carver object based on the given picture
    public SeamCarver(Picture picture) {
        if (picture == null) {
            throw new IllegalArgumentException("Original picture is null");
        }

        this.width = picture.width();
        this.height = picture.height();

        this.isTransposed = false;
        this.energies = new double[height][width];
        this.colors = new int[height][width];

        initialize(picture);
    }

    // current picture
    public Picture picture() {
        transposeIfNeeded(false);

        Picture p = new Picture(width(), height());
        for (int y = 0; y < height(); ++y) {
            for (int x = 0; x < width(); ++x) {
                p.setRGB(x, y, getColor(x, y));
            }
        }
        return p;
    }

    // width of current picture
    public int width() {
        return width;
    }

    // height of current picture
    public int height() {
        return height;
    }

    // energy of pixel at column x and row y
    public double energy(int x, int y) {
        if (!isValidDim(x, width()) || !isValidDim(y, height())) {
            throw new IllegalArgumentException(
                    String.format("(%d; %d) is not valid pixel address", x, y));
        }

        return isTransposed ? energies[x][y] : energies[y][x];
    }

    // sequence of indices for horizontal seam
    public int[] findHorizontalSeam() {
        transposeIfNeeded(true);
        return findSeam();
    }

    // sequence of indices for vertical seam
    public int[] findVerticalSeam() {
        transposeIfNeeded(false);
        return findSeam();
    }

    // remove horizontal seam from current picture
    public void removeHorizontalSeam(int[] seam) {
        removeSeam(seam, true);
    }

    // remove vertical seam from current picture
    public void removeVerticalSeam(int[] seam) {
        removeSeam(seam, false);
    }

    private void checkSeam(int[] seam, boolean isTransposed) {
        if (seam == null) {
            throw new IllegalArgumentException("Seam is null");
        }

        final int w = width(isTransposed);
        final int h = height(isTransposed);

        if (w <= 1) {
            throw new IllegalArgumentException("Seam cannot be removed in one pixel-width image");
        }

        if (seam.length != h) {
            throw new IllegalArgumentException(
                    "Seam doesn't have exact " + w + " elements");
        }

        int prev = seam[0];
        for (int p : seam) {
            checkDim(p, w);
            if (Math.abs(prev - p) > 1) {
                throw new IllegalArgumentException(
                        "Two adjacent entries of the seam differ by more than 1");
            }
            prev = p;
        }
    }

    private boolean isValidDim(int dim, int maxValue) {
        return 0 <= dim && dim < maxValue;
    }

    private void checkDim(int x, int maxValue) {
        if (!isValidDim(x, maxValue)) {
            throw new IllegalArgumentException("Pixel coordinate is out of bound");
        }
    }

    private void transposeIfNeeded(boolean shouldBeTransposed) {
        if (shouldBeTransposed != isTransposed) {
            final int newHeight = virtualWidth();
            final int newWidth = virtualHeight();

            double[][] newEnergies = new double[newHeight][newWidth];
            int[][] newColors = new int[newHeight][newWidth];

            for (int y = 0; y < virtualHeight(); ++y) {
                for (int x = 0; x < virtualWidth(); ++x) {
                    newEnergies[x][y] = getEnergy(x, y);
                    newColors[x][y] = getColor(x, y);
                }
            }

            // StdOut.println(String.format("Transpose from WxH %d x %d  -> %d x %d", virtualWidth(),
            //                              virtualHeight(), newWidth, newHeight));

            energies = newEnergies;
            colors = newColors;
            isTransposed = shouldBeTransposed;
        }
    }

    private int virtualWidth() {
        return width(isTransposed);
    }

    private int virtualHeight() {
        return height(isTransposed);
    }

    private int width(boolean isTransposed) {
        return isTransposed ? height() : width();
    }

    private int height(boolean isTransposed) {
        return isTransposed ? width() : height();
    }

    private void initialize(Picture p) {
        for (int y = 0; y < virtualHeight(); ++y) {
            for (int x = 0; x < virtualWidth(); ++x) {
                colors[y][x] = p.getRGB(x, y);
            }
        }

        for (int y = 0; y < virtualHeight(); ++y) {
            for (int x = 0; x < virtualWidth(); ++x) {
                recalculateEnergy(x, y);
            }
        }
    }

    private void recalculateEnergy(int x, int y) {
        energies[y][x] = calculateEnergy(x, y);
    }

    private void removeSeam(int[] seam, boolean shouldBeTransposed) {
        checkSeam(seam, shouldBeTransposed);
        transposeIfNeeded(shouldBeTransposed);
        removeSeam(seam);
    }

    private void removeSeam(int[] seam) {
        for (int y = 0; y < virtualHeight(); ++y) {
            final int x = seam[y];
            final int count = virtualWidth() - x - 1;
            if (count != 0) {
                final int source = x + 1;
                System.arraycopy(colors[y], source, colors[y], x, count);
                System.arraycopy(energies[y], source, energies[y], x, count);
            }
        }

        if (isTransposed) {
            --height;
        }
        else {
            --width;
        }

        for (int y = 0; y < virtualHeight(); ++y) {
            final int x = seam[y];

            tryUpdateEnergy(x, y - 1); // North
            tryUpdateEnergy(x, y); // Pixel itself
            tryUpdateEnergy(x, y + 1); // South
            tryUpdateEnergy(x - 1, y); // West
        }
    }

    private void tryUpdateEnergy(int x, int y) {
        if (isValidDim(x, virtualWidth()) && isValidDim(y, virtualHeight())) {
            recalculateEnergy(x, y);
        }
    }

    private int[] findSeam() {
        final int vCount = width() * height() + 2;
        final int virtualTopIndex = vCount - 1;
        final int virtualBottomIndex = vCount - 2;

        final int[] edgeTo = new int[vCount];
        final double[] distanceTo = new double[vCount];
        Arrays.fill(distanceTo, Double.POSITIVE_INFINITY);

        distanceTo[virtualTopIndex] = 0;
        for (int x = 0; x < virtualWidth(); ++x) {
            relax(virtualTopIndex, x, 0, edgeTo, distanceTo);
        }

        final int lastRowIndex = virtualHeight() - 1;
        final int lastColumnIndex = virtualWidth() - 1;

        for (int y = 0; y < lastRowIndex; ++y) {
            for (int x = 0; x < virtualWidth(); ++x) {
                final int from = toVirtualFlatIndex(x, y);
                final int nextRowY = y + 1;

                if (x > 0) {
                    relax(from, x - 1, nextRowY, edgeTo, distanceTo);
                }

                relax(from, x, nextRowY, edgeTo, distanceTo);

                if (x < lastColumnIndex) {
                    relax(from, x + 1, nextRowY, edgeTo, distanceTo);
                }
            }
        }

        for (int x = 0; x < virtualWidth(); ++x) {
            relax(toVirtualFlatIndex(x, lastRowIndex), virtualBottomIndex, edgeTo, distanceTo, 0.0);
        }

        int seamLength = virtualHeight();
        int[] seam = new int[seamLength];

        for (int v = edgeTo[virtualBottomIndex]; v != virtualTopIndex; v = edgeTo[v]) {
            seam[--seamLength] = v % virtualWidth();
        }

        return seam;
    }

    private int toVirtualFlatIndex(int x, int y) {
        return y * virtualWidth() + x;
    }

    private void relax(int from, int x, int y, int[] edgeTo, double[] distanceTo) {
        relax(from, toVirtualFlatIndex(x, y), edgeTo, distanceTo, getEnergy(x, y));
    }

    private void relax(int from, int to, int[] edgeTo, double[] distanceTo, double energy) {
        final double candidate = distanceTo[from] + energy;
        if (candidate < distanceTo[to]) {
            distanceTo[to] = candidate;
            edgeTo[to] = from;
        }
        // StdOut.println(
        //         String.format("Relaxation %d -- %.4f --> %d: dst - %.4f, edgeTo - %d", from, energy,
        //                       to,
        //                       distanceTo[to], edgeTo[to]));
    }

    private double calculateEnergy(int x, int y) {
        if (x == 0 || x == virtualWidth() - 1) {
            return MAX_ENERGY;
        }

        if (y == 0 || y == virtualHeight() - 1) {
            return MAX_ENERGY;
        }

        double dx = getDiff(getColor(x - 1, y), getColor(x + 1, y));
        double dy = getDiff(getColor(x, y - 1), getColor(x, y + 1));

        return Math.sqrt(dx + dy);
    }

    private int getColor(int x, int y) {
        return colors[y][x];
    }

    private double getEnergy(int x, int y) {
        return energies[y][x];
    }

    private int getR(int v) {
        return (v >> 16) & 0xFF;
    }

    private int getG(int v) {
        return (v >> 8) & 0xFF;
    }

    private int getB(int v) {
        return v & 0xFF;
    }

    private double getDiff(int first, int second) {
        final int dr = getR(first) - getR(second);
        final int dg = getG(first) - getG(second);
        final int db = getB(first) - getB(second);
        return dr * dr + dg * dg + db * db;
    }

    //  unit testing (optional)
    public static void main(String[] args) {

    }

}

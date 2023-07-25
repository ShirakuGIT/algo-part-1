import edu.princeton.cs.algs4.StdRandom;
import edu.princeton.cs.algs4.StdStats;
import edu.princeton.cs.algs4.WeightedQuickUnionUF;


public class Percolation {
    private final int n; // Size of the grid (n x n)
    private boolean[][] grid; // Tracks open or blocked sites
    private final WeightedQuickUnionUF uf; // Union-find data structure for connected components
    private final WeightedQuickUnionUF ufNoBottom;
    // Union-find data structure without the bottom virtual site
    private final int virtualTop; // Virtual site at the top
    private final int virtualBottom; // Virtual site at the bottom
    private int openSites; // Count of open sites

    // Create n-by-n grid, with all sites blocked
    public Percolation(int n) {
        if (n <= 0) {
            throw new IllegalArgumentException("Grid size must be greater than 0");
        }
        this.n = n;
        grid = new boolean[n][n];
        uf = new WeightedQuickUnionUF(n * n + 2); // 2 virtual sites (top and bottom)
        ufNoBottom = new WeightedQuickUnionUF(n * n + 1); // 1 virtual site (top)

        virtualTop = 0;
        virtualBottom = n * n + 1;
        openSites = 0;
    }

    // Open site (row, col) if it is not open already
    public void open(int row, int col) {
        validateIndices(row, col);
        if (!grid[row - 1][col - 1]) {
            grid[row - 1][col - 1] = true;
            openSites++;

            int index = getIndex(row, col);

            // Connect with neighboring open sites
            if (row == 1) {
                uf.union(virtualTop, index);
                ufNoBottom.union(virtualTop, index);
            }
            if (row == n) {
                uf.union(virtualBottom, index);
            }

            // Check and connect top, bottom, left, and right sites
            connectIfOpen(index, row - 1, col);
            connectIfOpen(index, row + 1, col);
            connectIfOpen(index, row, col - 1);
            connectIfOpen(index, row, col + 1);
        }
    }

    // Is site (row, col) open?
    public boolean isOpen(int row, int col) {
        validateIndices(row, col);
        return grid[row - 1][col - 1];
    }

    // Is site (row, col) full?
    public boolean isFull(int row, int col) {
        validateIndices(row, col);
        int index = getIndex(row, col);
        return isOpen(row, col) && ufNoBottom.find(virtualTop) == ufNoBottom.find(index);
    }

    // Returns the number of open sites
    public int numberOfOpenSites() {
        return openSites;
    }

    // Does the system percolate?
    public boolean percolates() {
        return uf.find(virtualTop) == uf.find(virtualBottom);
    }

    // Validate if row and column indices are within the allowed range
    private void validateIndices(int row, int col) {
        if (row < 1 || row > n || col < 1 || col > n) {
            throw new IllegalArgumentException("Row and column indices are out of range");
        }
    }

    // Convert 2D coordinates (row, col) to a 1D index for union-find data structure
    private int getIndex(int row, int col) {
        return (row - 1) * n + col;
    }

    // Connect two sites if the neighbor is open
    private void connectIfOpen(int fromIndex, int row, int col) {
        if (row >= 1 && row <= n && col >= 1 && col <= n && isOpen(row, col)) {
            int toIndex = getIndex(row, col);
            uf.union(fromIndex, toIndex);
            ufNoBottom.union(fromIndex, toIndex);
        }
    }

    // Static method to estimate the percolation threshold p*
    public static double estimatePercolationThreshold(int n, int trials) {
        if (n <= 0 || trials <= 0) {
            throw new IllegalArgumentException("n and trials must be greater than 0");
        }

        double[] percolationThresholds = new double[trials];

        for (int i = 0; i < trials; i++) {
            Percolation percolation = new Percolation(n);
            while (!percolation.percolates()) {
                int row = StdRandom.uniform(1, n + 1);
                int col = StdRandom.uniform(1, n + 1);
                if (!percolation.isOpen(row, col)) {
                    percolation.open(row, col);
                }
            }
            percolationThresholds[i] = (double) percolation.numberOfOpenSites() / (n * n);
        }

        return StdStats.mean(percolationThresholds);
    }

    // Main method (optional) to run the estimation
    public static void main(String[] args) {
        int n = 100; // Set the grid size (n x n)
        int trials = 10000; // Set the number of trials for the Monte Carlo simulation

        double percolationThreshold = estimatePercolationThreshold(n, trials);
        System.out.println("Estimated Percolation Threshold p*: " + percolationThreshold);
    }
}

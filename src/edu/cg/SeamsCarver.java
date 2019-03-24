package edu.cg;
import java.awt.image.BufferedImage;

public class SeamsCarver extends ImageProcessor {

	// MARK: An inner interface for functional programming.
	@FunctionalInterface
	interface ResizeOperation {
		BufferedImage resize();
	}

	// MARK: Fields
	private int numOfSeams;
	private ResizeOperation resizeOp;
	boolean[][] imageMask;
	private int [][] I = new int[inHeight][inWidth];
	private int updatedWidth = inWidth;
	private BufferedImage greyScaled = greyscale();

	public SeamsCarver(Logger logger, BufferedImage workingImage, int outWidth, RGBWeights rgbWeights,
			boolean[][] imageMask) {
		super((s) -> logger.log("Seam carving: " + s), workingImage, rgbWeights, outWidth, workingImage.getHeight());
		//THIS WE ADDED!!!!

		numOfSeams = Math.abs(outWidth - inWidth);
		this.imageMask = imageMask;
		if (inWidth < 2 | inHeight < 2)
			throw new RuntimeException("Can not apply seam carving: workingImage is too small");

		if (numOfSeams > inWidth / 2)
			throw new RuntimeException("Can not apply seam carving: too many seams...");

		// Setting resizeOp by with the appropriate method reference
		// initializing the index - matrix
		if (outWidth > inWidth){
			initializeIndexMatrix();
			resizeOp = this::increaseImageWidth;
		}
		else if (outWidth < inWidth){
			initializeIndexMatrix();
			resizeOp = this::reduceImageWidth;
		}
		else {
			initializeIndexMatrix();
			resizeOp = this::duplicateWorkingImage;
		}
		this.logger.log("preliminary calculations were ended.");
	}

	private void initializeIndexMatrix(){
		for(int i = 0; i < inHeight; i++){
			for(int j = 0; j < inWidth; j++){
				I[i][j] = j;
			}
		}
	}

	private int[][] getEnergyMatrix() {
		int[][] energyMatrix = new int[inHeight][updatedWidth];
		int E1, E2, E3;
		for(int i = 0; i < inHeight - 1; i++){
			for(int j = 0; j < updatedWidth; j++){
				if(j < updatedWidth - 1){
					E1 = Math.abs(greyScaled.getRGB(j, i) - greyScaled.getRGB(I[i][j]+1,i));
				}
				else {
					E1 = Math.abs(greyScaled.getRGB(j, i) - greyScaled.getRGB(I[i][j]-1,i));
				}
				if(i < inHeight - 1){
					E2 = Math.abs(greyScaled.getRGB(j, i) - greyScaled.getRGB(I[i][j],i+1));
				}
				else {
					E2 = Math.abs(greyScaled.getRGB(j, i) - greyScaled.getRGB(I[i][j],i-1));
				}
				if(imageMask[i][j]){
					E3 = Integer.MAX_VALUE;
				}else {
					E3 = 0;
				}
				energyMatrix[i][j] = E1 + E2 + E3;
			}
		}
		return energyMatrix;
	}
	// Change to new forward looking cost - from the group what yogev sent
	private int[][] getCostMatrix() {
		int [][] energyMatrix = getEnergyMatrix();
		int[][] M = new int[inHeight][updatedWidth];
		for (int j = 0; j < updatedWidth; j++){
			M[0][j] = energyMatrix[0][j];
		}
		for (int i = 1; i < inHeight; i++){
			for (int j = 0; j < updatedWidth; j++){
				if (j == inWidth - 1 ){
					M[i][j] = energyMatrix[i][j] + Math.min(energyMatrix[i-1][j-1], energyMatrix[i-1][j]);
				}
				else if(j == 0){
					M[i][j] = energyMatrix[i][j] + Math.min( M[i-1][j], M[i-1][j+1]);
				}
				else{
					M[i][j] = energyMatrix[i][j] + Math.min(Math.min(M[i-1][j-1], M[i-1][j]), M[i-1][j+1]);
				}
			}
		}
		return M;
	}

	public BufferedImage resize()
	{
		return resizeOp.resize();
	}

	private int[] findMinSeam()
	{
		int[][] cost = getCostMatrix();
		int[] minSeam = new int[inHeight];
		int minValue = cost[inHeight - 1][0] ;
		minSeam[inHeight - 1] = 0;
		for(int j = 0; j < inWidth; j++) {
			if (cost[inHeight - 1][j] < minValue) {
				minValue = cost[inHeight - 1][j];
				minSeam[inHeight - 1] = j;
			}
		}
		for(int i = inHeight - 2; i >= 0; i--){
			if(minSeam[i+1] == 0){
				minSeam[i] = Math.min(cost[i][minSeam[i+1]], cost[i][minSeam[i+1] + 1]);
			}
			else if(minSeam[i+1] == inWidth - 1){
				minSeam[i] = Math.min(cost[i][minSeam[i+1]], cost[i][minSeam[i+1] - 1]);
			}
			else {
				minSeam[i] = Math.min(Math.min(cost[i][minSeam[i+1]], cost[i][minSeam[i+1] + 1]), cost[i][minSeam[i+1] + -1]);
			}
		}
		return minSeam;
	}

	private void shiftRight(int[] arr)
	{
		for (int i = 0; i < inHeight; i++){
			for(int j = 0; j < updatedWidth; j++){
				if(I[i][j] > arr[i]){
					int temp = I[i][j-1];
					I[i][j-1] = I[i][j];
					I[i][j] = temp;
				}
			}
		}
	}
	// check creation of reducedImage - not sure it's correct with the I indexes at the end.
	private BufferedImage reduceImageWidth() {
		for (int i = 0; i < numOfSeams; i++){
			int[] minSeam = findMinSeam();
			shiftRight(minSeam);
			updatedWidth--;
		}
		BufferedImage reducedImage = newEmptyOutputSizedImage();
		for(int i = 0; i < inHeight; i++){
			for(int j = 0; j < updatedWidth; j++){
				reducedImage.setRGB(j,i,workingImage.getRGB(I[i][j],i));
			}
		}
		return reducedImage;
	}

	private BufferedImage increaseImageWidth() {
		// TODO: Implement this method, remove the exception.
		throw new UnimplementedMethodException("increaseImageWidth");
	}

	public BufferedImage showSeams(int seamColorRGB) {
		// TODO: Implement this method (bonus), remove the exception.
		throw new UnimplementedMethodException("showSeams");
	}

	public boolean[][] getMaskAfterSeamCarving() {
		// TODO: Implement this method, remove the exception.
		// This method should return the mask of the resize image after seam carving. Meaning,
		// after applying Seam Carving on the input image, getMaskAfterSeamCarving() will return
		// a mask, with the same dimensions as the resized image, where the mask values match the
		// original mask values for the corresponding pixels.
		// HINT:
		// Once you remove (replicate) the chosen seams from the input image, you need to also
		// remove (replicate) the matching entries from the mask as well.
		throw new UnimplementedMethodException("getMaskAfterSeamCarving");

	}
}

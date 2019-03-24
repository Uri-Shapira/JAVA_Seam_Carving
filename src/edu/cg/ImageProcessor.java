package edu.cg;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class ImageProcessor extends FunctioalForEachLoops {

	// MARK: fields
	public final Logger logger;
	public final BufferedImage workingImage;
	public final RGBWeights rgbWeights;
	public final int inWidth;
	public final int inHeight;
	public final int workingImageType;
	public final int outWidth;
	public final int outHeight;

	// MARK: constructors
	public ImageProcessor(Logger logger, BufferedImage workingImage, RGBWeights rgbWeights, int outWidth,
			int outHeight) {
		super(); // initializing for each loops...

		this.logger = logger;
		this.workingImage = workingImage;
		this.rgbWeights = rgbWeights;
		inWidth = workingImage.getWidth();
		inHeight = workingImage.getHeight();
		workingImageType = workingImage.getType();
		this.outWidth = outWidth;
		this.outHeight = outHeight;
		setForEachInputParameters();
	}

	public ImageProcessor(Logger logger, BufferedImage workingImage, RGBWeights rgbWeights) {
		this(logger, workingImage, rgbWeights, workingImage.getWidth(), workingImage.getHeight());
	}

	// MARK: change picture hue - example
	public BufferedImage changeHue() {
		logger.log("Prepareing for hue changing...");

		int r = rgbWeights.redWeight;
		int g = rgbWeights.greenWeight;
		int b = rgbWeights.blueWeight;
		int max = rgbWeights.maxWeight;

		BufferedImage ans = newEmptyInputSizedImage();

		forEach((y, x) -> {
			Color c = new Color(workingImage.getRGB(x, y));
			int red = r * c.getRed() / max;
			int green = g * c.getGreen() / max;
			int blue = b * c.getBlue() / max;
			Color color = new Color(red, green, blue);
			ans.setRGB(x, y, color.getRGB());
		});

		logger.log("Changing hue done!");

		return ans;
	}

	public final void setForEachInputParameters() {
		setForEachParameters(inWidth, inHeight);
	}

	public final void setForEachOutputParameters() {
		setForEachParameters(outWidth, outHeight);
	}

	public final BufferedImage newEmptyInputSizedImage() {
		return newEmptyImage(inWidth, inHeight);
	}

	public final BufferedImage newEmptyOutputSizedImage() {
		return newEmptyImage(outWidth, outHeight);
	}

	public final BufferedImage newEmptyImage(int width, int height) {
		return new BufferedImage(width, height, workingImageType);
	}

	// A helper method that deep copies the current working image.
	public final BufferedImage duplicateWorkingImage() {
		BufferedImage output = newEmptyInputSizedImage();
		setForEachInputParameters();
		forEach((y, x) -> output.setRGB(x, y, workingImage.getRGB(x, y)));

		return output;
	}

	public BufferedImage greyscale() {
		BufferedImage resultImage = newEmptyInputSizedImage();
		logger.log("Preparing for greyscale changing ...");
		for(int i = 0; i < outWidth;i++)
		{
			for(int j = 0; j < outHeight; j ++)
			{
				Color color = new Color(workingImage.getRGB(i ,j));
				int red = rgbWeights.redWeight * color.getRed() / rgbWeights.maxWeight;
				int blue = rgbWeights.blueWeight * color.getBlue() / rgbWeights.maxWeight;
				int green = rgbWeights.greenWeight * color.getGreen() / rgbWeights.maxWeight;
				int sum = red + blue + green;
				int average = sum / (rgbWeights.redWeight + rgbWeights.blueWeight + rgbWeights.greenWeight);
				Color updatedColor = new Color(average, average, average);
				resultImage.setRGB(i , j , updatedColor.getRGB());
			}
		}
		logger.log("Greyscale changing done...");
		return resultImage;
	}

	public BufferedImage nearestNeighbor() {
		logger.log("Preparing for nearest neighbor changing...");
		BufferedImage resultImage = newEmptyOutputSizedImage();
		double heightFactor = (double)inHeight / (double)outHeight;
		double widthFactor = (double)inWidth / (double)outWidth;
		for (int i = 0; i < outWidth; i++){
			for(int j = 0; j < outHeight; j++){
				int neighborWidth = (int)(i * widthFactor);
				int neighborHeight = (int)(j * heightFactor);
				Color pixelColor = new Color(workingImage.getRGB(neighborWidth, neighborHeight));
				int red = pixelColor.getRed();
				int green = pixelColor.getGreen();
				int blue = pixelColor.getBlue();
				Color newColor = new Color(red, green, blue);
				resultImage.setRGB(i, j, newColor.getRGB());
			}
		}
		logger.log("Nearest neighbor changing done...");
		return resultImage;
	}
}
